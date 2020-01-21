/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.asdctool.migration.scanner;

import org.apache.commons.io.FileUtils;
import org.openecomp.sdc.asdctool.migration.core.MigrationException;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * scan and instantiate classes of given type in the class path
 */
public class ClassScanner {


    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    public <T> List<T> getAllClassesOfType(String basePackage, Class<T> ofType) {
        Collection<File> allClassesInPackage = getAllClassesInPackage(basePackage);
        List<T> loadedClasses = new ArrayList<>();
        for (File clazzFile : allClassesInPackage) {
            Optional<T> instance = loadAndInstantiateClass(getClassReference(clazzFile), ofType);
            instance.ifPresent(loadedClasses::add);
        }
        return loadedClasses;
    }

    private <T> Optional<T> loadAndInstantiateClass(String classReference, Class<T> ofType)  {
        try {
            return instantiateClassOfType(classReference, ofType);
        }catch (ClassNotFoundException e) {
            //log
            throw new MigrationException(String.format("could not find class %s of type %s. cause: %s", classReference, ofType.toGenericString(), e.getMessage()));
        } catch (IllegalAccessException e1) {
            //log
            throw new MigrationException(String.format("could not instantiate class %s of type %s. class is not accessible. cause: %s", classReference, ofType.toGenericString(), e1.getMessage()));
        } catch (InstantiationException e2) {
            //log
            throw new MigrationException(String.format("could not instantiate class %s of type %s. cause: %s", classReference, ofType.toGenericString(), e2.getMessage()));
        }
    }

    private <T> Optional<T> instantiateClassOfType(String classReference, Class<T> ofType) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        String className = classReference.replaceAll(".class$", "").replaceAll(".class", "");
        Class<?> aClass = classLoader.loadClass(className);
        if (ofType.isAssignableFrom(aClass) && isInstantiateAbleClass(aClass)){
            return Optional.of((T) aClass.newInstance());
        }
        return Optional.empty();
    }

    private boolean isInstantiateAbleClass(Class<?> clazz) {
        return !Modifier.isAbstract(clazz.getModifiers()) && !clazz.isEnum() && !clazz.isAnonymousClass() && !clazz.isInterface();
    }

    private Collection<File> getAllClassesInPackage(String fromPackage) {
        String path = fromPackage.replace(".", "/");
        URL resource = classLoader.getResource(path);
        if (noMigrationTasks(resource)) {
            return Collections.emptyList();
        }
        return FileUtils.listFiles(new File(resource.getFile()), new String[]{"class"}, true);
    }

    private boolean noMigrationTasks(URL resource) {
        return resource == null;
    }

    private String getClassReference(File classFile) {
        String asPackage = classFile.getPath().replace(File.separator, ".");
        String classes = "classes.";
        return asPackage.substring(asPackage.indexOf(classes) + classes.length());
    }
}
