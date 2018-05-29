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
import org.apache.maven.settings.Proxy;

@Mojo(name = "init-artifact-helper", threadSafe = true, defaultPhase = LifecyclePhase.PRE_CLEAN,
        requiresDependencyResolution = ResolutionScope.NONE)
public class InitializationHelperMojo extends AbstractMojo {

    private static final String SKIP_GET = "skipGet";
    private static final String HTTP = "http";
    private static final String HTTPS = "https";

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

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (System.getProperties().containsKey(SKIP_GET)) {
            project.getProperties()
                   .setProperty(SKIP_GET, Boolean.toString(System.getProperties().containsKey(SKIP_GET)));
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
                URL url = new URL(repo.getUrl() + (groupId.replace('.', '/')) + '/' + artifactId + '/' + version
                                          + "/maven-metadata.xml");
                setProxy(url);
                String content = artifactHelper.getContents(url);
                Matcher m = timestampPattern.matcher(content);
                if (m.find()) {
                    timestamp = m.group(1);
                }
                m = buildNumberPattern.matcher(content);
                if (m.find()) {
                    buildNumber = m.group(1);
                }
            } catch (IOException e) {
                getLog().debug(e);
            }
            if (timestamp != null && buildNumber != null) {
                return timestamp + "-" + buildNumber;
            }
        }
        return version;
    }

    private void setProxy(URL url) {
        if (url.getProtocol().equalsIgnoreCase(HTTP)) {
            setProperties("http.proxyHost", "http.proxyPort", "http.nonProxyHosts", HTTP);
        } else if (url.getProtocol().equalsIgnoreCase(HTTPS)) {
            setProperties("https.proxyHost", "https.proxyPort", "https.nonProxyHosts", HTTPS);
        }
    }

    private void setProperties(String proxyHostProperty, String proxyPortProperty, String nonProxyHostsProperty,
            String protocol) {
        for (Proxy proxy : session.getSettings().getProxies()) {
            if (proxy.isActive() && proxy.getProtocol().equalsIgnoreCase(protocol)) {
                System.setProperty(proxyHostProperty, proxy.getHost());
                System.setProperty(proxyPortProperty, String.valueOf(proxy.getPort()));
                System.setProperty(nonProxyHostsProperty, proxy.getNonProxyHosts());
            }
        }
    }
}
