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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

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

    return new ByteArrayInputStream(content);
  }

  /**
   * Applies a business logic to a file's content while taking care of all retrieval logic.
   *
   * @param fileName name of a file inside this content handler.
   * @param processor the business logic to work on the file's input stream, which may not be set
   *                  (check the {@link Optional} if no such file can be found
   * @param <T> return type, may be {@link java.lang.Void}
   *
   * @return result produced by the processor
   */
  public <T> T processFileContent(String fileName, Function<Optional<InputStream>, T> processor) {

    // do not throw IOException to mimic the existing uses of getFileContent()
    try (InputStream contentInputStream = getFileContent(fileName)) {
      return processor.apply(Optional.ofNullable(contentInputStream));
    } catch (IOException e) {
      throw new ProcessingException("Failed to process file: " + fileName, e);
    }
  }

  public void addFile(String fileName, byte[] content) {
    files.put(fileName, content);
  }

  public void addFile(String fileName, InputStream is) {

    files.put(fileName, FileUtils.toByteArray(is));
  }

  public Map<String, byte[]> getFiles() {
    return files;
  }

  public void setFiles(Map<String, byte[]> files) {
    this.files = files;
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

  /**
   * An application-specific runtime exception
   */
  private static class ProcessingException extends RuntimeException {

    public ProcessingException() {
      super();
    }

    public ProcessingException(String message) {
      super(message);
    }

    public ProcessingException(Throwable cause) {
      super(cause);
    }

    public ProcessingException(String msg, Throwable cause) {
      super(msg, cause);
    }
  }
}
