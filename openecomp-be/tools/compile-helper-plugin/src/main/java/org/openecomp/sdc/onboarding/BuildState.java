/*
 * Copyright © 2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on a "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.onboarding;

import static org.openecomp.sdc.onboarding.Constants.RESOURCES_CHANGED;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.maven.project.MavenProject;

public class BuildState {

    private static Map<String, Long> fullBuildData = new HashMap<>();
    private static Map<String, Long> fullResourceBuildData = new HashMap<>();
    private static Map<String, Object> moduleBuildData = new HashMap<>();
    private static Map<String, Object> resourceBuildData = new HashMap<>();

    private static File buildStateFile;
    private static File resourceStateFile;
    private File moduleBuildDataFile;
    private File resourceBuildDataFile;
    private MavenProject project;
    private String buildStateFilePath;
    private String resourceStateFilePath;

    private void readFullBuildState() {
        buildStateFile = initialize(this::getBuildStateFile, fullBuildData,
                buildStateFilePath.substring(0, buildStateFilePath.indexOf('/')), project);
    }

    private void readResourceBuildState() {
        resourceStateFile = initialize(this::getResourceStateFile, fullResourceBuildData,
                resourceStateFilePath.substring(0, resourceStateFilePath.indexOf('/')), project);

    }

    private File initialize(BiFunction<String, MavenProject, File> funct, Map store, String moduleCoordinate,
            MavenProject proj) {
        File file = funct.apply(moduleCoordinate, proj);
        file.getParentFile().mkdirs();
        try (FileInputStream fis = new FileInputStream(file); ObjectInputStream ois = new ObjectInputStream(fis);) {
            if (store.isEmpty()) {
                store.putAll(HashMap.class.cast(ois.readObject()));
            }
        } catch (Exception e) {
            store.clear();
        }
        return file;
    }

    private void writeFullBuildState() throws IOException {
        writeState(buildStateFile, fullBuildData);
    }

    private void writeFullResourceBuildState() throws IOException {
        writeState(resourceStateFile, fullResourceBuildData);
    }

    private void writeState(File file, Map store) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(store);
        }
    }

    private File getBuildStateFile(String moduleCoordinate, MavenProject proj) {
        return getStateFile(moduleCoordinate, proj, buildStateFilePath);
    }

    private File getResourceStateFile(String moduleCoordinate, MavenProject proj) {
        return getStateFile(moduleCoordinate, proj, resourceStateFilePath);
    }

    private File getStateFile(String moduleCoordinate, MavenProject proj, String filePath) {
        return new File(getTopParentProject(moduleCoordinate, proj).getBasedir(),
                filePath.substring(filePath.indexOf('/') + 1));
    }

    private MavenProject getTopParentProject(String moduleCoordinate, MavenProject proj) {
        if (getModuleCoordinate(proj).equals(moduleCoordinate) || proj.getParent() == null) {
            return proj;
        } else {
            return getTopParentProject(moduleCoordinate, proj.getParent());
        }
    }

    private String getModuleCoordinate(MavenProject project) {
        return project.getGroupId() + ":" + project.getArtifactId();
    }

    void addModuleBuildTime(String moduleCoordinates, Long buildTime) {
        Long lastTime = fullBuildData.put(moduleCoordinates, buildTime);
        try {
            if (lastTime == null || !lastTime.equals(buildTime)) {
                writeFullBuildState();
            }
        } catch (IOException ignored) {
            // ignored. No need to handle. System will take care.
        }
    }

    void addResourceBuildTime(String moduleCoordinates, Long buildTime) {
        if (project.getProperties().containsKey(RESOURCES_CHANGED)) {
            Long lastTime = fullResourceBuildData.put(moduleCoordinates, buildTime);
            try {
                writeFullResourceBuildState();
            } catch (IOException ignored) {
                // ignored. No need to handle. System will take care.
            }
        }
    }

    void addModuleBuildData(String moduleCoordinates, Map moduleBuildDependencies) {
        moduleBuildData.put(moduleCoordinates, moduleBuildDependencies);
    }

    Map<String, Object> readModuleBuildData() {
        return readBuildData(moduleBuildDataFile);
    }

    void saveModuleBuildData(String moduleCoordinate) {
        saveBuildData(moduleBuildDataFile, moduleBuildData.get(moduleCoordinate));
    }

    void saveResourceBuildData(String moduleCoordinate) {
        saveBuildData(resourceBuildDataFile, resourceBuildData.get(moduleCoordinate));
    }

    private void saveBuildData(File file, Object dataToSave) {
        file.getParentFile().mkdirs();
        if (dataToSave != null) {
            try (FileOutputStream fos = new FileOutputStream(file);
                 ObjectOutputStream ois = new ObjectOutputStream(fos)) {
                ois.writeObject(dataToSave);
            } catch (IOException ignored) {
                //ignored. do nothing. system will take care.
            }
        }
    }

    Map<String, Object> readResourceBuildData() {
        return readBuildData(resourceBuildDataFile);
    }

    private Map<String, Object> readBuildData(File file) {
        try (FileInputStream fis = new FileInputStream(file); ObjectInputStream ois = new ObjectInputStream(fis)) {
            return HashMap.class.cast(ois.readObject());
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    void addResourceBuildData(String moduleCoordinates, Map currentModuleResourceBuildData) {
        resourceBuildData.put(moduleCoordinates, currentModuleResourceBuildData);
    }

    Long getBuildTime(String moduleCoordinates) {
        if (fullBuildData.isEmpty()) {
            readFullBuildState();
            readResourceBuildState();
        }
        Long buildTime = fullBuildData.get(moduleCoordinates);
        return buildTime == null ? 0 : buildTime;
    }

    Long getResourceBuildTime(String moduleCoordinates) {
        Long resourceBuildTime = fullResourceBuildData.get(moduleCoordinates);
        return resourceBuildTime == null ? 0 : resourceBuildTime;
    }

    boolean isCompileMust(String moduleCoordinates, Collection<String> dependencies) {
        return isMust(this::getBuildTime, moduleCoordinates, dependencies);
    }

    boolean isTestMust(String moduleCoordinates, Collection<String> dependencies) {
        return isMust(this::getResourceBuildTime, moduleCoordinates, dependencies);
    }

    private boolean isMust(Function<String, Long> funct, String moduleCoordinates, Collection<String> dependencies) {
        Long time = funct.apply(moduleCoordinates);
        if (time == null || time == 0) {
            return true;
        }
        for (String module : dependencies) {
            Long buildTime = funct.apply(module);
            if (buildTime >= time) {
                return true;
            }
        }
        return false;
    }

    void markModuleDirty(File file) throws IOException {
        if (file.exists()) {
            Stream<String> lines = Files.lines(file.toPath());
            Iterator<String> itr = lines.iterator();
            while (itr.hasNext()) {
                String line = itr.next();
                Path path = Paths.get(line);
                if (path.toFile().exists()) {
                    if (path.toFile().setLastModified(System.currentTimeMillis())) {
                        break;
                    } else {
                        continue;
                    }
                }
            }
        }
    }

}
