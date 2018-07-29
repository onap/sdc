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

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.util.List;

import static org.openecomp.sdc.onboarding.Constants.*;

@Mojo(name = "pre-test-compile-helper", threadSafe = true, defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES,
        requiresDependencyResolution = ResolutionScope.TEST)
public class PreTestCompileHelperMojo extends AbstractMojo {

    @Parameter
    private BuildState buildState;
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    @Parameter(defaultValue = "${project.artifact.groupId}:${project.artifact.artifactId}")
    private String moduleCoordinates;
    @Parameter(defaultValue = "${project.buildPlugins}", readonly = true)
    private List<Plugin> plugins;
    @Parameter
    private String excludePackaging;


    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!System.getProperties().containsKey(UNICORN)) {
            return;
        }
        if (project.getPackaging().equals(excludePackaging)) {
            return;
        }
        if (buildState.isTestExecutionMandatory()) {
            project.getProperties().setProperty(SKIP_TEST_RUN, Boolean.FALSE.toString());
        }
        boolean isTestMust = buildState.isTestMust(moduleCoordinates);
        if (isTestMust) {
            project.getProperties().setProperty(RESOURCES_CHANGED, Boolean.TRUE.toString());
            if (!project.getProperties().containsKey(SKIP_TEST_RUN)) {
                project.getProperties().setProperty(SKIP_TEST_RUN, Boolean.FALSE.toString());
            }
        }
        if (!project.getProperties().containsKey(SKIP_TEST_RUN) || isTestSkippedExplicitly()) {
            project.getProperties().setProperty(SKIP_TEST_RUN, Boolean.TRUE.toString());
        }
        if (System.getProperties().containsKey(JACOCO_SKIP) && Boolean.FALSE.equals(Boolean.valueOf(
                System.getProperties().getProperty(JACOCO_SKIP)))) {
            project.getProperties().setProperty(SKIP_TEST_RUN, Boolean.FALSE.toString());
        }
    }


    private boolean isTestSkippedExplicitly() {
        for (Plugin p : plugins) {
            if ("org.apache.maven.plugins:maven-surefire-plugin".equals(p.getKey())) {
                Xpp3Dom dom = Xpp3Dom.class.cast(p.getConfiguration());
                if (dom.getChild(SKIP_TESTS) != null) {
                    return Boolean.TRUE.equals(Boolean.valueOf(dom.getValue()));
                }
            }
        }
        return false;
    }
}
