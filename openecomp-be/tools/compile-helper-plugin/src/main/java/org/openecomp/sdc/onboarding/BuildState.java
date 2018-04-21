/*
 * Copyright © 2016-2017 European Support Limited
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
import java.util.stream.Stream;
import org.apache.maven.project.MavenProject;

public class BuildState {

    private static Map<String, Long> fullBuildData = new HashMap<>();
    private static Map<String, Object> moduleBuildData = new HashMap<>();

    private static File buildStateFile;
    private File moduleBuildDataFile;
    private MavenProject project;
    private String buildStateFilePath;

    private void readFullBuildState() {
        buildStateFile = getBuildStateFile(buildStateFilePath.substring(0, buildStateFilePath.indexOf('/')), project);
        buildStateFile.getParentFile().mkdirs();
        try (FileInputStream fis = new FileInputStream(buildStateFile);
             ObjectInputStream ois = new ObjectInputStream(fis);) {
            if (fullBuildData.isEmpty()) {
                fullBuildData = HashMap.class.cast(ois.readObject());
            }
        } catch (Exception e) {
            fullBuildData = new HashMap<>();
        }
    }

    private void writeFullBuildState() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(buildStateFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos);) {
            oos.writeObject(fullBuildData);
        }
    }

    private File getBuildStateFile(String moduleCoordinate, MavenProject proj) {
        return new File(getTopParentProject(moduleCoordinate, proj).getBasedir(),
                buildStateFilePath.substring(buildStateFilePath.indexOf('/') + 1));
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
        } catch (IOException e) {
            // ignore. No need to handle. System will take care.
        }
    }

    void addModuleBuildData(String moduleCoordinates, Map moduleBuildDependencies) {
        moduleBuildData.put(moduleCoordinates, moduleBuildDependencies);
    }

    Map<String, String> readModuleBuildData() {
        //        File file = new File(
        //                module.getBasedir().getAbsolutePath() + File.separator + "target" + File.separator + "maven-status",
        //                "ModuleDependencies.dat");
        try (FileInputStream fis = new FileInputStream(moduleBuildDataFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return HashMap.class.cast(ois.readObject());
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    void saveModuleBuildData(String moduleCoordinate) {
        Object currentModuleBuildData = moduleBuildData.get(moduleCoordinate);
        if (currentModuleBuildData != null) {
            //            File file = new File(project.getBasedir().getAbsolutePath() + File.separator + "target" + File.separator
            //                                         + "maven-status", "ModuleDependencies.dat");
            try (FileOutputStream fos = new FileOutputStream(moduleBuildDataFile);
                 ObjectOutputStream ois = new ObjectOutputStream(fos)) {
                ois.writeObject(currentModuleBuildData);
            } catch (IOException e) {
                //ignore. do nothing. system will take care.
            }
        }
    }

    Long getBuildTime(String moduleCoordinates) {
        if (fullBuildData.isEmpty()) {
            readFullBuildState();
        }
        Long buildTime = fullBuildData.get(moduleCoordinates);
        return buildTime == null ? 0 : buildTime;
    }

    boolean isCompileMust(String moduleCoordinates, Collection<String> dependencies) {
        Long lastBuildTime = getBuildTime(moduleCoordinates);
        if (lastBuildTime == null || lastBuildTime == 0) {
            return true;
        }
        for (String module : dependencies) {
            Long dependencyBuildTime = getBuildTime(module);
            if (dependencyBuildTime >= lastBuildTime) {
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
                    path.toFile().setLastModified(System.currentTimeMillis());
                    break;
                }
            }
        }
    }

}
