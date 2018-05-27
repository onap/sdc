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
        String resolvedVersion =
                project.getProperties().getProperty("resolvedVersion");//getResolvedVersion(artRepoList);
        try {
            if (!version.equals(resolvedVersion)) {
                copyResolvedArtifact(artRepoList, resolvedVersion);

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
                File[] files = new File(directory).listFiles(
                        f -> f.getName().endsWith(".jar") && !f.getName().equals(name) && f.getName().startsWith(
                                name.substring(0, name.lastIndexOf('-'))));
                if (files == null || files.length == 0) {
                    return;
                }
                Arrays.sort(files, this::compare);
                File tgtFile = files[files.length - 1];
                Files.copy(list[0].toPath(), tgtFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                for (String checksumType : Arrays.asList("sha1", "md5")) {
                    File potentialFile = new File(tgtFile.getAbsolutePath() + "." + checksumType);
                    if (potentialFile.exists()) {
                        Files.write(potentialFile.toPath(),
                                artifactHelper.getChecksum(list[0].getAbsolutePath(), checksumType).getBytes(),
                                StandardOpenOption.CREATE);
                    }
                }
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new MojoFailureException(e.getMessage());
        }
    }


    private boolean copyResolvedArtifact(List<ArtifactRepository> list, String resolvedVersion)
            throws NoSuchAlgorithmException {
        for (ArtifactRepository repo : list) {
            try {
                writeContents(
                        new URL(repo.getUrl() + (groupId.replace('.', '/')) + '/' + artifactId + '/' + version + '/'
                                        + artifactId + "-" + (version.equals(resolvedVersion) ? version :
                                                                      version.replace("SNAPSHOT", resolvedVersion))
                                        + ".jar"));
                return true;
            } catch (IOException e) {
                continue;
            }
        }
        return false;
    }


    private void writeContents(URL path) throws IOException, NoSuchAlgorithmException {
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
