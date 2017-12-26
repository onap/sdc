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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.tosca.services.YamlUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The type File utils.
 */
public class FileUtils {

  /**
   * Allows to consume an input stream open against a resource with a given file name.
   *
   * @param fileName the file name
   * @param function logic to be applied to the input stream
   */
  public static <T> T readViaInputStream(String fileName, Function<InputStream, T> function) {

    Objects.requireNonNull(fileName);

    // the leading slash doesn't make sense and doesn't work when used with a class loader
    URL resource = FileUtils.class.getClassLoader().getResource(fileName.startsWith("/")
        ? fileName.substring(1) : fileName);
    if (resource == null) {
      throw new IllegalArgumentException("Resource not found: " + fileName);
    }

    return readViaInputStream(resource, function);
  }

  /**
   * Allows to consume an input stream open against a resource with a given URL.
   *
   * @param urlFile the url file
   * @param function logic to be applied to the input stream
   */
  public static <T> T readViaInputStream(URL urlFile, Function<InputStream, T> function) {

    Objects.requireNonNull(urlFile);
    try (InputStream is = urlFile.openStream()) {
      return function.apply(is);
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  /**
   * Gets file input streams.
   *
   * @param fileName the file name
   * @return the file input streams
   */
  public static List<URL> getAllLocations(String fileName) {

    List<URL> urls = new LinkedList<>();
    Enumeration<URL> urlFiles;

    try {
      urlFiles = FileUtils.class.getClassLoader().getResources(fileName);
      while (urlFiles.hasMoreElements()) {
        urls.add(urlFiles.nextElement());
      }


    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }

    return urls.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(urls);
  }

  /**
   * Convert to bytes byte [ ].
   *
   * @param object    the object
   * @param extension the extension
   * @return the byte [ ]
   */
  public static byte[] convertToBytes(Object object, FileExtension extension) {
    if (object != null) {
      if (extension.equals(FileExtension.YAML) || extension.equals(FileExtension.YML)) {
        return new YamlUtil().objectToYaml(object).getBytes();
      } else {
        return JsonUtil.object2Json(object).getBytes();
      }
    } else {
      return new byte[]{};
    }
  }

  /**
   * Convert to input stream input stream.
   *
   * @param object    the object
   * @param extension the extension
   * @return the input stream
   */
  public static InputStream convertToInputStream(Object object, FileExtension extension) {
    if (object != null) {

      byte[] content;

      if (extension.equals(FileExtension.YAML) || extension.equals(FileExtension.YML)) {
        content = new YamlUtil().objectToYaml(object).getBytes();
      } else {
        content = JsonUtil.object2Json(object).getBytes();

      }
      return new ByteArrayInputStream(content);
    } else {
      return null;
    }
  }

  /**
   * Load file to input stream input stream.
   *
   * @param fileName the file name
   * @return the input stream
   */
  public static InputStream loadFileToInputStream(String fileName) {
    URL urlFile = Thread.currentThread().getContextClassLoader().getResource(fileName);
    try {
      Enumeration<URL> en = Thread.currentThread().getContextClassLoader().getResources(fileName);
      while (en.hasMoreElements()) {
        urlFile = en.nextElement();
      }
    } catch (IOException | NullPointerException exception) {
      throw new RuntimeException(exception);
    }
    try {
      if (urlFile != null) {
        return urlFile.openStream();
      } else {
        throw new RuntimeException();
      }
    } catch (IOException | NullPointerException exception) {
      throw new RuntimeException(exception);
    }

  }

  /**
   * To byte array byte [ ].
   *
   * @param input the input
   * @return the byte [ ]
   */
  public static byte[] toByteArray(InputStream input) {
    if (input == null) {
      return new byte[0];
    }
    try {
      return IOUtils.toByteArray(input);
    } catch (IOException exception) {
      throw new RuntimeException(
          "error will convertion input stream to byte array:" + exception.getMessage());
    }
  }

  /**
   * Gets file without extention.
   *
   * @param fileName the file name
   * @return the file without extention
   */
  public static String getFileWithoutExtention(String fileName) {
    if (!fileName.contains(".")) {
      return fileName;
    }
    return fileName.substring(0, fileName.lastIndexOf("."));
  }

  public static String getFileExtension(String filename) {
      return FilenameUtils.getExtension(filename);
  }

  public static String getNetworkPackageName(String filename) {
    String[] split = filename.split("\\.");
    String name = null;
    if (split.length > 1) {
      name = split[0];
    }
    return name;
  }

  /**
   * Gets file content map from zip.
   *
   * @param zipData the zip data
   * @return the file content map from zip
   * @throws IOException the io exception
   */
  public static FileContentHandler getFileContentMapFromZip(byte[] zipData) throws IOException {

    try (ZipInputStream inputZipStream = new ZipInputStream(new ByteArrayInputStream(zipData))) {

      FileContentHandler mapFileContent = new FileContentHandler();

      ZipEntry zipEntry;

      while ((zipEntry = inputZipStream.getNextEntry()) != null) {
        mapFileContent.addFile(zipEntry.getName(), FileUtils.toByteArray(inputZipStream));
      }

      return mapFileContent;

    } catch (RuntimeException exception) {
      throw new IOException(exception);
    }
  }


  /**
   * The enum File extension.
   */
  public enum FileExtension {

    /**
     * Json file extension.
     */
    JSON("json"),
    /**
     * Yaml file extension.
     */
    YAML("yaml"),
    /**
     * Yml file extension.
     */
    YML("yml");

    private String displayName;

    FileExtension(String displayName) {
      this.displayName = displayName;
    }

    /**
     * Gets display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
      return displayName;
    }
  }


}
