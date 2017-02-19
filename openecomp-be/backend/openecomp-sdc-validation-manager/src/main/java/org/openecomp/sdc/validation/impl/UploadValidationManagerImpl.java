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

package org.openecomp.sdc.validation.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.validation.api.ValidationManager;
import org.openecomp.core.validation.errors.Messages;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.utils.AsdcCommon;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;
import org.openecomp.sdc.heat.services.tree.HeatTreeManager;
import org.openecomp.sdc.heat.services.tree.HeatTreeManagerUtil;
import org.openecomp.sdc.validation.UploadValidationManager;
import org.openecomp.sdc.validation.types.ValidationFileResponse;
import org.openecomp.sdc.validation.utils.ValidationManagerUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UploadValidationManagerImpl implements UploadValidationManager {

  private static FileContentHandler getFileContentMapFromZip(byte[] uploadFileData)
      throws IOException, CoreException {
    ZipEntry zipEntry;
    List<String> folderList = new ArrayList<>();
    FileContentHandler mapFileContent = new FileContentHandler();
    try {
      ZipInputStream inputZipStream;

      byte[] fileByteContent;
      String currentEntryName;
      inputZipStream = new ZipInputStream(new ByteArrayInputStream(uploadFileData));

      while ((zipEntry = inputZipStream.getNextEntry()) != null) {
        currentEntryName = zipEntry.getName();
        // else, get the file content (as byte array) and save it in a map.
        fileByteContent = FileUtils.toByteArray(inputZipStream);

        int index = lastIndexFileSeparatorIndex(currentEntryName);
        String currSubstringWithoutSeparator =
            currentEntryName.substring(index + 1, currentEntryName.length());
        if (index != -1) {
          if (currSubstringWithoutSeparator.length() > 0) {
            mapFileContent.addFile(currentEntryName.substring(index + 1, currentEntryName.length()),
                fileByteContent);
          } else {
            folderList.add(currentEntryName);
          }
        } else {
          mapFileContent.addFile(currentEntryName, fileByteContent);
        }
      }
    } catch (RuntimeException exception) {
      throw new IOException(exception);
    }

    if (CollectionUtils.isNotEmpty(folderList)) {
      throw new CoreException((new ErrorCode.ErrorCodeBuilder())
          .withMessage(Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage())
          .withId(Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage())
          .withCategory(ErrorCategory.APPLICATION).build());

    }

    return mapFileContent;
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

  @Override
  public ValidationFileResponse validateFile(String type, InputStream fileToValidate)
      throws IOException {

    ValidationFileResponse validationFileResponse = new ValidationFileResponse();

    HeatTreeManager tree;
    ValidationStructureList validationStructureList = new ValidationStructureList();
    if (type.toLowerCase().equals("heat")) {
      FileContentHandler content = getFileContent(fileToValidate);
      if (!content.containsFile(AsdcCommon.MANIFEST_NAME)) {
        throw new CoreException((new ErrorCode.ErrorCodeBuilder())
            .withMessage(Messages.MANIFEST_NOT_EXIST.getErrorMessage())
            .withId(Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage())
            .withCategory(ErrorCategory.APPLICATION).build());
      }
      Map<String, List<org.openecomp.sdc.datatypes.error.ErrorMessage>> errors =
          validateHeatUploadData(content);
      tree = HeatTreeManagerUtil.initHeatTreeManager(content);
      tree.createTree();
      if (MapUtils.isNotEmpty(errors)) {


        tree.addErrors(errors);
        validationStructureList.setImportStructure(tree.getTree());
        //validationFileResponse.setStatus(ValidationFileStatus.Failure);
      } else {
        //validationFileResponse.setStatus(ValidationFileStatus.Success);
      }
    } else {
      throw new RuntimeException("invalid type:" + type);
    }
    validationFileResponse.setValidationData(validationStructureList);
    return validationFileResponse;
  }

  private Map<String, List<org.openecomp.sdc.datatypes.error.ErrorMessage>> validateHeatUploadData(
      FileContentHandler fileContentMap)
      throws IOException {
    ValidationManager validationManager =
        ValidationManagerUtil.initValidationManager(fileContentMap);
    return validationManager.validate();
  }

  private FileContentHandler getFileContent(InputStream is) throws IOException {
    return getFileContentMapFromZip(FileUtils.toByteArray(is));


  }

}
