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

import static org.openecomp.sdc.onboarding.Constants.JACOCO_SKIP;
import static org.openecomp.sdc.onboarding.Constants.SKIP_TEST_RUN;
import static org.openecomp.sdc.onboarding.Constants.RESOURCES_CHANGED;
import static org.openecomp.sdc.onboarding.Constants.UNICORN;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Collectors;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "pre-test-compile-helper", threadSafe = true, defaultPhase = LifecyclePhase.GENERATE_TEST_RESOURCES,
        requiresDependencyResolution = ResolutionScope.TEST)
public class PreTestCompileHelperMojo extends AbstractMojo {

    @Parameter
    private File compiledFilesList;
    @Parameter
    private Long staleThreshold;
    @Parameter
    private File inputTestFilesList;
    @Parameter
    private BuildState buildState;
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    @Parameter(defaultValue = "${project.artifact.groupId}:${project.artifact.artifactId}")
    private String moduleCoordinates;
    @Parameter
    private String excludePackaging;


    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!System.getProperties().containsKey(UNICORN)) {
            return;
        }
        if (project.getPackaging().equals(excludePackaging)) {
            return;
        }
        if (compiledFilesList.exists()
                    && compiledFilesList.lastModified() > System.currentTimeMillis() - staleThreshold) {
            try {
                buildState.markModuleDirty(inputTestFilesList);
                project.getProperties().setProperty(SKIP_TEST_RUN, Boolean.FALSE.toString());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        boolean isTestMust = buildState.isTestMust(moduleCoordinates,
                project.getDependencies().stream().map(d -> d.getGroupId() + ":" + d.getArtifactId())
                       .collect(Collectors.toList()));
        if (isTestMust) {
            project.getProperties().setProperty(RESOURCES_CHANGED, Boolean.TRUE.toString());
            if (!project.getProperties().containsKey(SKIP_TEST_RUN)) {
                project.getProperties().setProperty(SKIP_TEST_RUN, Boolean.FALSE.toString());
            }
        }
        if (!project.getProperties().containsKey(SKIP_TEST_RUN)) {
            project.getProperties().setProperty(SKIP_TEST_RUN, Boolean.TRUE.toString());
        }
        if (System.getProperties().containsKey(JACOCO_SKIP) && Boolean.FALSE.equals(Boolean.valueOf(
                System.getProperties().getProperty(JACOCO_SKIP)))) {
            project.getProperties().setProperty(SKIP_TEST_RUN, Boolean.FALSE.toString());
        }
    }
}
