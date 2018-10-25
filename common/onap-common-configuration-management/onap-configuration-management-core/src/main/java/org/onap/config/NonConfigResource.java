/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.config;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class NonConfigResource {

    private static final Set<URL> urls = new HashSet<>();
    private static final Set<File> files = new HashSet<>();

    public static void add(URL url) {
        urls.add(url);
    }

    public static Path locate(String resource) {
        try {
            if (resource != null) {
                File file = new File(resource);
                if (file.exists()) {
                    return Paths.get(resource);
                }
                for (File availableFile : files) {
                    if (availableFile.getAbsolutePath().endsWith(resource) && availableFile.exists()) {
                        return Paths.get(availableFile.getAbsolutePath());
                    }
                }
                if (System.getProperty("node.config.location") != null) {
                    Path path = locate(new File(System.getProperty("node.config.location")), resource);
                    if (path != null) {
                        return path;
                    }
                }
                if (System.getProperty("config.location") != null) {
                    Path path = locate(new File(System.getProperty("config.location")), resource);
                    if (path != null) {
                        return path;
                    }
                }
                for (URL url : urls) {
                    if (url.getFile().endsWith(resource)) {
                        return Paths.get(url.toURI());
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }

    private static Path locate(File root, String resource) {
        if (root.exists()) {
            Collection<File> filesystemResources = ConfigurationUtils.getAllFiles(root, true, false);
            Predicate<File> f1 = ConfigurationUtils::isConfig;
            for (File file : filesystemResources) {
                if (!f1.test(file)) {
                    add(file);
                    if (file.getAbsolutePath().endsWith(resource)) {
                        return Paths.get(file.getAbsolutePath());
                    }
                }
            }
        }
        return null;
    }

    public static void add(File file) {
        files.add(file);
    }
}
