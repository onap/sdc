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
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "generate-signature", threadSafe = true, defaultPhase = LifecyclePhase.COMPILE,
        requiresDependencyResolution = ResolutionScope.NONE)
public class SnapshotSignature extends AbstractMojo {

    public static final String JAVA_EXT = ".java";
    public static final String UNICORN = "unicorn";
    public static final String CHECKSUM = "checksum";
    public static final String DOT = ".";
    public static final String SHA1 = "sha1";
    public static final String COLON = ":";
    public static final String ANY_EXT = "*";
    public static final String SNAPSHOT = "SNAPSHOT";
    public static final String JAR = "jar";

    @Parameter
    private File mainSourceLocation;
    @Parameter
    private File mainResourceLocation;
    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!JAR.equals(project.getPackaging())) {
            return;
        }
        init();
        long resourceChecksum = getChecksum(mainResourceLocation, ANY_EXT);
        long mainChecksum = getChecksum(mainSourceLocation, JAVA_EXT);
        byte[] sourceChecksum = calculateChecksum(mainChecksum, resourceChecksum).getBytes();
        generateSignature(sourceChecksum);
    }

    private void init() {
        if (mainSourceLocation == null) {
            mainSourceLocation = Paths.get(project.getBuild().getSourceDirectory()).toFile();
        }
        if (mainResourceLocation == null) {
            mainResourceLocation = Paths.get(project.getBuild().getResources().get(0).getDirectory()).toFile();
        }
    }

    private long getChecksum(File file, String fileType) {
        try {
            return readSources(file, fileType).hashCode();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Map<String, List<String>> readSources(File file, String fileType) throws IOException {
        Map<String, List<String>> source = new HashMap<>();
        if (file.exists()) {
            List<File> list = Files.walk(Paths.get(file.getAbsolutePath()))
                                   .filter(JAVA_EXT.equals(fileType) ? this::isRegularJavaFile : Files::isRegularFile)
                                   .map(Path::toFile).collect(Collectors.toList());
            source.putAll(ForkJoinPool.commonPool()
                                      .invoke(new FileReadTask(list.toArray(new File[0]), file.getAbsolutePath())));
        }
        return source;
    }

    private boolean isRegularJavaFile(Path path) {
        File file = path.toFile();
        return file.isFile() && file.getName().endsWith(JAVA_EXT);
    }

    private class FileReadTask extends RecursiveTask<Map<String, List<String>>> {

        private Map<String, List<String>> store = new HashMap<>();
        File[] files;
        String pathPrefix;
        private static final int MAX_FILES = 10;

        FileReadTask(File[] files, String pathPrefix) {
            this.files = files;
            this.pathPrefix = pathPrefix;
        }

        private List<String> getData(File file) throws IOException {
            List<String> coll = Files.readAllLines(file.toPath(), StandardCharsets.ISO_8859_1);
            if (file.getAbsolutePath().contains(File.separator + "generated-sources" + File.separator)) {
                Iterator<String> itr = coll.iterator();
                while (itr.hasNext()) {
                    String s = itr.next();
                    if (s == null || s.trim().startsWith("/") || s.trim().startsWith("*")) {
                        itr.remove();
                    }
                }
            }
            return coll;
        }


        @Override
        protected Map<String, List<String>> compute() {
            if (files.length > MAX_FILES) {
                FileReadTask task1 = new FileReadTask(Arrays.copyOfRange(files, 0, files.length / 2), pathPrefix);
                FileReadTask task2 =
                        new FileReadTask(Arrays.copyOfRange(files, files.length / 2, files.length), pathPrefix);
                task1.fork();
                task2.fork();
                store.putAll(task1.join());
                store.putAll(task2.join());
            } else {
                for (File toRead : files) {
                    try {
                        store.put(toRead.getAbsolutePath().substring(pathPrefix.length())
                                        .replace(File.separatorChar, '.'), getData(toRead));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }

            return store;
        }
    }

    private void generateSignature(byte[] sourceChecksum) {
        try {
            Paths.get(project.getBuild().getOutputDirectory()).toFile().mkdirs();
            Files.write(Paths.get(project.getBuild().getOutputDirectory(), UNICORN + DOT + CHECKSUM), sourceChecksum,
                    StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String calculateChecksum(long mainChecksum, long resourceChecksum) throws MojoExecutionException {
        try {
            return getSourceChecksum(mainChecksum + COLON + resourceChecksum, SHA1);
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private String getSourceChecksum(String data, String hashType) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(hashType);
        md.update(data.getBytes());
        byte[] hashBytes = md.digest();

        StringBuilder buffer = new StringBuilder();
        for (byte hashByte : hashBytes) {
            buffer.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
        }
        return buffer.toString();
    }
}
