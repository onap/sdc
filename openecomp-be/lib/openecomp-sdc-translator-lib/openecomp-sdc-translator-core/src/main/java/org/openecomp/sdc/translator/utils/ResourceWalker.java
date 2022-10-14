/*
 * Copyright © 2016-2017 European Support Limited
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
package org.openecomp.sdc.translator.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;

public class ResourceWalker {

    private static final String RESOURCE_FILE_READ_ERROR = "Can't read resource file from class path.";

    private ResourceWalker() {
    }

    /**
     * Read resources from directory map.
     *
     * @param resourceDirectoryToStart the resource directory to start
     * @return the map of file where key is file name and value is its data
     * @throws Exception the exception
     */
    public static Map<String, String> readResourcesFromDirectory(String resourceDirectoryToStart) throws Exception {
        Map<String, String> filesContent = new HashMap<>();
        traverse(resourceDirectoryToStart, (fileName, stream) -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                filesContent.put(fileName, IOUtils.toString(reader));
            } catch (IOException exception) {
                throw new CoreException((new ErrorCode.ErrorCodeBuilder()).withMessage(RESOURCE_FILE_READ_ERROR + " File name = " + fileName)
                    .withId("Resource Read Error").withCategory(ErrorCategory.APPLICATION).build(), exception);
            }
        });
        return filesContent;
    }

    private static void traverse(String start, BiConsumer<String, InputStream> handler) throws Exception {
        URL url = ResourceWalker.class.getClassLoader().getResource(start);
        if (url == null) {
            throw new FileNotFoundException("Resource not found: " + start);
        }
        switch (url.getProtocol().toLowerCase()) {
            case "file":
                traverseFile(new File(url.getPath()), handler);
                break;
            case "zip":
            case "jar":
                String path = url.getPath();
                int resourcePosition = path.lastIndexOf("!/" + start);
                traverseArchive(path.substring(0, resourcePosition), start, handler);
                break;
            default:
                throw new IllegalArgumentException("Unknown protocol");
        }
    }

    private static void traverseArchive(String file, String resource, BiConsumer<String, InputStream> handler)
        throws URISyntaxException, IOException {
        // There is what looks like a bug in Java:

        // if "abc" is a directory in an archive,

        // both "abc" and "abc/" will be found successfully.

        // However, calling isDirectory() will return "true" for "abc/",

        // but "false" for "abc".
        try (ZipFile zip = new ZipFile(new URI(file).getPath())) {
            Predicate<ZipEntry> predicate = buildPredicate(resource);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                handleZipEntry(predicate, zip, entries.nextElement(), handler);
            }
        }
    }

    private static Predicate<ZipEntry> buildPredicate(String resource) {
        if (resource.endsWith("/")) {
            return zipEntry -> zipEntry.getName().startsWith(resource) && !zipEntry.isDirectory();
        } else {
            return zipEntry -> {
                String name = zipEntry.getName();
                return (name.equals(resource) || name.startsWith(resource + "/")) && !zipEntry.isDirectory() && !name.contains("../");
            };
        }
    }

    private static void handleZipEntry(Predicate<ZipEntry> predicate, ZipFile zip, ZipEntry zipEntry, BiConsumer<String, InputStream> handler)
        throws IOException {
        if (predicate.test(zipEntry)) {
            try (InputStream input = zip.getInputStream(zipEntry)) {
                handler.accept(zipEntry.getName(), input);
            }
        }
    }

    private static void traverseFile(File file, BiConsumer<String, InputStream> handler) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File sub : files) {
                    traverseFile(sub, handler);
                }
            }
        } else {
            try (FileInputStream stream = new FileInputStream(file)) {
                handler.accept(file.getPath(), stream);
            }
        }
    }
}
