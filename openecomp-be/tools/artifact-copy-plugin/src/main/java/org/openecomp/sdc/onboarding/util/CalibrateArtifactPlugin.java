package org.openecomp.sdc.onboarding.util;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

@Mojo(name = "calibrate-artifact-helper", threadSafe = true, defaultPhase = LifecyclePhase.INSTALL,
        requiresDependencyResolution = ResolutionScope.TEST)
public class CalibrateArtifactPlugin extends AbstractMojo {

    private static final String ARTIFACT_COPY_PATH = "artifactPathToCopy";

    @Parameter(defaultValue = "${session}")
    private MavenSession session;
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    @Component
    private MavenProjectHelper projectHelper;
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
    private String excludePackaging;
    @Parameter
    private ArtifactHelper artifactHelper;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (project.getPackaging().equals(excludePackaging)) {
            return;
        }
        if (project.getProperties().containsKey(ARTIFACT_COPY_PATH)
                    && project.getProperties().getProperty(ARTIFACT_COPY_PATH) != null) {
            File f = null;
            String artifactPath = project.getProperties().getProperty(ARTIFACT_COPY_PATH)
                                         .startsWith(session.getLocalRepository().getBasedir()) ?
                                          project.getProperties().getProperty(ARTIFACT_COPY_PATH) :
                                          project.getProperties().getProperty(ARTIFACT_COPY_PATH)
                                                 .replace(groupId, groupId.replace('.', '/'));
            if (artifactPath.startsWith(session.getLocalRepository().getBasedir())) {
                f = new File(artifactPath);
            } else {
                f = new File(session.getLocalRepository().getBasedir(), artifactPath);
            }
            if (f.exists()) {
                project.getArtifact().setFile(f);
            }
        }
        File file = new File(project.getBuild().getDirectory(), project.getBuild().getFinalName() + ".unicorn");
        if (file.exists()) {
            try {
                Files.copy(file.toPath(), Paths.get(
                        session.getLocalRepository().getBasedir() + File.separator + project.getGroupId().replace(".",
                                File.separator) + File.separator + project.getArtifactId() + File.separator
                                + project.getVersion(), project.getBuild().getFinalName() + ".unicorn"),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
