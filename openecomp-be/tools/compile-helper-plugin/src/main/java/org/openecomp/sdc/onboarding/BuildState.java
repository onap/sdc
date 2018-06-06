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
import static org.openecomp.sdc.onboarding.Constants.RESOURCES_CHANGED;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

public class BuildState {

    private static final String SHUTDOWN_TIME = "shutdownTime";
    private static final String VERSION = "version";

    private static HashMap<String, Map> compileDataStore = new HashMap<>();
    private static final Map<String, Object> MODULE_BUILD_DATA = new HashMap<>();
    private static final Map<String, Object> RESOURCE_BUILD_DATA = new HashMap<>();
    private static final Map<String, Artifact> ARTIFACTS = new HashMap<>();
    private static final Set<String> EXECUTE_TESTS_IF_DEPENDS_ON_STORE = new HashSet<>();
    private static final Set<String> PMD_EXECUTED_IN_RUN = new HashSet<>();
    private static File stateFileLocation =
            new File(Paths.get(System.getProperties().getProperty("java.io.tmpdir")).toFile(), "compileState.dat");

    private static File compileStateFile;
    private static final Logger LOG = Logger.getAnonymousLogger();
    private MavenProject project;
    private String compileStateFilePath;

    static {
        initializeStore();
        Optional<HashMap> masterStore = readState("compile.dat", HashMap.class);
        //noinspection unchecked
        compileDataStore = (HashMap) masterStore.orElseGet(() -> compileDataStore);
        String version = String.class.cast(compileDataStore.get(VERSION));
        if (version != null) {
            stateFileLocation = new File(Paths.get(System.getProperties().getProperty("java.io.tmpdir")).toFile(),
                    "compileState.dat-" + version);
        }
        if (stateFileLocation.exists()) {
            HashMap dat = loadState(stateFileLocation);
            if (swapStates(compileDataStore, dat)) {
                compileDataStore = dat;
            }
        }
    }


    void init() {
        ARTIFACTS.clear();
        for (Artifact artifact : project.getArtifacts()) {
            if (artifact.isSnapshot() && JAR.equals(artifact.getType())) {
                ARTIFACTS.put(artifact.getGroupId() + ":" + artifact.getArtifactId(), artifact);
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
        compileDataStore.put(Constants.FULL_BUILD_DATA, new HashMap<>());
        compileDataStore.put(Constants.FULL_RESOURCE_BUILD_DATA, new HashMap<>());
        compileDataStore.put(Constants.MODULE_BUILD_DATA, new HashMap<>());
        compileDataStore.put(Constants.RESOURCE_BUILD_DATA, new HashMap<>());
    }


    static void recordPMDRun(String moduleCoordinates) {
        PMD_EXECUTED_IN_RUN.add(moduleCoordinates);
    }

    static boolean isPMDRun(String moduleCoordinates) {
        return PMD_EXECUTED_IN_RUN.contains(moduleCoordinates);
    }

    private void writeCompileState() throws IOException {
        writeState(compileStateFile, compileDataStore);
    }

    private void writeState(File file, HashMap store) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(store);
        }
    }


    private File getCompileStateFile(String moduleCoordinate, MavenProject mavenProject) {
        return getStateFile(moduleCoordinate, mavenProject, compileStateFilePath);
    }


    private File getStateFile(String moduleCoordinate, MavenProject mavenProject, String filePath) {
        return new File(getTopParentProject(moduleCoordinate, mavenProject).getBasedir(),
                filePath.substring(filePath.indexOf('/') + 1));
    }

    MavenProject getTopParentProject(String moduleCoordinate, MavenProject mavenProject) {
        if (getModuleCoordinate(mavenProject).equals(moduleCoordinate) || mavenProject.getParent() == null) {
            return mavenProject;
        } else {
            return getTopParentProject(moduleCoordinate, mavenProject.getParent());
        }
    }

    private String getModuleCoordinate(MavenProject project) {
        return project.getGroupId() + ":" + project.getArtifactId();
    }

    void addModuleBuildTime(String moduleCoordinates, Long buildTime) {
        Map fullBuildData = compileDataStore.get(FULL_BUILD_DATA);
        @SuppressWarnings("unchecked") Long lastTime = (Long) fullBuildData.put(moduleCoordinates, buildTime);
        try {
            if (lastTime == null || !lastTime.equals(buildTime)) {
                boolean skipMainCompile = project.getProperties().containsKey(SKIP_MAIN_SOURCE_COMPILE);
                if (!skipMainCompile) {
                    writeCompileState();
                }
            }
        } catch (IOException e) {
            LOG.log(Level.FINE, e.getMessage(), e);
        }
    }

    void addResourceBuildTime(String moduleCoordinates, Long buildTime) {
        Map fullResourceBuildData = compileDataStore.get(FULL_RESOURCE_BUILD_DATA);
        if (project.getProperties().containsKey(RESOURCES_CHANGED)
                    || fullResourceBuildData.get(moduleCoordinates) == null) {
            try {
                //noinspection unchecked
                fullResourceBuildData.put(moduleCoordinates, buildTime);
                writeCompileState();
            } catch (IOException e) {
                LOG.log(Level.FINE, e.getMessage(), e);
            }
        }
    }

    void addModuleBuildData(String moduleCoordinates, Map moduleBuildDependencies) {
        MODULE_BUILD_DATA.put(moduleCoordinates, moduleBuildDependencies);
    }

    HashMap readModuleBuildData() {
        return (HashMap) compileDataStore.get(Constants.MODULE_BUILD_DATA).get(getModuleCoordinate(project));
    }

    void saveModuleBuildData(String moduleCoordinate) {
        if (MODULE_BUILD_DATA.get(moduleCoordinate) != null) {
            Map buildData = compileDataStore.get(Constants.MODULE_BUILD_DATA);
            //noinspection unchecked
            buildData.put(moduleCoordinate, MODULE_BUILD_DATA.get(moduleCoordinate));
        }
        saveCompileData();
    }

    void saveResourceBuildData(String moduleCoordinate) {
        if (RESOURCE_BUILD_DATA.get(moduleCoordinate) != null) {
            Map buildData = compileDataStore.get(Constants.RESOURCE_BUILD_DATA);
            //noinspection unchecked
            buildData.put(moduleCoordinate, RESOURCE_BUILD_DATA.get(moduleCoordinate));
        }
        saveCompileData();
    }

    void saveCompileData() {
        saveBuildData(compileStateFile, compileDataStore);
    }

    void markTestsMandatoryModule(String moduleCoordinates) {
        EXECUTE_TESTS_IF_DEPENDS_ON_STORE.add(moduleCoordinates);
    }

    private void saveBuildData(File file, Object dataToSave) {
        file.getParentFile().mkdirs();
        if (dataToSave != null) {
            try (FileOutputStream fos = new FileOutputStream(file);
                    ObjectOutputStream ois = new ObjectOutputStream(fos)) {
                ois.writeObject(dataToSave);
            } catch (IOException e) {
                LOG.log(Level.FINE, e.getMessage(), e);
            }
        }
    }

    HashMap readResourceBuildData() {
        return (HashMap) compileDataStore.get(Constants.RESOURCE_BUILD_DATA).get(getModuleCoordinate(project));
    }


    void addResourceBuildData(String moduleCoordinates, Map currentModuleResourceBuildData) {
        RESOURCE_BUILD_DATA.put(moduleCoordinates, currentModuleResourceBuildData);
    }

    Long getBuildTime(String moduleCoordinates) {
        Long buildTime = (Long) compileDataStore.get(FULL_BUILD_DATA).get(moduleCoordinates);
        return buildTime == null ? 0 : buildTime;
    }

    Long getResourceBuildTime(String moduleCoordinates) {
        Long resourceBuildTime = (Long) compileDataStore.get(FULL_RESOURCE_BUILD_DATA).get(moduleCoordinates);
        return resourceBuildTime == null ? 0 : resourceBuildTime;
    }

    boolean isCompileMust(String moduleCoordinates, Collection<String> dependencies) {
        for (String d : dependencies) {
            if (ARTIFACTS.containsKey(d) && JAR.equals(ARTIFACTS.get(d).getType())) {
                boolean versionEqual = ARTIFACTS.get(d).getVersion().equals(project.getVersion());
                if (versionEqual && getBuildTime(d) == 0) {
                    LOG.warning(ANSI_YELLOW + "[WARNING:]" + "You have module[" + d
                                        + "] not locally compiled even once, please compile your project once daily "
                                        + "from root to have reliable build results."
                                        + ANSI_COLOR_RESET);
                    return true;
                }
            }
        }
        return isMust(this::getBuildTime, moduleCoordinates, dependencies);
    }

    boolean isTestExecutionMandatory() {
        for (String d : ARTIFACTS.keySet()) {
            if (EXECUTE_TESTS_IF_DEPENDS_ON_STORE.contains(d)) {
                return true;
            }
        }
        return false;
    }

    boolean isTestMust(String moduleCoordinates) {
        return getBuildTime(moduleCoordinates) > getResourceBuildTime(moduleCoordinates) || isMust(
                this::getResourceBuildTime, moduleCoordinates, ARTIFACTS.keySet());
    }

    private boolean isMust(Function<String, Long> function, String moduleCoordinates, Collection<String> dependencies) {
        Long time = function.apply(moduleCoordinates);
        if (time == null || time == 0) {
            return true;
        }
        for (String module : dependencies) {
            Long buildTime = function.apply(module);
            if (buildTime >= time) {
                return true;
            }
        }
        return false;
    }

    private static HashMap loadState(File file) {
        try (InputStream is = new FileInputStream(file); ObjectInputStream ois = new ObjectInputStream(is)) {
            return (HashMap) ois.readObject();
        } catch (Exception e) {
            LOG.log(Level.FINE, e.getMessage(), e);
            return new HashMap<>();
        }
    }

    private static boolean swapStates(HashMap repo, HashMap last) {
        long repoTime = repo.get(SHUTDOWN_TIME) == null ? 0 : (Long) repo.get(SHUTDOWN_TIME);
        long lastTime = last.get(SHUTDOWN_TIME) == null ? 0 : (Long) last.get(SHUTDOWN_TIME);
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
        return repoTime < lastTime;
    }


}
