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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import static org.openecomp.sdc.onboarding.Constants.*;

@Mojo(name = "post-test-run-helper", threadSafe = true, defaultPhase = LifecyclePhase.TEST,
        requiresDependencyResolution = ResolutionScope.NONE)
public class PostTestRunHelperMojo extends AbstractMojo {

    @Parameter
    private BuildState buildState;
    @Parameter(defaultValue = "${project.artifact.groupId}:${project.artifact.artifactId}")
    private String moduleCoordinates;
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    @Parameter
    private String excludePackaging;


    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!System.getProperties().containsKey(UNICORN)) {
            return;
        }
        if (project.getPackaging().equals(excludePackaging)) {
            return;
        }

        if (project.getProperties().containsKey(SKIP_TEST_RUN) && !Boolean.valueOf(
                project.getProperties().getProperty(SKIP_TEST_RUN))) {
            if (!System.getProperties().containsKey(SKIP_TESTS)) {
                buildState.saveResourceBuildData(moduleCoordinates);
                buildState.addResourceBuildTime(moduleCoordinates, System.currentTimeMillis());

            }
        }

    }
}
