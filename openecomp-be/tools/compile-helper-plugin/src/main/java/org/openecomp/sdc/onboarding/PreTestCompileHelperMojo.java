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
import java.io.IOException;
import java.io.UncheckedIOException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "pre-test-compile-helper", threadSafe = true, defaultPhase = LifecyclePhase.GENERATE_TEST_RESOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE)
public class PreTestCompileHelperMojo extends AbstractMojo {

    @Parameter
    private File compiledFilesList;
    private static final String JAVA_EXT = "java";
    @Parameter
    private Long staleThreshold;
    @Parameter
    private File inputTestFilesList;
    @Parameter
    private BuildState buildState;


    public void execute() throws MojoExecutionException, MojoFailureException {
        if (compiledFilesList.exists()
                    && compiledFilesList.lastModified() > System.currentTimeMillis() - staleThreshold) {
            try {
                buildState.markModuleDirty(inputTestFilesList);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
