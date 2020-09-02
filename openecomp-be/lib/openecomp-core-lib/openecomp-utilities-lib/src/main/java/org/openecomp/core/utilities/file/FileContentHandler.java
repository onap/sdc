/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.core.utilities.file;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

/**
 * Stores the content of files in a path:byte[] structure
 */
public class FileContentHandler {

    private final Map<String, byte[]> files = new HashMap<>();

    public FileContentHandler() {
    }

    public FileContentHandler(final FileContentHandler other) {
        addAll(other);
    }

    /**
     * Gets file content as stream.
     *
     * @param fileName the file name
     * @return if the file was found, its content as stream, otherwise {@code null}.
     */
    public InputStream getFileContentAsStream(final String fileName) {
        byte[] content = files.get(fileName);
        if (content == null || content.length == 0) {
            return null;
        }

        return new ByteArrayInputStream(content);
    }

    /**
     * Gets the content of a file.
     *
     * @param filePath the file path
     * @return the content of the file
     */
    public byte[] getFileContent(final String filePath) {
        return files.get(filePath);
    }

    /**
     * Checks if the path is a folder.
     *
     * @param filePath the file path to verify
     * @return {@code true} if the path is a folder, {@code false} otherwise
     */
    public boolean isFolder(final String filePath) {
        return files.get(filePath) == null;
    }

    /**
     * Checks if the path is a file.
     *
     * @param filePath the file path to verify
     * @return {@code true} if the path is a file, {@code false} otherwise
     */
    public boolean isFile(final String filePath) {
        return files.get(filePath) != null;
    }

    /**
     * Adds a folder.
     *
     * @param folderPath the folder path to add
     */
    public void addFolder(final String folderPath) {
        files.put(folderPath, null);
    }

    /**
     * Adds a file.
     *
     * @param filePath the file path
     * @param content the file content
     */
    public void addFile(final String filePath, final byte[] content) {
        files.put(filePath, content == null ? new byte[0] : content);
    }

    /**
     * Adds a file.
     *
     * @param filePath the file path
     * @param fileInputStream the file input stream
     */
    public void addFile(final String filePath, final InputStream fileInputStream) {
        files.put(filePath, FileUtils.toByteArray(fileInputStream));
    }

    /**
     * Gets only the files, ignoring directories from the structure.
     *
     * @return a file path:content map
     */
    public Map<String, byte[]> getFiles() {
        return files.entrySet().stream().filter(entry -> entry.getValue() != null)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void setFiles(final Map<String, byte[]> files) {
        addAll(files);
    }

    /**
     * Gets only the file paths, ignoring directories from the structure.
     *
     * @return a set of the file paths
     */
    public Set<String> getFileList() {
        return files.keySet().stream().filter(this::isFile).collect(Collectors.toSet());
    }

    /**
     * Gets only the folder paths, ignoring files from the structure.
     *
     * @return a set of the folder paths
     */
    public Set<String> getFolderList() {
        return files.keySet().stream().filter(this::isFolder).collect(Collectors.toSet());
    }

    public void addAll(final FileContentHandler fileContentHandlerOther) {
        if (CollectionUtils.isNotEmpty(fileContentHandlerOther.getFolderList())) {
            fileContentHandlerOther.getFolderList().forEach(this::addFolder);
        }
        addAll(fileContentHandlerOther.getFiles());
    }

    private void addAll(final Map<String, byte[]> files) {
        if (!MapUtils.isEmpty(files)) {
            files.forEach(this::addFile);
        }
    }

    /**
     * Checks if the file structure is empty.
     *
     * @return {@code true} if the file structure is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return MapUtils.isEmpty(this.files);
    }

    /**
     * Removes a file or folder from the file structure.
     *
     * @param filePath the file path to remove
     * @return the removed file content
     */
    public byte[] remove(final String filePath) {
        return files.remove(filePath);
    }

    /**
     * Checks if the file structure contains the provided file.
     *
     * @param filePath the file path to search
     * @return {@code true} if the file exists, {@code false} otherwise
     */
    public boolean containsFile(final String filePath) {
        return files.containsKey(filePath);
    }

}
