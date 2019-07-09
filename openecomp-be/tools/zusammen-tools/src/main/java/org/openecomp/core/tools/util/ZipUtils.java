/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.core.tools.util;

import com.google.common.io.ByteStreams;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    private static final Logger logger = LoggerFactory.getLogger(ZipUtils.class);

    private ZipUtils() {
        // prevent instantiation
    }

    public static void createZip(String zipFileName, Path dir) throws IOException {
        File dirObj = dir.toFile();
        Path zippedFile = Files.createFile(Paths.get(zipFileName));
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(File.separator + zippedFile.toFile());
                BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
                ZipOutputStream out = new ZipOutputStream(bos)) {
            File[] files = dirObj.listFiles();
            for (File file : files) {
                out.putNextEntry(new ZipEntry(file.getName()));
                Files.copy(Paths.get(file.getPath()), out);
                out.closeEntry();
            }
            Utils.printMessage(logger, "Zip file was created " + zipFileName);
        }
    }

    public static void unzip(Path zipFile, Path outputFolder) throws IOException {
        if (zipFile == null || outputFolder == null) {
            return;
        }
        createDirectoryIfNotExists(outputFolder);

        try (FileInputStream fileInputStream = new FileInputStream(zipFile.toFile());
             ZipInputStream stream = new ZipInputStream(fileInputStream)) {

            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                assertEntryNotVulnerable(entry);
                String fileName = entry.getName();
                File newFile = new File(outputFolder.toString() + File.separator + fileName);
                if (entry.isDirectory()) {
                    createDirectoryIfNotExists(newFile.toPath());
                } else {
                    persistFile(stream, newFile);
                }
            }
        }

    }

    private static void persistFile(ZipInputStream stream, File newFile) throws IOException {
        new File(newFile.getParent()).mkdirs();
        try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
            ByteStreams.copy(stream, outputStream);
        }
    }

    private static void createDirectoryIfNotExists(Path path) throws IOException {
        if (!path.toFile().exists()) {
            Files.createDirectories(path);
        }
    }

    private static void assertEntryNotVulnerable(ZipEntry entry) throws ZipException {
        if (entry.getName().contains("../")) {
            throw new ZipException("Path traversal attempt discovered.");
        }
    }
}

