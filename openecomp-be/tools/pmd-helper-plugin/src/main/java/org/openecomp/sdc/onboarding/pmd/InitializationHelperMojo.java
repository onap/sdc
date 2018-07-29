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

package org.openecomp.sdc.onboarding.pmd;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import static org.openecomp.sdc.onboarding.pmd.PMDHelperUtils.*;

@Mojo(name = "init-pmd-helper", threadSafe = true, defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
        requiresDependencyResolution = ResolutionScope.NONE)
public class InitializationHelperMojo extends AbstractMojo {

    private static final String SKIP_PMD = "skipPMD";

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    @Parameter(defaultValue = "${project.artifact.groupId}:${project.artifact.artifactId}")
    private String moduleCoordinates;
    @Parameter
    private File pmdTargetLocation;
    @Parameter
    private File pmdReportFile;
    @Parameter
    private String persistingModuleCoordinates;
    @Parameter
    private File pmdStateFile;
    @Parameter
    private String pmdCurrentStateFilePath;
    @Parameter
    private String excludePackaging;

    static {
        PMDState.setHistoricState(readCurrentPMDState("pmd.dat"));
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (project.getPackaging().equals(excludePackaging)) {
            return;
        }
        if (moduleCoordinates.equals(persistingModuleCoordinates)) {
            pmdStateFile.getParentFile().mkdirs();
            try (OutputStream os = new FileOutputStream(pmdStateFile);
                 ObjectOutputStream oos = new ObjectOutputStream(os)) {
                File f = getStateFile(pmdCurrentStateFilePath.substring(0, pmdCurrentStateFilePath.indexOf('/')),
                        project, pmdCurrentStateFilePath);
                Map<String, List<Violation>> data = readCurrentPMDState(f);
                if (PMDState.getHistoricState() != null) {
                    PMDState.getHistoricState().putAll(data);
                    oos.writeObject(PMDState.getHistoricState());
                } else {
                    oos.writeObject(data);
                }
                if (Paths.get(f.getParentFile().getAbsolutePath(), "compileState.dat").toFile().exists()) {
                    Files.copy(Paths.get(f.getParentFile().getAbsolutePath(), "compileState.dat"),
                            Paths.get(pmdStateFile.getParentFile().getAbsolutePath(), "compile.dat"),
                            StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return;
        }
        if (project.getProperties().containsKey(SKIP_PMD) && Boolean.TRUE.equals(Boolean.valueOf(
                project.getProperties().getProperty(SKIP_PMD)))) {
            return;
        }
        pmdTargetLocation.getParentFile().mkdirs();
        try (InputStream is = this.getClass().getResourceAsStream("/pmd-empty.xml");
             OutputStream os = new FileOutputStream(pmdTargetLocation)) {
            String text = readInputStream(is);
            os.write(text.getBytes());
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
    }

}
