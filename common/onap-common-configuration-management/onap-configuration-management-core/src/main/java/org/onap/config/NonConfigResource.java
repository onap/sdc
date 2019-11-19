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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class NonConfigResource {

    static final String NODE_CONFIG_LOCATION = "node.config.location";
    static final String CONFIG_LOCATION = "config.location";

    private final List<Function<String, Path>> lookupFunctions =
            Arrays.asList(this::getFromFile, this::findInFiles, this::getForNode, this::getGlobal, this::findInUris);

    private final Set<URI> uris = Collections.synchronizedSet(new HashSet<>());
    private final Set<File> files = Collections.synchronizedSet(new HashSet<>());

    private final Function<String, String> propertyGetter;

    NonConfigResource(Function<String, String> propertyGetter) {
        this.propertyGetter = propertyGetter;
    }

    public NonConfigResource() {
        this(System::getProperty);
    }

    public void add(URL url) {
        uris.add(toUri(url));
    }

    public void add(File file) {
        files.add(file);
    }

    public Path locate(String resource) {

        if (resource == null) {
            return null;
        }

        try {

            return lookupFunctions.stream()
                           .map(f -> f.apply(resource))
                           .filter(Objects::nonNull)
                           .findFirst().orElse(null);

        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private Path locate(File root, String resource) {

        if (!root.exists()) {
            return null;
        }

        return ConfigurationUtils.getAllFiles(root, true, false)
                       .stream()
                       .filter(f -> !ConfigurationUtils.isConfig(f))
                       .peek(this::add).filter(f -> f.getAbsolutePath().endsWith(resource))
                       .findFirst()
                       .map(file -> Paths.get(file.getAbsolutePath())).orElse(null);
    }

    private Path getFromFile(String resource) {
        return new File(resource).exists() ? Paths.get(resource) : null;
    }

    private Path findInUris(String resource) {
        for (URI uri : uris) {
            if (toUrl(uri).getFile().endsWith(resource)) {
                return Paths.get(uri);
            }
        }
        return null;
    }

    private Path findInFiles(String resource) {

        for (File availableFile : files) {

            String absolutePath = availableFile.getAbsolutePath();
            if (absolutePath.endsWith(resource) && availableFile.exists()) {
                return Paths.get(absolutePath);
            }
        }

        return null;
    }

    private Path getForNode(String resource) {
        return getFromProperty(NODE_CONFIG_LOCATION, resource);
    }

    private Path getGlobal(String resource) {
        return getFromProperty(CONFIG_LOCATION, resource);
    }

    private Path getFromProperty(String property, String resource) {
        String value = propertyGetter.apply(property);
        return (value == null) ? null : locate(new File(value), resource);
    }

    private static URI toUri(URL url) {

        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unexpected URL syntax: " + url, e);
        }
    }

    private static URL toUrl(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Unexpected URI syntax: " + uri, e);
        }
    }
}
