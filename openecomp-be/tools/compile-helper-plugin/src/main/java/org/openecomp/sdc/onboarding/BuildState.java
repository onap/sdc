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

import static org.openecomp.sdc.onboarding.BuildHelper.readState;
import static org.openecomp.sdc.onboarding.Constants.ANSI_COLOR_RESET;
import static org.openecomp.sdc.onboarding.Constants.ANSI_YELLOW;
import static org.openecomp.sdc.onboarding.Constants.FULL_BUILD_DATA;
import static org.openecomp.sdc.onboarding.Constants.FULL_RESOURCE_BUILD_DATA;
import static org.openecomp.sdc.onboarding.Constants.JAR;
import static org.openecomp.sdc.onboarding.Constants.MODULE_BUILD_DATA;
import static org.openecomp.sdc.onboarding.Constants.RESOURCES_CHANGED;
import static org.openecomp.sdc.onboarding.Constants.RESOURCE_BUILD_DATA;
import static org.openecomp.sdc.onboarding.Constants.SKIP_MAIN_SOURCE_COMPILE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

public class BuildState {

    private static final String SHUTDOWN_TIME = "shutdownTime";
    private static final String VERSION = "version";

    private static Map<String, Map> compileDataStore = new HashMap<>();
    private static Map<String, Object> moduleBuildData = new HashMap<>();
    private static Map<String, Object> resourceBuildData = new HashMap<>();
    private static Map<String, Artifact> artifacts = new HashMap<>();
    private static Set<String> executeTestsIfDependsOnStore = new HashSet<>();
    private static Set<String> pmdExecutedInRun = new HashSet<>();
    private static File stateFileLocation =
            new File(Paths.get(System.getProperties().getProperty("java.io.tmpdir")).toFile(), "compileState.dat");

    private static File compileStateFile;
    private MavenProject project;
    private String compileStateFilePath;

    static {
        initializeStore();
        Optional<HashMap> masterStore = readState("compile.dat", HashMap.class);
        compileDataStore = masterStore.isPresent() ? masterStore.get() : compileDataStore;
        String version = String.class.cast(compileDataStore.get(VERSION));
        if (version != null) {
            stateFileLocation = new File(Paths.get(System.getProperties().getProperty("java.io.tmpdir")).toFile(),
                    "compileState.dat-" + version);
        }
        if (stateFileLocation.exists()) {
            HashMap dat = loadState(stateFileLocation);
            if (swapStates((HashMap<?, ?>) compileDataStore, dat)) {
                compileDataStore = dat;
            }
        }
    }


    void init() {
        artifacts.clear();
        for (Artifact artifact : project.getArtifacts()) {
            if (artifact.isSnapshot() && JAR.equals(artifact.getType())) {
                artifacts.put(artifact.getGroupId() + ":" + artifact.getArtifactId(), artifact);
            }
        }
        if (compileStateFile == null) {
            setCompileStateFile(
                    getCompileStateFile(compileStateFilePath.substring(0, compileStateFilePath.indexOf('/')), project));
        }
    }

    private static void setCompileStateFile(File file) {
        compileStateFile = file;
    }

    static void initializeStore() {
        compileDataStore.put(FULL_BUILD_DATA, new HashMap<>());
        compileDataStore.put(FULL_RESOURCE_BUILD_DATA, new HashMap<>());
        compileDataStore.put(MODULE_BUILD_DATA, new HashMap<>());
        compileDataStore.put(RESOURCE_BUILD_DATA, new HashMap<>());
    }


    static void recordPMDRun(String moduleCoordinates) {
        pmdExecutedInRun.add(moduleCoordinates);
    }

    static boolean isPMDRun(String moduleCoordintes) {
        return pmdExecutedInRun.contains(moduleCoordintes);
    }

    private void writeCompileState() throws IOException {
        writeState(compileStateFile, compileDataStore);
    }

    private void writeState(File file, Map store) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(store);
        }
    }


    private File getCompileStateFile(String moduleCoordinate, MavenProject proj) {
        return getStateFile(moduleCoordinate, proj, compileStateFilePath);
    }


    private File getStateFile(String moduleCoordinate, MavenProject proj, String filePath) {
        return new File(getTopParentProject(moduleCoordinate, proj).getBasedir(),
                filePath.substring(filePath.indexOf('/') + 1));
    }

    MavenProject getTopParentProject(String moduleCoordinate, MavenProject proj) {
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
        Long lastTime = Long.class.cast(compileDataStore.get(FULL_BUILD_DATA).put(moduleCoordinates, buildTime));
        try {
            if (lastTime == null || !lastTime.equals(buildTime)) {
                boolean skipMainCompile = project.getProperties().containsKey(SKIP_MAIN_SOURCE_COMPILE);
                if (!skipMainCompile) {
                    writeCompileState();
                }
            }
        } catch (IOException ignored) {
            // ignored. No need to handle. System will take care.
        }
    }

    void addResourceBuildTime(String moduleCoordinates, Long buildTime) {
        if (project.getProperties().containsKey(RESOURCES_CHANGED)
                    || compileDataStore.get(FULL_RESOURCE_BUILD_DATA).get(moduleCoordinates) == null) {
            try {
                compileDataStore.get(FULL_RESOURCE_BUILD_DATA).put(moduleCoordinates, buildTime);
                writeCompileState();
            } catch (IOException ignored) {
                // ignored. No need to handle. System will take care.
            }
        }
    }

    void addModuleBuildData(String moduleCoordinates, Map moduleBuildDependencies) {
        moduleBuildData.put(moduleCoordinates, moduleBuildDependencies);
    }

    Map<String, Object> readModuleBuildData() {
        return HashMap.class.cast(compileDataStore.get(MODULE_BUILD_DATA).get(getModuleCoordinate(project)));
    }

    void saveModuleBuildData(String moduleCoordinate) {
        if (moduleBuildData.get(moduleCoordinate) != null) {
            compileDataStore.get(MODULE_BUILD_DATA).put(moduleCoordinate, moduleBuildData.get(moduleCoordinate));
        }
        saveCompileData();
    }

    void saveResourceBuildData(String moduleCoordinate) {
        if (resourceBuildData.get(moduleCoordinate) != null) {
            compileDataStore.get(RESOURCE_BUILD_DATA).put(moduleCoordinate, resourceBuildData.get(moduleCoordinate));
        }
        saveCompileData();
    }

    void saveCompileData() {
        saveBuildData(compileStateFile, compileDataStore);
    }

    void markTestsMandatoryModule(String moduleCoordinates) {
        executeTestsIfDependsOnStore.add(moduleCoordinates);
    }

    private void saveBuildData(File file, Object dataToSave) {
        file.getParentFile().mkdirs();
        if (dataToSave != null) {
            try (FileOutputStream fos = new FileOutputStream(file);
                 ObjectOutputStream ois = new ObjectOutputStream(fos)) {
                ois.writeObject(dataToSave);
            } catch (IOException ignored) {
                // ignored. No need to handle. System will take care.
            }
        }
    }

    Map<String, Object> readResourceBuildData() {
        return HashMap.class.cast(compileDataStore.get(RESOURCE_BUILD_DATA).get(getModuleCoordinate(project)));
    }


    void addResourceBuildData(String moduleCoordinates, Map currentModuleResourceBuildData) {
        resourceBuildData.put(moduleCoordinates, currentModuleResourceBuildData);
    }

    Long getBuildTime(String moduleCoordinates) {
        Long buildTime = Long.class.cast(compileDataStore.get(FULL_BUILD_DATA).get(moduleCoordinates));
        return buildTime == null ? 0 : buildTime;
    }

    Long getResourceBuildTime(String moduleCoordinates) {
        Long resourceBuildTime = Long.class.cast(compileDataStore.get(FULL_RESOURCE_BUILD_DATA).get(moduleCoordinates));
        return resourceBuildTime == null ? 0 : resourceBuildTime;
    }

    boolean isCompileMust(String moduleCoordinates, Collection<String> dependencies) {
        for (String d : dependencies) {
            if (artifacts.containsKey(d) && JAR.equals(artifacts.get(d).getType())) {
                boolean versionEqual = artifacts.get(d).getVersion().equals(project.getVersion());
                if (versionEqual && getBuildTime(d) == 0) {
                    System.err.println(ANSI_YELLOW + "[WARNING:]" + "You have module[" + d
                                               + "] not locally compiled even once, please compile your project once daily from root to have reliable build results."
                                               + ANSI_COLOR_RESET);
                    return true;
                }
            }
        }
        return isMust(this::getBuildTime, moduleCoordinates, dependencies);
    }

    boolean isTestExecutionMandatory() {
        for (String d : artifacts.keySet()) {
            if (executeTestsIfDependsOnStore.contains(d)) {
                return true;
            }
        }
        return false;
    }

    boolean isTestMust(String moduleCoordinates) {
        return getBuildTime(moduleCoordinates) > getResourceBuildTime(moduleCoordinates) || isMust(
                this::getResourceBuildTime, moduleCoordinates, artifacts.keySet());
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

    private static HashMap loadState(File file) {
        try (InputStream is = new FileInputStream(file); ObjectInputStream ois = new ObjectInputStream(is)) {
            return HashMap.class.cast(ois.readObject());
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private static boolean swapStates(HashMap repo, HashMap last) {
        Long repoTime = repo.get(SHUTDOWN_TIME) == null ? 0 : (Long) repo.get(SHUTDOWN_TIME);
        Long lastTime = last.get(SHUTDOWN_TIME) == null ? 0 : (Long) last.get(SHUTDOWN_TIME);
        String repoVersion = repo.get(VERSION) == null ? "" : (String) repo.get(VERSION);
        String lastVersion = last.get(VERSION) == null ? "" : (String) last.get(VERSION);
        long repoBuildNumber = repoTime % 1000;
        long lastBuildNumber = lastTime % 1000;
        if (repoBuildNumber != lastBuildNumber) {
            return false;
        }
        if (!repoVersion.equals(lastVersion)) {
            return false;
        }
        return Long.compare(repoTime, lastTime) < 0;
    }


}
