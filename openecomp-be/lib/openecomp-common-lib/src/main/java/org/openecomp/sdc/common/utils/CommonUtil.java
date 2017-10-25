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

package org.openecomp.sdc.common.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.slf4j.MDC;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CommonUtil {

  public static FileContentHandler validateAndUploadFileContent(OnboardingTypesEnum type,
                                                                byte[] uploadedFileData)
      throws IOException {
    return getFileContentMapFromOrchestrationCandidateZipAndValidateNoFolders(type, uploadedFileData);
  }

  /**
   * Gets files out of the zip AND validates zip is flat (no folders)
   *
   *
   * @param type
   * @param uploadFileData zip file
   * @return FileContentHandler if input is valid and has no folders
   */
  private static FileContentHandler getFileContentMapFromOrchestrationCandidateZipAndValidateNoFolders(
      OnboardingTypesEnum type, byte[] uploadFileData)
      throws IOException {
    Pair<FileContentHandler,List<String> > pair = getFileContentMapFromOrchestrationCandidateZip(uploadFileData);

    if(isFileOriginFromZip(type.toString())) {
      validateNoFolders(pair.getRight());
    }

    return pair.getLeft();
  }

  public static Pair<FileContentHandler,List<String> > getFileContentMapFromOrchestrationCandidateZip(
          byte[] uploadFileData)
          throws IOException {
    ZipEntry zipEntry;
    List<String> folderList = new ArrayList<>();
    FileContentHandler mapFileContent = new FileContentHandler();
     try ( ByteArrayInputStream in = new ByteArrayInputStream(uploadFileData);
          ZipInputStream inputZipStream = new ZipInputStream(in)){
      byte[] fileByteContent;
      String currentEntryName;

      while ((zipEntry = inputZipStream.getNextEntry()) != null) {
        currentEntryName = zipEntry.getName();
        // else, get the file content (as byte array) and save it in a map.
        fileByteContent = FileUtils.toByteArray(inputZipStream);

        int index = lastIndexFileSeparatorIndex(currentEntryName);
        if (index != -1) { //todo ?
          folderList.add(currentEntryName);
        }
        if(isFile(currentEntryName)) {
          mapFileContent.addFile(currentEntryName, fileByteContent);
        }
      }

    } catch (RuntimeException exception) {
      throw new IOException(exception);
    }

    return new ImmutablePair<>(mapFileContent,folderList);
  }

  private static boolean isFile(String currentEntryName) {
    return !(currentEntryName.endsWith("\\") || currentEntryName.endsWith("/"));
  }

  private static void validateNoFolders(List<String> folderList) {
    if (CollectionUtils.isNotEmpty(folderList)) {
      MDC.put(LoggerConstants.ERROR_DESCRIPTION, LoggerErrorDescription.INVALID_ZIP);
      throw new CoreException((new ErrorCode.ErrorCodeBuilder())
          .withMessage(Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage())
          .withId(Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage())
          .withCategory(ErrorCategory.APPLICATION).build());
    }
  }

  private static int lastIndexFileSeparatorIndex(String filePath) {
    int length = filePath.length() - 1;

    for (int i = length; i >= 0; i--) {
      char currChar = filePath.charAt(i);
      if (currChar == '/' || currChar == File.separatorChar || currChar == File.pathSeparatorChar) {
        return i;
      }
    }
    // if we've reached to the start of the string and didn't find file separator - return -1
    return -1;
  }

  private static boolean validateFilesExtensions(Set<String> allowedExtensions, FileContentHandler
      files) {
    for (String fileName : files.getFileList()) {
      if (!allowedExtensions.contains(FilenameUtils.getExtension(fileName))) {
        return false;
      }
    }
    return true;
  }

  public static boolean validateAllFilesYml(FileContentHandler files) {
    Set<String> allowedExtensions = new HashSet<>(Arrays.asList("yml", "yaml"));
    return validateFilesExtensions(allowedExtensions, files);
  }

  public static boolean isFileOriginFromZip(String fileOrigin){
   return Objects.nonNull(fileOrigin)
        && fileOrigin.toLowerCase().equals(OnboardingTypesEnum.ZIP.toString());
  }
}
