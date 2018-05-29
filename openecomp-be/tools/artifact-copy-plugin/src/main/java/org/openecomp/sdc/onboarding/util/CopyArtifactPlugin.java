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

package org.openecomp.sdc.onboarding.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "copy-helper", threadSafe = true, defaultPhase = LifecyclePhase.CLEAN,
        requiresDependencyResolution = ResolutionScope.NONE)
public class CopyArtifactPlugin extends AbstractMojo {

    @Parameter(defaultValue = "${session}")
    private MavenSession session;
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    @Parameter
    private String groupId;
    @Parameter
    private String artifactId;
    @Parameter
    private String version;
    @Parameter
    private String targetLocation;
    @Parameter
    private String name;
    @Parameter
    private ArtifactHelper artifactHelper;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!project.getProperties().containsKey("resolvedVersion")) {
            return;
        }
        boolean isSnapshot = version.contains("SNAPSHOT");
        List<ArtifactRepository> artRepoList = artifactHelper.getRepositories(isSnapshot);
        String resolvedVersion = project.getProperties().getProperty("resolvedVersion");
        try {
            if (!version.equals(resolvedVersion)) {
                if (copyResolvedArtifact(artRepoList, resolvedVersion) && getLog().isInfoEnabled()) {
                    getLog().info("Data Artifact Copied with " + resolvedVersion);
                }

            }
            File orgFile = new File(
                    session.getLocalRepository().getBasedir() + File.separator + (groupId.replace(".", File.separator))
                            + File.separator + artifactId + File.separator + version);
            if (!orgFile.exists()) {
                return;
            }
            File[] list = orgFile.listFiles(t -> t.getName().equals(artifactId + "-" + version + ".jar"));
            if (list != null && list.length > 0) {
                String directory = session.getLocalRepository().getBasedir() + File.separator + (groupId.replace(".",
                        File.separator)) + File.separator + targetLocation + File.separator + version;
                if (!Paths.get(directory, name).toFile().exists()) {
                    return;
                }
                Files.copy(list[0].toPath(), Paths.get(directory, name), StandardCopyOption.REPLACE_EXISTING);
                copyTargetArtifact(directory, list[0]);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new MojoFailureException(e.getMessage());
        }
    }

    private void copyTargetArtifact(String directory, File source) throws IOException, NoSuchAlgorithmException {
        File[] files = new File(directory).listFiles(
                f -> f.getName().endsWith(".jar") && !f.getName().equals(name) && f.getName().startsWith(
                        name.substring(0, name.lastIndexOf('-'))));
        if (files == null || files.length == 0) {
            return;
        }
        Arrays.sort(files, this::compare);
        File tgtFile = files[files.length - 1];
        Files.copy(source.toPath(), tgtFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        for (String checksumType : Arrays.asList("sha1", "md5")) {
            File potentialFile = new File(tgtFile.getAbsolutePath() + "." + checksumType);
            if (potentialFile.exists()) {
                Files.write(potentialFile.toPath(),
                        artifactHelper.getChecksum(source.getAbsolutePath(), checksumType).getBytes(),
                        StandardOpenOption.CREATE);
            }
        }
    }


    private boolean copyResolvedArtifact(List<ArtifactRepository> list, String resolvedVersion) {
        for (ArtifactRepository repo : list) {
            try {
                writeContents(
                        new URL(repo.getUrl() + (groupId.replace('.', '/')) + '/' + artifactId + '/' + version + '/'
                                        + artifactId + "-" + (version.equals(resolvedVersion) ? version :
                                                                      version.replace("SNAPSHOT", resolvedVersion))
                                        + ".jar"));
                return true;
            } catch (IOException e) {
                getLog().debug(e);
            }
        }
        return false;
    }


    private void writeContents(URL path) throws IOException {
        String directory =
                session.getLocalRepository().getBasedir() + File.separator + (groupId.replace(".", File.separator))
                        + File.separator + artifactId + File.separator + version;
        try (InputStream is = path.openStream()) {
            Files.copy(is, Paths.get(directory, artifactId + "-" + version + ".jar"),
                    StandardCopyOption.REPLACE_EXISTING);
        }

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

}
