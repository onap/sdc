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

import static org.openecomp.sdc.onboarding.BuildHelper.getArtifactPathInLocalRepo;
import static org.openecomp.sdc.onboarding.BuildHelper.getChecksum;
import static org.openecomp.sdc.onboarding.BuildHelper.getSourceChecksum;
import static org.openecomp.sdc.onboarding.BuildHelper.readState;
import static org.openecomp.sdc.onboarding.Constants.ANY_EXT;
import static org.openecomp.sdc.onboarding.Constants.CHECKSUM;
import static org.openecomp.sdc.onboarding.Constants.COLON;
import static org.openecomp.sdc.onboarding.Constants.DOT;
import static org.openecomp.sdc.onboarding.Constants.EMPTY_JAR;
import static org.openecomp.sdc.onboarding.Constants.GENERATED_SOURCE_CHECKSUM;
import static org.openecomp.sdc.onboarding.Constants.INSTRUMENT_ONLY;
import static org.openecomp.sdc.onboarding.Constants.INSTRUMENT_WITH_TEST_ONLY;
import static org.openecomp.sdc.onboarding.Constants.JAR;
import static org.openecomp.sdc.onboarding.Constants.JAVA_EXT;
import static org.openecomp.sdc.onboarding.Constants.MAIN;
import static org.openecomp.sdc.onboarding.Constants.MAIN_CHECKSUM;
import static org.openecomp.sdc.onboarding.Constants.MAIN_SOURCE_CHECKSUM;
import static org.openecomp.sdc.onboarding.Constants.PREFIX;
import static org.openecomp.sdc.onboarding.Constants.RESOURCES_CHANGED;
import static org.openecomp.sdc.onboarding.Constants.RESOURCE_CHECKSUM;
import static org.openecomp.sdc.onboarding.Constants.RESOURCE_ONLY;
import static org.openecomp.sdc.onboarding.Constants.RESOURCE_WITH_TEST_ONLY;
import static org.openecomp.sdc.onboarding.Constants.SHA1;
import static org.openecomp.sdc.onboarding.Constants.SKIP_INSTALL;
import static org.openecomp.sdc.onboarding.Constants.SKIP_MAIN_SOURCE_COMPILE;
import static org.openecomp.sdc.onboarding.Constants.SKIP_PMD;
import static org.openecomp.sdc.onboarding.Constants.SKIP_RESOURCE_COLLECTION;
import static org.openecomp.sdc.onboarding.Constants.SKIP_TEST_RUN;
import static org.openecomp.sdc.onboarding.Constants.SKIP_TEST_SOURCE_COMPILE;
import static org.openecomp.sdc.onboarding.Constants.TEST;
import static org.openecomp.sdc.onboarding.Constants.TEST_CHECKSUM;
import static org.openecomp.sdc.onboarding.Constants.TEST_ONLY;
import static org.openecomp.sdc.onboarding.Constants.TEST_RESOURCE_CHECKSUM;
import static org.openecomp.sdc.onboarding.Constants.TEST_RESOURCE_ONLY;
import static org.openecomp.sdc.onboarding.Constants.TEST_SOURCE_CHECKSUM;
import static org.openecomp.sdc.onboarding.Constants.UNICORN;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;


@Mojo(name = "pre-compile-helper", threadSafe = true, defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
        requiresDependencyResolution = ResolutionScope.TEST)
public class PreCompileHelperMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}")
    private MavenProject project;
    @Parameter(defaultValue = "${project.artifact.groupId}:${project.artifact.artifactId}")
    private String moduleCoordinates;
    @Parameter(defaultValue = "${session}")
    private MavenSession session;
    @Parameter
    private String excludePackaging;
    @Parameter
    private List<String> excludeDependencies;
    @Parameter
    private BuildState buildState;
    @Parameter
    private File mainSourceLocation;
    @Parameter
    private File testSourceLocation;
    @Parameter
    private File generatedSourceLocation;
    @Component
    private MavenPluginManager pluginManager;
    @Parameter
    private File mainResourceLocation;
    @Parameter
    private File testResourceLocation;
    private Map<String, Object> resourceBuildData;

    private static Map<String, String> checksumMap;
    private long mainChecksum = 0;
    private long testChecksum = 0;
    private long resourceChecksum = 0;
    private long testResourceChecksum = 0;
    Optional<String> artifactPath;

    static {
        checksumMap = readCurrentPMDState("pmd.dat");
    }

    public void execute() throws MojoExecutionException, MojoFailureException {


        if (project.getPackaging().equals(excludePackaging)) {
            return;
        }
        init();
        processPMDCheck();
        project.getProperties().setProperty(EMPTY_JAR, "");
        if (!System.getProperties().containsKey(UNICORN)) {
            return;
        }
        resourceChecksum = getChecksum(mainResourceLocation, ANY_EXT);
        testResourceChecksum = getChecksum(testResourceLocation, ANY_EXT);
        project.getProperties().setProperty(RESOURCE_CHECKSUM, String.valueOf(resourceChecksum));
        project.getProperties().setProperty(TEST_RESOURCE_CHECKSUM, String.valueOf(testResourceChecksum));
        byte[] sourceChecksum = calculateChecksum(mainChecksum, resourceChecksum).getBytes();
        boolean instrumented = isCurrentModuleInstrumented();
        artifactPath = getArtifactPathInLocalRepo(session.getLocalRepository().getUrl(), project, sourceChecksum);

        boolean isFirstBuild = buildState.getBuildTime(moduleCoordinates) == 0 || !artifactPath.isPresent();

        Map<String, Object> moduleBuildData = getCurrentModuleBuildData();
        Map<String, Object> lastTimeModuleBuildData = buildState.readModuleBuildData();
        resourceBuildData = getCurrentResourceBuildData();
        Map<String, Object> lastTimeResourceBuildData = buildState.readResourceBuildData();
        generateSyncAlert(lastTimeResourceBuildData != null && (
                !resourceBuildData.get(MAIN).equals(lastTimeResourceBuildData.get(MAIN)) || !resourceBuildData.get(TEST)
                                                                                                              .equals(lastTimeResourceBuildData
                                                                                                                              .get(TEST)
                                                                                                                              .toString())));
        boolean buildDataSameWithPreviousBuild =
                isBuildDataSameWithPreviousBuild(lastTimeModuleBuildData, moduleBuildData);
        boolean resourceMainBuildDataSameWithPreviousBuild =
                isResourceMainBuildDataSameWithPreviousBuild(lastTimeResourceBuildData);

        boolean mainToBeCompiled = isCompileNeeded(HashMap.class.cast(moduleBuildData.get(MAIN)).keySet(), isFirstBuild,
                buildDataSameWithPreviousBuild);

        boolean resourceDataSame = resourceBuildData.equals(lastTimeResourceBuildData);

        boolean testToBeCompiled =
                lastTimeModuleBuildData == null || !moduleBuildData.get(TEST).equals(lastTimeModuleBuildData.get(TEST));
        setMainBuildAttribute(mainToBeCompiled, testToBeCompiled);
        generateSignature(sourceChecksum);
        setTestBuild(resourceDataSame, resourceMainBuildDataSameWithPreviousBuild, testToBeCompiled, mainToBeCompiled);
        setInstrumentedBuild(testToBeCompiled, mainToBeCompiled, instrumented);

        if (!moduleBuildData.equals(lastTimeModuleBuildData) || isFirstBuild) {
            buildState.addModuleBuildData(moduleCoordinates, moduleBuildData);
        }
        setResourceBuild(resourceMainBuildDataSameWithPreviousBuild, mainToBeCompiled, testToBeCompiled);
        setJarFlags(mainToBeCompiled, instrumented, !resourceMainBuildDataSameWithPreviousBuild);
        setInstallFlags(mainToBeCompiled, instrumented, project.getPackaging(),
                !resourceMainBuildDataSameWithPreviousBuild);

        setArtifactPath(mainToBeCompiled, instrumented, JAR.equals(project.getPackaging()),
                resourceMainBuildDataSameWithPreviousBuild);
    }

    private void generateSignature(byte[] sourceChecksum) {
        try {
            Paths.get(project.getBuild().getOutputDirectory()).toFile().mkdirs();
            Files.write(Paths.get(project.getBuild().getOutputDirectory(), UNICORN + DOT + CHECKSUM), sourceChecksum,
                    StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String calculateChecksum(long mainChecksum, long resourceChecksum) throws MojoExecutionException {
        try {
            return getSourceChecksum(mainChecksum + COLON + resourceChecksum, SHA1);
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private boolean isResourceMainBuildDataSameWithPreviousBuild(Map<String, Object> lastTimeResourceBuildData) {
        return lastTimeResourceBuildData != null && (lastTimeResourceBuildData.get(MAIN) != null && resourceBuildData
                                                                                                            .get(MAIN)
                                                                                                            .equals(lastTimeResourceBuildData
                                                                                                                            .get(MAIN)));
    }

    private boolean isBuildDataSameWithPreviousBuild(Map<String, Object> lastTimeModuleBuildData,
            Map<String, Object> moduleBuildData) {
        return lastTimeModuleBuildData != null && (lastTimeModuleBuildData.get(MAIN) != null && moduleBuildData
                                                                                                        .get(MAIN)
                                                                                                        .equals(lastTimeModuleBuildData
                                                                                                                        .get(MAIN)));
    }

    private void setInstrumentedBuild(boolean testToBeCompiled, boolean mainToBeCompiled, boolean instrumented) {
        if (!testToBeCompiled && !mainToBeCompiled && instrumented) {
            project.getProperties().setProperty(INSTRUMENT_ONLY, Boolean.TRUE.toString());
            project.getProperties().remove(SKIP_MAIN_SOURCE_COMPILE);
            project.getProperties().remove(SKIP_TEST_SOURCE_COMPILE);
        }
        if (testToBeCompiled && !mainToBeCompiled && instrumented) {
            project.getProperties().setProperty(INSTRUMENT_WITH_TEST_ONLY, Boolean.TRUE.toString());
            project.getProperties().remove(SKIP_MAIN_SOURCE_COMPILE);
        }
        if (instrumented) {
            buildState.markTestsMandatoryModule(moduleCoordinates);
            project.getProperties().setProperty(SKIP_TEST_RUN, Boolean.FALSE.toString());
        }
    }

    private void setArtifactPath(boolean mainToBeCompiled, boolean instrumented, boolean isJar,
            boolean resourceDataSame) {
        if (!mainToBeCompiled && !instrumented && isJar && resourceDataSame) {
            project.getProperties().setProperty("artifactPathToCopy", artifactPath.orElse(null));
        }
    }

    private void setResourceBuild(boolean resourceMainBuildDataSameWithPreviousBuild, boolean mainToBeCompiled,
            boolean testToBeCompiled) {
        if (resourceMainBuildDataSameWithPreviousBuild) {
            project.getProperties().setProperty(SKIP_RESOURCE_COLLECTION, Boolean.TRUE.toString());
        } else {
            project.getProperties().setProperty(RESOURCES_CHANGED, Boolean.TRUE.toString());
        }
        if (!resourceMainBuildDataSameWithPreviousBuild && !mainToBeCompiled) {
            project.getProperties().remove(SKIP_MAIN_SOURCE_COMPILE);
            if (!testToBeCompiled) {
                project.getProperties().remove(SKIP_TEST_SOURCE_COMPILE);
                project.getProperties().setProperty(RESOURCE_ONLY, Boolean.TRUE.toString());
            } else {
                project.getProperties().setProperty(RESOURCE_WITH_TEST_ONLY, Boolean.TRUE.toString());
            }
        }
    }

    private void setTestBuild(boolean resourceDataSame, boolean resourceMainBuildDataSameWithPreviousBuild,
            boolean testToBeCompiled, boolean mainToBeCompiled) {
        if (!resourceDataSame) {
            buildState.addResourceBuildData(moduleCoordinates, resourceBuildData);
            project.getProperties().setProperty(SKIP_TEST_RUN, Boolean.FALSE.toString());
            if (resourceMainBuildDataSameWithPreviousBuild && !testToBeCompiled && !mainToBeCompiled) {
                project.getProperties().setProperty(TEST_RESOURCE_ONLY, Boolean.TRUE.toString());
                project.getProperties().remove(SKIP_MAIN_SOURCE_COMPILE);
                project.getProperties().remove(SKIP_TEST_SOURCE_COMPILE);
            }
        }
    }

    private void setMainBuildAttribute(boolean mainToBeCompiled, boolean testToBeCompiled) {
        if (!mainToBeCompiled) {
            project.getProperties().setProperty(SKIP_MAIN_SOURCE_COMPILE, Boolean.TRUE.toString());
        }
        if (testToBeCompiled && !mainToBeCompiled) {
            project.getProperties().setProperty(TEST_ONLY, Boolean.TRUE.toString());
            project.getProperties().remove(SKIP_MAIN_SOURCE_COMPILE);
        }

        if (mainToBeCompiled || testToBeCompiled) {
            project.getProperties().setProperty(SKIP_TEST_RUN, Boolean.FALSE.toString());
        } else {
            project.getProperties().setProperty(SKIP_TEST_SOURCE_COMPILE, Boolean.TRUE.toString());
        }
    }

    private void setJarFlags(boolean compile, boolean instrumented, boolean resourceChanged) {
        if (compile || instrumented || resourceChanged || PREFIX == UNICORN) {
            project.getProperties().setProperty(EMPTY_JAR, "");
        } else {
            project.getProperties().setProperty(EMPTY_JAR, "**/*");
            project.getProperties().setProperty("mvnDsc", "false");
        }
    }

    private void setInstallFlags(boolean compile, boolean instrumented, String packaging, boolean resourceChanged) {
        if (!compile && !instrumented && !resourceChanged && JAR.equals(packaging)) {
            project.getProperties().setProperty(SKIP_INSTALL, Boolean.TRUE.toString());
        }
    }

    private boolean isCompileNeeded(Collection<String> dependencyCoordinates, boolean isFirstBuild,
            boolean buildDataSame) {
        return isFirstBuild || !buildDataSame || buildState.isCompileMust(moduleCoordinates, dependencyCoordinates);
    }

    private boolean isCurrentModuleInstrumented() {
        try {
            return scanModuleFor(LifecyclePhase.PROCESS_CLASSES.id(), LifecyclePhase.PROCESS_TEST_CLASSES.id(),
                    LifecyclePhase.COMPILE.id(), LifecyclePhase.TEST_COMPILE.id());
        } catch (Exception e) {
            getLog().debug(e);
            return true;
        }
    }

    boolean isCodeGenerator() {
        try {
            return scanModuleFor(LifecyclePhase.GENERATE_RESOURCES.id(), LifecyclePhase.GENERATE_SOURCES.id(),
                    LifecyclePhase.GENERATE_TEST_RESOURCES.id(), LifecyclePhase.GENERATE_TEST_SOURCES.id());
        } catch (Exception e) {
            getLog().debug(e);
            return true;
        }
    }

    private Map<String, Object> getCurrentModuleBuildData() throws MojoExecutionException {
        Map<String, Object> moduleBuildData = new HashMap<>();
        moduleBuildData.put(MAIN, new HashMap<String, String>());
        moduleBuildData.put(TEST, new HashMap<String, String>());
        HashMap.class.cast(moduleBuildData.get(MAIN))
                     .put(MAIN_SOURCE_CHECKSUM, project.getProperties().getProperty(MAIN_CHECKSUM));
        HashMap.class.cast(moduleBuildData.get(TEST))
                     .put(TEST_SOURCE_CHECKSUM, project.getProperties().getProperty(TEST_CHECKSUM));
        if (isCodeGenerator()) {
            HashMap.class.cast(moduleBuildData.get(MAIN))
                         .put(GENERATED_SOURCE_CHECKSUM, getChecksum(generatedSourceLocation, JAVA_EXT));
        }
        if (project.getArtifacts() == null || project.getArtifacts().isEmpty()) {
            return moduleBuildData;
        }
        for (Artifact dependency : project.getArtifacts()) {
            if (excludeDependencies.contains(dependency.getScope())) {
                HashMap.class.cast(moduleBuildData.get(TEST))
                             .put(dependency.getGroupId() + COLON + dependency.getArtifactId(),
                                     dependency.getVersion());
                continue;
            }
            HashMap.class.cast(moduleBuildData.get(MAIN))
                         .put(dependency.getGroupId() + COLON + dependency.getArtifactId(), dependency.getVersion());
        }
        return moduleBuildData;
    }

    private static Map<String, String> readCurrentPMDState(String fileName) {
        Optional<HashMap> val = readState(fileName, HashMap.class);
        return val.orElseGet(HashMap::new);
    }

    private boolean isPMDMandatory(Set<Artifact> dependencies) {
        for (Artifact artifact : dependencies) {
            if (BuildState.isPMDRun(artifact.getGroupId() + COLON + artifact.getArtifactId())) {
                return true;
            }
        }
        return false;
    }

    private boolean scanModuleFor(String... types)
            throws InvalidPluginDescriptorException, PluginResolutionException, MojoNotFoundException,
                           PluginDescriptorParsingException {
        for (Plugin plugin : project.getBuildPlugins()) {
            if (!("org.apache.maven.plugins".equals(plugin.getGroupId()) && plugin.getArtifactId().startsWith("maven"))
                        && !plugin.getGroupId().startsWith("org.openecomp.sdc")) {
                boolean success = scanPlugin(plugin, types);
                if (success) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean scanPlugin(Plugin plugin, String... types)
            throws InvalidPluginDescriptorException, PluginDescriptorParsingException, MojoNotFoundException,
                           PluginResolutionException {
        for (PluginExecution pluginExecution : plugin.getExecutions()) {
            if (pluginExecution.getPhase() != null) {
                boolean phaseAvailable = Arrays.asList(types).contains(pluginExecution.getPhase());
                if (phaseAvailable) {
                    return true;
                }
            }
            for (String goal : pluginExecution.getGoals()) {
                MojoDescriptor md = pluginManager.getMojoDescriptor(plugin, goal, project.getRemotePluginRepositories(),
                        session.getRepositorySession());
                if (Arrays.asList(types).contains(md.getPhase())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<String, Object> getCurrentResourceBuildData() {
        HashMap<String, Object> resourceBuildStateData = new HashMap<>();
        resourceBuildStateData.put(MAIN, project.getProperties().getProperty(RESOURCE_CHECKSUM));
        resourceBuildStateData.put(TEST, project.getProperties().getProperty(TEST_RESOURCE_CHECKSUM));
        resourceBuildStateData.put("dependency", getDependencies().hashCode());
        return resourceBuildStateData;
    }

    private Map<String, String> getDependencies() {
        Map<String, String> dependencies = new HashMap<>();
        for (Artifact d : project.getArtifacts()) {
            dependencies.put(d.getGroupId() + COLON + d.getArtifactId(), d.getVersion());
        }
        return dependencies;
    }

    private void init() {
        if (mainSourceLocation == null) {
            mainSourceLocation = Paths.get(project.getBuild().getSourceDirectory()).toFile();
        }
        if (testSourceLocation == null) {
            testSourceLocation = Paths.get(project.getBuild().getTestSourceDirectory()).toFile();
        }
        if (mainResourceLocation == null) {
            mainResourceLocation = Paths.get(project.getBuild().getResources().get(0).getDirectory()).toFile();
        }
        if (testResourceLocation == null) {
            testResourceLocation = Paths.get(project.getBuild().getTestResources().get(0).getDirectory()).toFile();
        }
    }

    private void processPMDCheck() {
        mainChecksum = getChecksum(mainSourceLocation, JAVA_EXT);
        testChecksum = getChecksum(testSourceLocation, JAVA_EXT);
        project.getProperties().setProperty(MAIN_CHECKSUM, String.valueOf(mainChecksum));
        project.getProperties().setProperty(TEST_CHECKSUM, String.valueOf(testChecksum));
        String checksum = mainChecksum + COLON + testChecksum;
        if (!checksum.equals(checksumMap.get(moduleCoordinates)) || isPMDMandatory(project.getArtifacts())) {
            project.getProperties().setProperty(SKIP_PMD, Boolean.FALSE.toString());
            BuildState.recordPMDRun(moduleCoordinates);
            generateSyncAlert(!checksum.equals(checksumMap.get(moduleCoordinates)));
        }
    }

    private void generateSyncAlert(boolean required) {
        if (required) {
            getLog().warn(
                    "\u001B[33m\u001B[1m UNICORN Alert!!! Source code in version control system for this module is different than your local one. \u001B[0m");
        }
    }
}
