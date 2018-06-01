package org.openecomp.sdc.onboarding.util;

import java.io.File;
import java.io.IOException;
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
    private String excludePackaging;
    @Parameter
    private ArtifactHelper artifactHelper;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (project.getPackaging().equals(excludePackaging)) {
            return;
        }
        if (project.getProperties().containsKey(ARTIFACT_COPY_PATH)
                    && project.getProperties().getProperty(ARTIFACT_COPY_PATH) != null) {
            File f = new File(project.getProperties().getProperty(ARTIFACT_COPY_PATH));
            if (f.exists()) {
                project.getArtifact().setFile(f);
            }
        }
        try {
            artifactHelper.shutDown(project);
        } catch (IOException | ClassNotFoundException e) {
            throw new MojoExecutionException("Unexpected Error Occured during shutdown activities", e);
        }
    }
}
