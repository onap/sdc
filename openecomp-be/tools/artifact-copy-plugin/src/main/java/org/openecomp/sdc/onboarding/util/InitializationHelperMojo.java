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
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    private static final String UNICORN_INITIALIZED = "unicorn_initialized";
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final String SNAPSHOT = "SNAPSHOT";
    private static final String DOT = ".";
    File tempLocation = Paths.get(System.getProperties().getProperty("java.io.tmpdir")).toFile();

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
    private String excludePackaging;
    @Parameter
    private ArtifactHelper artifactHelper;
    @Parameter
    private String cleanupRoot;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (System.getProperties().containsKey(UNICORN_INITIALIZED)) {
            return;
        }
        artifactHelper.init(groupId + ":" + artifactId);
        String resolvedVersion =
                getResolvedVersion(artifactHelper.getRepositories(version.contains(SNAPSHOT)), artifactId);
        getLog().info("Unicorn Initialization status :" + !resolvedVersion.equals(version));
        System.getProperties().setProperty(UNICORN_INITIALIZED, Boolean.TRUE.toString());
        artifactHelper.deleteAll(artifactHelper.getUnicornMetaLocation());
    }

    private String getResolvedVersion(List<ArtifactRepository> list, String artifactId) {
        Pattern timestampPattern = Pattern.compile(".*<timestamp>(.*)</timestamp>.*");
        Pattern buildNumberPattern = Pattern.compile(".*<buildNumber>(.*)</buildNumber>.*");

        String timestamp = null;
        String buildNumber = null;
        for (ArtifactRepository repo : list) {
            try {
                URL url = new URL(repo.getUrl() + (groupId.replace('.', '/')) + '/' + artifactId + '/' + version
                                          + "/maven-metadata.xml");
                URL fallbackUrl =
                        new URL(repo.getUrl() + (groupId.replace('.', '/')) + '/' + artifactId + '/' + version + '/');
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
                timestamp = verifyBuildTimestamp(buildNumber, timestamp, fallbackUrl);
                if (timestamp != null && buildNumber != null) {
                    //                    project.getProperties().setProperty(artifactId + "-Version", timestamp + "-" + buildNumber);
                    byte[] data = fetchContents(repo.getUrl(), artifactId, timestamp + "-" + buildNumber);
                    artifactHelper.store(artifactId, data);
                    //                    project.getProperties().setProperty(artifactId + "-URL", repo.getUrl());
                    getLog().info(artifactId + " Version to be copied is " + timestamp + "-" + buildNumber);
                    return timestamp + "-" + buildNumber;
                }
            } catch (IOException e) {
                getLog().debug(e);
            }
        }
        return version;
    }

    private String verifyBuildTimestamp(String buildNumber, String timestamp, URL fallbackUrl) throws IOException {
        if (buildNumber == null) {
            return timestamp;
        }
        String buildPage = artifactHelper.getContents(fallbackUrl);
        Pattern verifyPattern = Pattern.compile(
                ".*" + artifactId + "-" + version.replace(SNAPSHOT, "") + "(.*)" + "-" + buildNumber + ".jar</a>.*");
        Matcher m = verifyPattern.matcher(buildPage);
        if (m.find()) {
            String str = m.group(1);
            if (!str.equals(timestamp)) {
                return str;
            }
        }
        return timestamp;
    }

    private byte[] fetchContents(String repoUrl, String artifactId, String resolvedVersion) throws IOException {
        File file = new File(tempLocation, "build-data-installer.jar");
        URL path = new URL(repoUrl + (groupId.replace('.', '/')) + '/' + artifactId + '/' + version + '/' + artifactId
                                   + "-" + (version.equals(resolvedVersion) ? version :
                                                    version.replace(SNAPSHOT, resolvedVersion)) + DOT + "jar");
        try (InputStream is = path.openStream()) {
            Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        byte[] data = Files.readAllBytes(file.toPath());
        project.getProperties().setProperty("artifactPathToCopy", file.getAbsolutePath());
        try {
            addJarToClasspath(Paths.get(project.getProperties().getProperty("artifactPathToCopy")).toFile());
        } catch (Exception e) {
            getLog().error("Error while feeding the build-data into system.", e);
        }

        return data;
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
                if (proxy.getHost() != null && !proxy.getHost().trim().isEmpty()) {
                    System.setProperty(proxyHostProperty, proxy.getHost());
                    System.setProperty(proxyPortProperty, String.valueOf(proxy.getPort()));
                }
                if (proxy.getNonProxyHosts() != null && !proxy.getNonProxyHosts().trim().isEmpty()) {
                    System.setProperty(nonProxyHostsProperty, proxy.getNonProxyHosts());
                }
            }
        }
    }

    public void addJarToClasspath(File jar) throws MojoFailureException {
        try {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            Class<?> clazz = cl.getClass();

            Method method = clazz.getSuperclass().getDeclaredMethod("addURL", new Class[] {URL.class});

            method.setAccessible(true);
            method.invoke(cl, new Object[] {jar.toURI().toURL()});
        } catch (Exception e) {
            throw new MojoFailureException("Problem while loadig build-data", e);
        }
    }

}
