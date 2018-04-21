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

import static org.openecomp.sdc.onboarding.Constants.MAIN;
import static org.openecomp.sdc.onboarding.Constants.SKIP_TEST_RUN;
import static org.openecomp.sdc.onboarding.Constants.TEST;
import static org.openecomp.sdc.onboarding.Constants.UNICORN;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "pre-compile-helper", threadSafe = true, defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE)
public class PreCompileHelperMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    @Parameter(defaultValue = "${project.artifact.groupId}:${project.artifact.artifactId}")
    private String moduleCoordinates;
    @Parameter
    private String excludePackaging;
    @Parameter
    private List<String> excludeDependencies;
    @Parameter
    private File mainCompiledLocation;
    @Parameter
    private File testCompiledLocation;
    @Parameter
    private File inputSourceFilesList;
    @Parameter
    private File inputTestFilesList;
    @Parameter
    private BuildState buildState;

    public void execute() throws MojoExecutionException {
        if (!System.getProperties().containsKey(UNICORN)) {
            return;
        }
        if (project.getPackaging().equals(excludePackaging)) {
            return;
        }

        Map<String, Object> moduleBuildData = getCurrentModuleBuildData();
        Map<String, Object> lastTimeModuleBuildData = buildState.readModuleBuildData();

        boolean buildDataSameWithPreviousBuild = lastTimeModuleBuildData.get(MAIN) != null && moduleBuildData.get(MAIN)
                                                                                                             .equals(lastTimeModuleBuildData
                                                                                                                             .get(MAIN));
        boolean isFirstBuild = buildState.getBuildTime(moduleCoordinates) == 0;

        if (isCompileNeeded(HashMap.class.cast(moduleBuildData.get(MAIN)).keySet(), isFirstBuild,
                buildDataSameWithPreviousBuild)) {
            try {
                buildState.markModuleDirty(inputSourceFilesList);
                buildState.markModuleDirty(inputTestFilesList);
                project.getProperties().setProperty(SKIP_TEST_RUN, "false");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        if (!moduleBuildData.get(TEST).equals(lastTimeModuleBuildData.get(TEST))) {
            try {
                buildState.markModuleDirty(inputTestFilesList);
                project.getProperties().setProperty(SKIP_TEST_RUN, "false");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        if (!moduleBuildData.equals(lastTimeModuleBuildData)) {
            buildState.addModuleBuildData(moduleCoordinates, moduleBuildData);
        }

        if (inputSourceFilesList.isFile() && inputSourceFilesList.length() == 0) {
            if (!inputSourceFilesList.delete()) {
                throw new MojoExecutionException(
                        "****** Please remove 'target' directory manually under path " + project.getBasedir()
                                                                                                .getAbsolutePath());
            }
        }
        if (inputTestFilesList.isFile() && inputTestFilesList.length() == 0) {
            if (!inputTestFilesList.delete()) {
                throw new MojoExecutionException(
                        "****** Please remove 'target' directory manually under path " + project.getBasedir()
                                                                                                .getAbsolutePath());
            }
        }
    }

    private boolean isCompileNeeded(Collection<String> dependencyCoordinates, boolean isFirstBuild,
            boolean buildDataSame) {
        return isFirstBuild || !buildDataSame || buildState.isCompileMust(moduleCoordinates, dependencyCoordinates);
    }

    private Map<String, Object> getCurrentModuleBuildData() {
        Map<String, Object> moduleBuildData = new HashMap<>();
        moduleBuildData.put(MAIN, new HashMap<String, String>());
        moduleBuildData.put(TEST, new HashMap<String, String>());
        if (project.getDependencies() == null || project.getDependencies().isEmpty()) {
            return moduleBuildData;
        }
        for (Dependency dependency : project.getDependencies()) {
            if (excludeDependencies.contains(dependency.getScope())) {
                HashMap.class.cast(moduleBuildData.get(TEST))
                             .put(dependency.getGroupId() + ":" + dependency.getArtifactId(), dependency.getVersion());
                continue;
            }
            HashMap.class.cast(moduleBuildData.get(MAIN))
                         .put(dependency.getGroupId() + ":" + dependency.getArtifactId(), dependency.getVersion());
        }
        return moduleBuildData;
    }
}
