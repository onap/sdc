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
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;
import org.openecomp.sdc.heat.services.tree.HeatTreeManager;
import org.openecomp.sdc.heat.services.tree.HeatTreeManagerUtil;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.validation.UploadValidationManager;
import org.openecomp.sdc.validation.types.ValidationFileResponse;
import org.openecomp.sdc.validation.util.ValidationManagerUtil;
import org.slf4j.MDC;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * Created by TALIO on 4/20/2016.
 */
public class UploadValidationManagerImpl implements UploadValidationManager {

  private static final MdcDataDebugMessage MDC_DATA_DEBUG_MESSAGE = new MdcDataDebugMessage();


  private static FileContentHandler getFileContentMapFromZip(byte[] uploadFileData)
      throws IOException, CoreException {

    ZipEntry zipEntry;
    List<String> folderList = new ArrayList<>();
    FileContentHandler mapFileContent = new FileContentHandler();
    try (ZipInputStream inputZipStream = new ZipInputStream(new ByteArrayInputStream(uploadFileData))) {

      byte[] fileByteContent;
      String currentEntryName;

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
      MDC.put(LoggerConstants.ERROR_DESCRIPTION, LoggerErrorDescription.INVALID_ZIP);
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


    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage(null, (String[]) null);

    ValidationFileResponse validationFileResponse = new ValidationFileResponse();

    HeatTreeManager tree;
    ValidationStructureList validationStructureList = new ValidationStructureList();
    if (type.toLowerCase().equals("heat")) {
      FileContentHandler content = getFileContent(fileToValidate);
      if (!content.containsFile(SdcCommon.MANIFEST_NAME)) {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
            LoggerTragetServiceName.VALIDATE_MANIFEST_CONTENT, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_ZIP);
        throw new CoreException((new ErrorCode.ErrorCodeBuilder())
            .withMessage(Messages.MANIFEST_NOT_EXIST.getErrorMessage())
            .withId(Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage())
            .withCategory(ErrorCategory.APPLICATION).build());
      }
      Map<String, List<ErrorMessage>> errors = validateHeatUploadData(content);
      tree = HeatTreeManagerUtil.initHeatTreeManager(content);
      tree.createTree();

      if (MapUtils.isNotEmpty(errors)) {
        tree.addErrors(errors);
        validationStructureList.setImportStructure(tree.getTree());
      }

    } else {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
          LoggerTragetServiceName.VALIDATE_FILE_TYPE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.INVALID_FILE_TYPE);
      throw new RuntimeException("invalid type:" + type);
    }
    validationFileResponse.setValidationData(validationStructureList);

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage(null, (String[]) null);
    return validationFileResponse;
  }

  private Map<String, List<ErrorMessage>> validateHeatUploadData(FileContentHandler fileContentMap) {
    ValidationManager validationManager =
        ValidationManagerUtil.initValidationManager(fileContentMap);
    return validationManager.validate();
  }

  private FileContentHandler getFileContent(InputStream is) throws IOException {
    return getFileContentMapFromZip(FileUtils.toByteArray(is));


  }

}
