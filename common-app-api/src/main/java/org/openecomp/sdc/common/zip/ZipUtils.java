/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.common.zip;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.openecomp.sdc.common.zip.exception.ZipException;
import org.openecomp.sdc.common.zip.exception.ZipSlipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles zip operations.
 */
public class ZipUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipUtils.class);

    private ZipUtils() {
    }

    /**
     * Checks if the path is a zip slip attempt calling the {@link #checkForZipSlipInRead(Path)} method.
     * @param zipEntry the zip entry
     * @throws ZipSlipException when a zip slip attempt is detected
     */
    public static void checkForZipSlipInRead(final ZipEntry zipEntry) throws ZipSlipException {
        final Path filePath = Paths.get(zipEntry.getName());
        checkForZipSlipInRead(filePath);
    }

    /**
     * Checks if the path is a zip slip attempt when you don't have a destination folder eg in memory reading or zip
     * creation.
     *
     * @param filePath the file path
     * @throws ZipSlipException when a zip slip attempt is detected
     */
    public static void checkForZipSlipInRead(final Path filePath) throws ZipSlipException {
        final File file = filePath.toFile();
        String canonicalPath = null;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (final IOException ex) {
            LOGGER.debug("Could not get canonical path of file '{}'", file.getPath(), ex);
        }
        if (canonicalPath != null && !canonicalPath.equals(file.getAbsolutePath())) {
            throw new ZipSlipException(filePath.toString());
        }

        if (filePath.toString().contains("../") || filePath.toString().contains("..\\")) {
            throw new ZipSlipException(filePath.toString());
        }
    }

    /**
     * Checks if the zip entry is a zip slip attempt based on the destination directory.
     *
     * @param zipEntry the zip entry
     * @param targetDirectoryPath the target extraction folder
     * @throws ZipException when the zip slip was detected as a {@link ZipSlipException}. Also when there was a problem
     * getting the canonical paths from the zip entry or target directory.
     */
    public static void checkForZipSlipInExtraction(final ZipEntry zipEntry,
                                                   final Path targetDirectoryPath) throws ZipException {
        final File targetDirectoryAsFile = targetDirectoryPath.toFile();
        final File targetFile = new File(targetDirectoryAsFile, zipEntry.getName());
        final String targetDirectoryCanonicalPath;
        try {
            targetDirectoryCanonicalPath = targetDirectoryAsFile.getCanonicalPath();
        } catch (final IOException e) {
            throw new ZipException(
                String.format("Could not obtain canonical path of: '%s'", targetDirectoryAsFile.getAbsolutePath()), e);
        }
        final String targetFileCanonicalPath;
        try {
            targetFileCanonicalPath = targetFile.getCanonicalPath();
        } catch (final IOException e) {
            throw new ZipException(
                String.format("Could not obtain canonical path of: '%s'", targetFile.getAbsolutePath()), e);
        }

        if (!targetFileCanonicalPath.startsWith(targetDirectoryCanonicalPath + File.separator)) {
            throw new ZipSlipException(zipEntry.getName());
        }
    }

    /**
     * Creates a ZipInputStream from a byte array.
     *
     * @param zipFileBytes the zip byte array
     * @return the created ZipInputStream.
     */
    private static ZipInputStream getInputStreamFromBytes(final byte[] zipFileBytes) {
        return new ZipInputStream(new ByteArrayInputStream(zipFileBytes));
    }

    /**
     * Reads a zip file into memory. Parses the zipFile in byte array and calls {@link #readZip(byte[], boolean)}.
     *
     * @param zipFile the zip file to read
     * @param hasToIncludeDirectories includes or not the directories found during the zip reading
     * @return a Map representing a pair of file path and file byte array
     * @throws ZipException when there was a problem during the reading process
     */
    public static Map<String, byte[]> readZip(final File zipFile,
                                              final boolean hasToIncludeDirectories) throws ZipException {
        try {
            return readZip(Files.readAllBytes(zipFile.toPath()), hasToIncludeDirectories);
        } catch (final IOException e) {
            throw new ZipException(String.format("Could not read the zip file '%s'", zipFile.getName()), e);
        }
    }

    /**
     * Reads a zip file to a in memory structure formed by the file path and its bytes. The structure can contains only
     * files or files and directories. If configured to include directories, only empty directories and directories that
     * contains files will be included. The full directory tree will not be generated, eg:
     * <pre>
     * \
     * \..\Directory
     * \..\..\ChildDirectory
     * \..\..\..\aFile.txt
     * \..\..\EmptyChildDirectory
     * </pre>
     * The return will include "Directory\ChildDirectory\aFile.txt" and "Directory\EmptyChildDirectory" but not
     * "Directory" or the root.
     *
     * @param zipFileBytes the zip file byte array to read
     * @param hasToIncludeDirectories includes or not the directories found during the zip reading.
     * @return a Map representing a pair of file path and file byte array
     * @throws ZipException when there was a problem during the reading process
     */
    public static Map<String, byte[]> readZip(final byte[] zipFileBytes,
                                              final boolean hasToIncludeDirectories) throws ZipException {
        final Map<String, byte[]> filePathAndByteMap = new HashMap<>();

        try (final ZipInputStream inputZipStream = ZipUtils.getInputStreamFromBytes(zipFileBytes)) {
            ZipEntry zipEntry;
            while ((zipEntry = inputZipStream.getNextEntry()) != null) {
                filePathAndByteMap
                    .putAll(processZipEntryInRead(zipEntry, getBytes(inputZipStream), hasToIncludeDirectories));
            }
        } catch (final IOException e) {
            LOGGER.warn("Could not close the zip input stream", e);
        }

        return filePathAndByteMap;
    }

    private static Map<String, byte[]> processZipEntryInRead(final ZipEntry zipEntry,
                                                             final byte[] inputStreamBytes,
                                                             final boolean hasToIncludeDirectories) throws ZipException {
        final Map<String, byte[]> filePathAndByteMap = new HashMap<>();
        checkForZipSlipInRead(zipEntry);
        if (zipEntry.isDirectory()) {
            if (hasToIncludeDirectories) {
                filePathAndByteMap.put(normalizeFolder(zipEntry.getName()), null);
            }
            return filePathAndByteMap;
        }

        if (hasToIncludeDirectories) {
            final Path parentFolderPath = Paths.get(zipEntry.getName()).getParent();
            if (parentFolderPath != null) {
                filePathAndByteMap.putIfAbsent(normalizeFolder(parentFolderPath.toString()), null);
            }
        }
        filePathAndByteMap.put(zipEntry.getName(), inputStreamBytes);

        return filePathAndByteMap;
    }

    /**
     * Adds a {@link File#separator} at the end of the folder path if not present.
     *
     * @param folderPath the folder to normalize
     * @return the normalized folder
     */
    private static String normalizeFolder(final String folderPath) {
        final StringBuilder normalizedFolderBuilder = new StringBuilder(folderPath);
        if(!folderPath.endsWith(File.separator)) {
            normalizedFolderBuilder.append(File.separator);
        }
        return normalizedFolderBuilder.toString();
    }

    /**
     * Converts a ZipInputStream in byte array.
     *
     * @param inputZipStream the zip input stream
     * @return the byte array representing the input stream
     * @throws ZipException when there was a problem parsing the input zip stream
     */
    private static byte[] getBytes(final ZipInputStream inputZipStream) throws ZipException {
        final byte[] fileByteContent;
        try {
            fileByteContent = IOUtils.toByteArray(inputZipStream);
        } catch (final IOException e) {
            throw new ZipException("Could not read bytes from file", e);
        }
        return fileByteContent;
    }

    /**
     * Unzips a zip file into an output folder.
     *
     * @param zipFilePath the zip file path
     * @param outputFolder the output folder path
     * @throws ZipException when there was a problem during the unzip process
     */
    public static void unzip(final Path zipFilePath, final Path outputFolder) throws ZipException {
        if (zipFilePath == null || outputFolder == null) {
            return;
        }
        createDirectoryIfNotExists(outputFolder);

        final File zipFile = zipFilePath.toFile();
        try (final FileInputStream fileInputStream = new FileInputStream(zipFile);
            final ZipInputStream stream = new ZipInputStream(fileInputStream)) {

            ZipEntry zipEntry;
            while ((zipEntry = stream.getNextEntry()) != null) {
                checkForZipSlipInExtraction(zipEntry, outputFolder);
                final String fileName = zipEntry.getName();
                final Path fileToWritePath = Paths.get(outputFolder.toString(), fileName);
                if (zipEntry.isDirectory()) {
                    createDirectoryIfNotExists(fileToWritePath);
                } else {
                    writeFile(stream, fileToWritePath);
                }
            }
        } catch (final FileNotFoundException e) {
            throw new ZipException(String.format("Could not find file: '%s'", zipFile.getAbsolutePath()), e);
        } catch (final IOException e) {
            throw new ZipException(
                String.format("An unexpected error occurred trying to unzip '%s'", zipFile.getAbsolutePath()), e);
        }
    }

    /**
     * Writes a file from a zipInputStream to a path. Creates the file parent directories if they don't exist.
     * @param zipInputStream the zip input stream
     * @param fileToWritePath the file path to write
     * @throws ZipException when there was a problem during the file creation
     */
    private static void writeFile(final ZipInputStream zipInputStream, final Path fileToWritePath) throws ZipException {
        final Path parentFolderPath = fileToWritePath.getParent();
        if (parentFolderPath != null) {
            try {
                Files.createDirectories(parentFolderPath);
            } catch (final IOException e) {
                throw new ZipException(
                    String.format("Could not create parent directories of '%s'", fileToWritePath.toString()), e);
            }
        }
        try (final FileOutputStream outputStream = new FileOutputStream(fileToWritePath.toFile())) {
            IOUtils.copy(zipInputStream, outputStream);
        } catch (final FileNotFoundException e) {
            throw new ZipException(String.format("Could not find file '%s'", fileToWritePath.toString()), e);
        } catch (final IOException e) {
            throw new ZipException(
                String.format("An unexpected error has occurred while writing file '%s'", fileToWritePath.toString())
                , e);
        }
    }

    /**
     * Creates the path directories if the provided path does not exists.
     *
     * @param path the path to create directories
     * @throws ZipException when there was a problem to create the directories
     */
    private static void createDirectoryIfNotExists(final Path path) throws ZipException {
        if(path.toFile().exists()) {
            return;
        }
        try {
            Files.createDirectories(path);
        } catch (final IOException e) {
            throw new ZipException(String.format("Could not create directories for path '%s'", path.toString()), e);
        }
    }

    /**
     * Zips a directory and its children content.
     *
     * @param fromPath the directory path to zip
     * @param toZipFilePath the path to the zip file that will be created
     * @throws ZipException when there was a problem during the zip process
     */
    public static void createZipFromPath(final Path fromPath, final Path toZipFilePath) throws ZipException {
        final Path createdZipFilePath;
        try {
            createdZipFilePath = Files.createFile(toZipFilePath);
        } catch (final IOException e) {
            throw new ZipException(String.format("Could not create file '%s'", toZipFilePath.toString()), e);
        }

        try(final FileOutputStream fileOutputStream = new FileOutputStream(createdZipFilePath.toFile());
            final BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            final ZipOutputStream zipOut = new ZipOutputStream(bos);
            final Stream<Path> walkStream = Files.walk(fromPath)) {
            final Set<Path> allFilesSet = walkStream.collect(Collectors.toSet());
            for (final Path path : allFilesSet) {
                checkForZipSlipInRead(path);
                if (path.equals(fromPath)) {
                    continue;
                }
                final Path relativePath = fromPath.relativize(path);
                final File file = path.toFile();
                if (file.isDirectory()) {
                    zipOut.putNextEntry(new ZipEntry(relativePath.toString() + File.separator));
                } else {
                    zipOut.putNextEntry(new ZipEntry(relativePath.toString()));
                    zipOut.write(Files.readAllBytes(path));
                }
                zipOut.closeEntry();
            }
        } catch (final FileNotFoundException e) {
            throw new ZipException(String.format("Could not create file '%s'", toZipFilePath.toString()), e);
        } catch (final IOException e) {
            throw new ZipException("An error has occurred while creating the zip package", e);
        }
    }

}
