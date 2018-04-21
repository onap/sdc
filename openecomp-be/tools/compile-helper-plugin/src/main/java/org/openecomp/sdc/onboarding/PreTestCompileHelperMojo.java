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
import org.apache.commons.io.FileUtils;
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
    @Parameter
    private File testSourceLocation;
    private static final String JAVA_EXT = "java";
    @Parameter
    private Long staleThreshold;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (compiledFilesList.exists()
                    && compiledFilesList.lastModified() > System.currentTimeMillis() - staleThreshold) {
            markModuleDirty(testSourceLocation);
        }
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
