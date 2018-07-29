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
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;

import static org.openecomp.sdc.onboarding.Constants.*;

@Mojo(name = "post-compile-helper", threadSafe = true, defaultPhase = LifecyclePhase.TEST_COMPILE,
        requiresDependencyResolution = ResolutionScope.TEST)
public class PostCompileHelperMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    @Parameter(defaultValue = "${project.artifact.groupId}:${project.artifact.artifactId}")
    private String moduleCoordinates;
    @Parameter
    private String excludePackaging;
    @Parameter
    private BuildState buildState;
    @Parameter
    private File mainResourceLocation;
    @Parameter
    private File testResourceLocation;


    public void execute() throws MojoExecutionException {
        if (!System.getProperties().containsKey(UNICORN)) {
            return;
        }
        if (project.getPackaging().equals(excludePackaging)) {
            return;
        }
        if (project.getProperties().containsKey(TEST_ONLY)) {
            project.getProperties().setProperty(SKIP_MAIN_SOURCE_COMPILE, Boolean.TRUE.toString());
            project.getProperties().remove(TEST_ONLY);
        }
        postProcessInstrumentedModules();

        if (project.getProperties().containsKey(RESOURCE_WITH_TEST_ONLY)) {
            project.getProperties().setProperty(SKIP_MAIN_SOURCE_COMPILE, Boolean.TRUE.toString());
            project.getProperties().remove(RESOURCE_WITH_TEST_ONLY);
        }
        if (project.getProperties().containsKey(RESOURCE_ONLY)) {
            project.getProperties().setProperty(SKIP_MAIN_SOURCE_COMPILE, Boolean.TRUE.toString());
            project.getProperties().setProperty(SKIP_TEST_SOURCE_COMPILE, Boolean.TRUE.toString());
            project.getProperties().remove(RESOURCE_ONLY);
        }
        if (project.getProperties().containsKey(TEST_RESOURCE_ONLY)) {
            project.getProperties().setProperty(SKIP_MAIN_SOURCE_COMPILE, Boolean.TRUE.toString());
            project.getProperties().setProperty(SKIP_TEST_SOURCE_COMPILE, Boolean.TRUE.toString());
            project.getProperties().remove(TEST_RESOURCE_ONLY);
        }
        if (!project.getProperties().containsKey(SKIP_MAIN_SOURCE_COMPILE)) {
            buildState.addModuleBuildTime(moduleCoordinates, System.currentTimeMillis());
            project.getProperties().setProperty(SKIP_PMD, Boolean.FALSE.toString());
        }
        if (!project.getProperties().containsKey(SKIP_TEST_SOURCE_COMPILE)) {
            project.getProperties().setProperty(SKIP_PMD, Boolean.FALSE.toString());
        }
        buildState.saveModuleBuildData(moduleCoordinates);
    }

    private void postProcessInstrumentedModules() {
        if (project.getProperties().containsKey(INSTRUMENT_ONLY)) {
            project.getProperties().setProperty(SKIP_MAIN_SOURCE_COMPILE, Boolean.TRUE.toString());
            project.getProperties().setProperty(SKIP_TEST_SOURCE_COMPILE, Boolean.TRUE.toString());
            project.getProperties().remove(INSTRUMENT_ONLY);
        }
        if (project.getProperties().containsKey(INSTRUMENT_WITH_TEST_ONLY)) {
            project.getProperties().setProperty(SKIP_MAIN_SOURCE_COMPILE, Boolean.TRUE.toString());
            project.getProperties().remove(INSTRUMENT_WITH_TEST_ONLY);
        }
    }
}
