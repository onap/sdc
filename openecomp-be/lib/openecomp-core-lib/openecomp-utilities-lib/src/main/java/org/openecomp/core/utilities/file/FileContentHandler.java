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

import org.apache.commons.collections4.MapUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FileContentHandler {
  private Map<String, byte[]> files = new HashMap<>();

  /**
   * Gets file content.
   *
   * @param fileName the file name
   * @return the file content
   */
  public InputStream getFileContent(String fileName) {

    byte[] content = files.get(fileName);
    if (content == null || content.length == 0) {
      return null;
    }

    ByteArrayInputStream is = new ByteArrayInputStream(content);
    return is;
  }

  public void addFile(String fileName, byte[] contect) {
    files.put(fileName, contect);
  }

  public void addFile(String fileName, InputStream is) {

    files.put(fileName, FileUtils.toByteArray(is));
  }

  public void setFiles(FileContentHandler extFiles) {
    extFiles.getFileList().stream()
        .forEach(fileName -> this.addFile(fileName, extFiles.getFileContent(fileName)));
  }

  public Set<String> getFileList() {
    return files.keySet();
  }

  public void putAll(Map<String, byte[]> files) {
    this.files = files;
  }

  public void addAll(FileContentHandler other) {
    this.files.putAll(other.files);
  }

  public boolean isEmpty() {
    return MapUtils.isEmpty(this.files);
  }

  public void remove(String fileName) {
    files.remove(fileName);
  }

  public boolean containsFile(String fileName) {
    return files.containsKey(fileName);
  }
}
