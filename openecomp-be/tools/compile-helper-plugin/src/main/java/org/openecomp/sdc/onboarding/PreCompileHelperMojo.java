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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
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

    private static final String JAVA_EXT = "java";

    @Parameter
    private String excludePackaging;
    @Parameter
    private List<String> excludeDependencies;
    @Parameter
    private File mainSourceLocation;
    @Parameter
    private File testSourceLocation;
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

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (project.getPackaging().equals(excludePackaging)) {
            return;
        }

        Map<String, String> moduleBuildData = getCurrentModuleBuildData();
        Map<String, String> lastTimeModuleBuildData = buildState.readModuleBuildData();

        boolean buildDataSame = lastTimeModuleBuildData.equals(moduleBuildData);

        if (buildState.getBuildTime(moduleCoordinates) == 0 || !buildDataSame || buildState.isCompileMust(
                moduleCoordinates, moduleBuildData.keySet())) {
            markModuleDirty(mainSourceLocation);
            markModuleDirty(testSourceLocation);
        }

        if (!buildDataSame) {
            buildState.addModuleBuildData(moduleCoordinates, moduleBuildData);
        }
        if (inputSourceFilesList.isFile() && inputSourceFilesList.length() == 0) {
            inputSourceFilesList.delete();
        }
        if (inputTestFilesList.isFile() && inputTestFilesList.length() == 0) {
            inputTestFilesList.delete();
        }
    }

    private Map<String, String> getCurrentModuleBuildData() {
        Map<String, String> moduleBuildData = new HashMap<>();
        if (project.getDependencies() == null || project.getDependencies().isEmpty()) {
            return moduleBuildData;
        }
        for (Dependency dependency : project.getDependencies()) {
            if (excludeDependencies.contains(dependency.getScope())) {
                continue;
            }
            moduleBuildData.put(dependency.getGroupId() + ":" + dependency.getArtifactId(), dependency.getVersion());
        }
        return moduleBuildData;
    }

    private void markModuleDirty(File file) {
        if (file.exists()) {
            File[] files = FileUtils.listFiles(file, new String[] {JAVA_EXT}, true).toArray(new File[0]);
            if (files != null && files.length > 0) {
                files[0].setLastModified(System.currentTimeMillis());
            }
        }
    }
}
