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

import static org.openecomp.sdc.onboarding.Constants.JAVA_EXT;
import static org.openecomp.sdc.onboarding.Constants.UNICORN;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

public class BuildHelper {

    private BuildHelper() {
        // donot remove.
    }

    static long getChecksum(File file, String fileType) {
        try {
            return readSources(file, fileType).hashCode();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static String getSourceChecksum(String data, String hashType) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(hashType);
        md.update(data.getBytes());
        byte[] hashBytes = md.digest();

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < hashBytes.length; i++) {
            sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }


    private static Map<String, String> readSources(File file, String fileType) throws IOException {
        Map<String, String> source = new HashMap<>();
        if (file.exists()) {
            List<File> list = Files.walk(Paths.get(file.getAbsolutePath()))
                                   .filter(JAVA_EXT.equals(fileType) ? BuildHelper::isRegularJavaFile :
                                                   Files::isRegularFile).map(p -> p.toFile())
                                   .collect(Collectors.toList());
            source.putAll(ForkJoinPool.commonPool()
                                      .invoke(new FileReadTask(list.toArray(new File[0]), file.getAbsolutePath())));
        }
        return source;
    }

    private static boolean isRegularJavaFile(Path path) {
        File file = path.toFile();
        return file.isFile() && file.getName().endsWith(JAVA_EXT);
    }

    private static String getData(File file, byte[] buffer) {
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis, 64 * 1024)) {
            bis.read(buffer, 0, ((int) file.length()));
            if (file.getAbsolutePath().indexOf(File.separator + "generated-sources" + File.separator) != -1) {
                StringBuffer sb = new StringBuffer();
                List<String> coll = Files.readAllLines(file.toPath());
                for (String s : coll) {
                    if (s != null && !s.trim().startsWith("/") && !s.trim().startsWith("*")) {
                        sb.append(s);
                    }
                }
                return sb.toString();
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        }
        return new String(buffer, 0, ((int) file.length()));
    }


    private static class FileReadTask extends RecursiveTask<Map<String, String>> {

        Map<String, String> store = new HashMap<>();
        private byte[] buffer = new byte[1024 * 1024];
        File[] files;
        String pathPrefix;
        private final int MAX_FILES = 10;

        FileReadTask(File[] files, String pathPrefix) {
            this.files = files;
            this.pathPrefix = pathPrefix;
        }

        @Override
        protected Map<String, String> compute() {
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
                    store.put(toRead.getAbsolutePath().substring(pathPrefix.length()), getData(toRead, buffer));
                }
            }

            return store;
        }
    }

    static String getArtifactPathInLocalRepo(String repoPath, MavenProject project, byte[] sourceChecksum)
            throws MojoFailureException {

        URI uri = null;
        try {
            uri = new URI(repoPath + (project.getGroupId().replace('.', '/')) + '/' + project.getArtifactId() + '/'
                                  + project.getVersion());
        } catch (URISyntaxException e) {
            throw new MojoFailureException(e.getMessage());
        }
        File f = new File(uri);
        File[] list = f.listFiles(t -> t.getName().equals(project.getArtifactId() + "-" + project.getVersion() + "."
                                                                  + project.getPackaging()));
        if (list != null && list.length > 0) {
            File checksumFile = new File(list[0].getParentFile(), project.getBuild().getFinalName() + "." + UNICORN);
            try {
                if (checksumFile.exists() && Arrays.equals(sourceChecksum, Files.readAllBytes(checksumFile.toPath()))) {
                    return list[0].getAbsolutePath();
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return null;
    }

    static <T> T readState(String fileName, Class<T> clazz) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
             ObjectInputStream ois = new ObjectInputStream(is)) {
            return clazz.cast(ois.readObject());
        } catch (Exception ignored) {
            //ignore. it is taken care.
            return null;
        }
    }

}
