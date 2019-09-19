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

public class FileContentHandler {

    private Map<String, byte[]> files = new HashMap<>();

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

    public byte[] getFileContent(final String fileName) {
        return files.get(fileName);
    }

    public boolean isFolder(final String fileName) {
        return files.get(fileName) == null;
    }

    public boolean isFile(final String fileName) {
        return files.get(fileName) != null;
    }

    public void addFolder(final String folder) {
        files.put(folder, null);
    }

    public void addFile(final String fileName, final byte[] content) {
        files.put(fileName, content == null ? new byte[0] : content);
    }

    public void addFile(final String fileName, final InputStream is) {
        files.put(fileName, FileUtils.toByteArray(is));
    }

    public Map<String, byte[]> getFiles() {
        return files.entrySet().stream().filter(entry -> entry.getValue() != null)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void setFiles(final Map<String, byte[]> files) {
        addAll(files);
    }

    public Set<String> getFileList() {
        return files.keySet().stream().filter(this::isFile).collect(Collectors.toSet());
    }

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

    public boolean isEmpty() {
        return MapUtils.isEmpty(this.files);
    }

    public byte[] remove(final String fileName) {
        return files.remove(fileName);
    }

    public boolean containsFile(final String fileName) {
        return files.containsKey(fileName);
    }

}
