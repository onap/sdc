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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;


@Mojo(name = "post-source-generator-helper", threadSafe = true, defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
        requiresDependencyResolution = ResolutionScope.TEST)
public class PostSourceGeneratorMojo extends PreCompileHelperMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (isCodeGenerator()) {
            super.execute();
        }
    }

    //    @Override
    void deleteAllClasses(File file) {
        if (!file.exists()) {
            return;
        }
        try {
            List<File> list =
                    Files.walk(Paths.get(file.getAbsolutePath())).filter(Files::isRegularFile).map(p -> p.toFile())
                         .collect(Collectors.toList());
            for (File f : list) {
                f.delete();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }
}
