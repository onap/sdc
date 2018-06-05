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

import static org.openecomp.sdc.onboarding.Constants.FORK_COUNT;
import static org.openecomp.sdc.onboarding.Constants.FORK_MODE;
import static org.openecomp.sdc.onboarding.Constants.JACOCO;
import static org.openecomp.sdc.onboarding.Constants.JACOCO_BUILD;
import static org.openecomp.sdc.onboarding.Constants.PREFIX;
import static org.openecomp.sdc.onboarding.Constants.SKIP_PMD;
import static org.openecomp.sdc.onboarding.Constants.UNICORN;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "init-helper", threadSafe = true, defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
        requiresDependencyResolution = ResolutionScope.TEST)
public class InitializationHelperMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    @Parameter
    private BuildState buildState;
    @Parameter
    private String excludePackaging;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (PREFIX == UNICORN || JACOCO == UNICORN) {
            System.getProperties().setProperty(UNICORN, Boolean.TRUE.toString());
        }
        if (project.getPackaging().equals(excludePackaging)) {
            return;
        }
        if (Boolean.valueOf(JACOCO_BUILD)) {
            project.getProperties().setProperty(FORK_COUNT, "1");
            project.getProperties().setProperty(FORK_MODE, "once");
        } else {
            project.getProperties().setProperty(FORK_COUNT, "0");
            project.getProperties().setProperty(FORK_MODE, "never");
        }

        project.getProperties().setProperty(SKIP_PMD, Boolean.TRUE.toString());

        if (System.getProperties().containsKey(UNICORN)) {
            buildState.init();
        } else {
            project.getProperties().setProperty("skipMainSourceCompile", Boolean.FALSE.toString());
            project.getProperties().setProperty("skipTestSourceCompile", Boolean.FALSE.toString());
        }


    }


}
