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

package org.openecomp.sdc.heat.services.manifest;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * The type Manifest util.
 */
public class ManifestUtil {


  /**
   * Gets file and its env.
   *
   * @param manifestContent the manifest content
   * @return the file and its env
   */
  public static Map<String, FileData> getFileAndItsEnv(ManifestContent manifestContent) {
    Map<String, FileData> fileEnvMap = new HashMap<>();
    scanFileEnvMap(null, manifestContent.getData(), fileEnvMap);
    return fileEnvMap;
  }


  /**
   * Scan file env map.
   *
   * @param fileData     the file data
   * @param fileDataList the file data list
   * @param fileEnvMap   the file env map
   */
  public static void scanFileEnvMap(FileData fileData, List<FileData> fileDataList,
                                    Map<String, FileData> fileEnvMap) {
    if (CollectionUtils.isEmpty(fileDataList)) {
      return;
    }

    for (FileData childFileData : fileDataList) {
      FileData.Type childType = childFileData.getType();
      if (fileData != null) {
        if (childType != null && childType.equals(FileData.Type.HEAT_ENV)) {
          fileEnvMap.put(fileData.getFile(), childFileData);
        }
      }
      scanFileEnvMap(childFileData, childFileData.getData(), fileEnvMap);
    }
  }


  /**
   * Gets file type map.
   *
   * @param manifestContent the manifest content
   * @return the file type map
   */
  public static Map<String, FileData.Type> getFileTypeMap(ManifestContent manifestContent) {
    Map<String, FileData.Type> fileTypeMap = new HashMap<>();
    scanFileTypeMap(null, manifestContent.getData(), fileTypeMap);
    return fileTypeMap;
  }

  private static FileData.Type scanFileTypeMap(FileData fileData, List<FileData> data,
                                               Map<String, FileData.Type> fileTypeMap) {
    if (fileData != null) {
      fileTypeMap.put(fileData.getFile(), fileData.getType());
    }
    if (data == null) {
      return null;
    }

    for (FileData chileFileData : data) {
      FileData.Type type = scanFileTypeMap(chileFileData, chileFileData.getData(), fileTypeMap);
      if (type != null) {
        return type;
      }
    }
    return null;
  }


  /**
   * Gets artifacts.
   *
   * @param manifestContent the manifest content
   * @return the artifacts
   */
  public static Set<String> getArtifacts(ManifestContent manifestContent) {
    Set<String> artifacts = new HashSet<>();
    scanArtifacts(null, manifestContent.getData(), artifacts);

    return artifacts;
  }


  private static void scanArtifacts(FileData fileData, List<FileData> data, Set<String> artifacts) {
    if (fileData != null && fileData.getType() != null) {
      if (isArtifact(fileData)) {
        artifacts.add(fileData.getFile());
      }
    }

    if (data == null) {
      return;
    }

    for (FileData chileFileData : data) {
      scanArtifacts(chileFileData, chileFileData.getData(), artifacts);
    }
  }

  private static boolean isArtifact(FileData fileData) {
    if (FileData.Type.valueOf(fileData.getType().name()) != null
        && !fileData.getType().equals(FileData.Type.HEAT)
        && !fileData.getType().equals(FileData.Type.HEAT_ENV)
        && !fileData.getType().equals(FileData.Type.HEAT_NET)
        && !fileData.getType().equals(FileData.Type.HEAT_VOL)) {
      return true;
    }
    return false;
  }

  /**
   * Gets base files.
   *
   * @param manifestContent the manifest content
   * @return the base files
   */
  public static Set<String> getBaseFiles(ManifestContent manifestContent) {
    Set<String> baseFiles = new HashSet<>();
    scanBase(null, manifestContent.getData(), baseFiles);
    return baseFiles;
  }

  private static void scanBase(FileData fileData, List<FileData> data, Set<String> baseFiles) {
    if (fileData != null && fileData.getBase() != null) {
      if (fileData.getBase()) {
        baseFiles.add(fileData.getFile());
      }
    }
    if (data == null) {
      return;
    }

    for (FileData chileFileData : data) {
      scanBase(chileFileData, chileFileData.getData(), baseFiles);
    }
  }
}
