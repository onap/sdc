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

import org.apache.commons.io.IOUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.utilities.yaml.YamlUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The type File utils.
 */
public class FileUtils {

  /**
   * Gets file input stream.
   *
   * @param fileName the file name
   * @return the file input stream
   */
  public static InputStream getFileInputStream(String fileName) {
    URL urlFile = FileUtils.class.getClassLoader().getResource(fileName);
    InputStream is;
    try {
      assert urlFile != null;
      is = urlFile.openStream();
    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }
    return is;
  }

  /**
   * Gets file input streams.
   *
   * @param fileName the file name
   * @return the file input streams
   */
  public static List<InputStream> getFileInputStreams(String fileName) {
    Enumeration<URL> urlFiles;
    List<InputStream> streams = new ArrayList<>();
    InputStream is;
    URL url;
    try {
      urlFiles = FileUtils.class.getClassLoader().getResources(fileName);
      while (urlFiles.hasMoreElements()) {
        url = urlFiles.nextElement();
        is = url.openStream();
        streams.add(is);
      }


    } catch (IOException exception) {
      throw new RuntimeException(exception);
    }
    return streams;
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
    URL urlFile = FileUtils.class.getClassLoader().getResource(fileName);
    try {
      Enumeration<URL> en = FileUtils.class.getClassLoader().getResources(fileName);
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

  /**
   * Gets file content map from zip.
   *
   * @param zipData the zip data
   * @return the file content map from zip
   * @throws IOException the io exception
   */
  public static FileContentHandler getFileContentMapFromZip(byte[] zipData) throws IOException {
    ZipEntry zipEntry;
    FileContentHandler mapFileContent = new FileContentHandler();
    try {
      ZipInputStream inputZipStream;

      byte[] fileByteContent;
      String currentEntryName;
      inputZipStream = new ZipInputStream(new ByteArrayInputStream(zipData));

      while ((zipEntry = inputZipStream.getNextEntry()) != null) {
        currentEntryName = zipEntry.getName();
        fileByteContent = FileUtils.toByteArray(inputZipStream);
        mapFileContent.addFile(currentEntryName, fileByteContent);
      }

    } catch (RuntimeException exception) {
      throw new IOException(exception);
    }
    return mapFileContent;
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
