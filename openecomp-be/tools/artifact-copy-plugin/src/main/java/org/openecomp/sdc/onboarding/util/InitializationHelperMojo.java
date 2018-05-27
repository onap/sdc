package org.openecomp.sdc.onboarding.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

@Mojo(name = "init-artifact-helper", threadSafe = true, defaultPhase = LifecyclePhase.PRE_CLEAN,
        requiresDependencyResolution = ResolutionScope.NONE)
public class InitializationHelperMojo extends AbstractMojo {

    private static final String SKIP_GET = "skipGet";

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
    private String excludePackaging;
    @Parameter
    private ArtifactHelper artifactHelper;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (System.getProperties().containsKey(SKIP_GET)) {
            project.getProperties()
                   .setProperty(SKIP_GET, Boolean.valueOf(System.getProperties().containsKey(SKIP_GET)).toString());
            return;
        } else {
            File orgFile = new File(
                    session.getLocalRepository().getBasedir() + File.separator + (groupId.replace(".", File.separator))
                            + File.separator + artifactId + File.separator + version);
            String resolvedVersion = getResolvedVersion(artifactHelper.getRepositories(version.contains("SNAPSHOT")));
            project.getProperties().setProperty("resolvedVersion", resolvedVersion);
            System.getProperties().setProperty(SKIP_GET, Boolean.TRUE.toString());
            if (resolvedVersion.equals(version) && !orgFile.exists()) {
                project.getProperties().setProperty(SKIP_GET, Boolean.TRUE.toString());
            }
        }
    }

    private String getResolvedVersion(List<ArtifactRepository> list) {
        Pattern timestampPattern = Pattern.compile(".*<timestamp>(.*)</timestamp>.*");
        Pattern buildNumberPattern = Pattern.compile(".*<buildNumber>(.*)</buildNumber>.*");
        String timestamp = null;
        String buildNumber = null;
        for (ArtifactRepository repo : list) {
            try {
                String content = artifactHelper.getContents(
                        new URL(repo.getUrl() + (groupId.replace('.', '/')) + '/' + artifactId + '/' + version
                                        + "/maven-metadata.xml"));
                Matcher m = timestampPattern.matcher(content);
                if (m.find()) {
                    timestamp = m.group(1);
                }
                m = buildNumberPattern.matcher(content);
                if (m.find()) {
                    buildNumber = m.group(1);
                }
            } catch (IOException e) {
                continue;
            }
        }
        return timestamp != null && buildNumber != null ? timestamp + "-" + buildNumber : version;
    }

}
