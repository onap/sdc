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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "post-compile-helper", threadSafe = true, defaultPhase = LifecyclePhase.PROCESS_TEST_CLASSES,
        requiresDependencyResolution = ResolutionScope.COMPILE)
public class PostCompileHelperMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    @Parameter(defaultValue = "${project.artifact.groupId}:${project.artifact.artifactId}")
    private String moduleCoordinates;


    private static final String JAVA_EXT = ".java";
    private static final String CLASS_EXT = ".class";

    @Parameter
    private Long staleThreshold;

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

    private File[] getCompiledClasses(File compiledFiles) {
        if (!compiledFiles.exists()) {
            return new File[0];
        }
        File[] list = null;
        try {
            list = Files.walk(Paths.get(compiledFiles.getAbsolutePath()))
                        .filter(p -> p.toFile().getAbsolutePath().endsWith(CLASS_EXT)).map(p -> p.toFile())
                        .sorted(this::compare).collect(Collectors.toList()).toArray(new File[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (list == null || list.length == 0) {
            return new File[0];
        }
        return list;
    }

    private int compare(File file1, File file2) {
        if (file1.lastModified() > file2.lastModified()) {
            return 1;
        }
        if (file1.lastModified() < file2.lastModified()) {
            return -1;
        }
        return 0;
    }

    private File[] getStaleCompiledClasses(File[] compiledClasses, File javaSourceLocation) {
        List<File> staleFiles = new ArrayList<>();
        for (File file : compiledClasses) {
            String classLocation = file.getAbsolutePath().replace(
                    project.getBasedir().getAbsolutePath() + File.separator + "target" + File.separator, "");
            String classLocationWithPackageOnly =
                    classLocation.substring(classLocation.indexOf(File.separatorChar) + 1);
            String sourceFilePath = javaSourceLocation.getAbsolutePath() + File.separator + classLocationWithPackageOnly
                                                                                                    .replace(CLASS_EXT,
                                                                                                            JAVA_EXT);
            if (Paths.get(sourceFilePath).toFile().exists()) {
                return staleFiles.toArray(new File[0]);
            } else {
                staleFiles.add(file);
            }
        }
        return staleFiles.toArray(new File[0]);
    }

    private boolean deleteAll(File[] files) {
        for (File file : files) {
            if (!file.delete()) {
                return false;
            }
        }
        return true;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (project.getPackaging().equals(excludePackaging)) {
            return;
        }
        String moduleLocation = project.getBasedir().getAbsolutePath();

        File[] mainClasses = getCompiledClasses(mainCompiledLocation);
        processStaleClassesIfAny(mainClasses, mainSourceLocation, inputSourceFilesList);

        File[] testClasses = getCompiledClasses(testCompiledLocation);
        processStaleClassesIfAny(testClasses, testSourceLocation, inputTestFilesList);

        if (mainClasses.length == 0 && testClasses.length == 0) {
            return;
        }
        buildState.addModuleBuildTime(project.getGroupId() + ":" + project.getArtifactId(),
                mainClasses.length > 0 ? mainClasses[mainClasses.length - 1].lastModified() :
                        testClasses.length > 0 ? testClasses[testClasses.length - 1].lastModified() : 0);
        buildState.saveModuleBuildData(moduleCoordinates);
    }

    private void processStaleClassesIfAny(File[] classes, File sourceLocation, File listFile)
            throws MojoExecutionException {
        if (classes.length > 0) {
            List<File> list = new ArrayList<>(Arrays.asList(classes));
            File[] staleClasses = null;
            boolean allStale = listFile.isFile() && listFile.length() == 0;
            if (allStale) {
                staleClasses = classes;
                listFile.delete();
            } else {
                list.removeIf(f -> f.lastModified() > classes[classes.length - 1].lastModified() - staleThreshold);
                staleClasses = getStaleCompiledClasses(list.toArray(new File[0]), sourceLocation);
            }
            if (!deleteAll(staleClasses)) {
                throw new MojoExecutionException(
                        "****** Please remove 'target' directory manually under path " + project.getBasedir()
                                                                                                .getAbsolutePath());
            }
        }
    }
}
