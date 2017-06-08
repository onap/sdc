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

package org.openecomp.sdc.vendorsoftwareproduct.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.enrichment.types.ArtifactType;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.MibEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.OrchestrationTemplateActionResponse;
import org.slf4j.MDC;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VendorSoftwareProductUtils {
  protected static Logger logger =
      (Logger) LoggerFactory.getLogger(VendorSoftwareProductUtils.class);

  /**
   * Add file names to upload file response.
   *
   * @param fileContentMap     the file content map
   * @param uploadFileResponse the upload file response
   */
  public static void addFileNamesToUploadFileResponse(FileContentHandler fileContentMap,
                                                      OrchestrationTemplateActionResponse uploadFileResponse) {
    uploadFileResponse.setFileNames(new ArrayList<>());
    for (String filename : fileContentMap.getFileList()) {
      if (!new File(filename).isDirectory()) {
        uploadFileResponse.addNewFileToList(filename);
      }
    }
    uploadFileResponse.removeFileFromList(SdcCommon.MANIFEST_NAME);
  }

  /**
   * Validate raw zip data.
   *
   * @param uploadedFileData the uploaded file data
   * @param errors           the errors
   */
  public static void validateRawZipData(byte[] uploadedFileData,
                                        Map<String, List<ErrorMessage>> errors) {
    if (uploadedFileData.length == 0) {
      MDC.put(LoggerConstants.ERROR_DESCRIPTION, LoggerErrorDescription.INVALID_ZIP);
      ErrorMessage.ErrorMessageUtil.addMessage(SdcCommon.UPLOAD_FILE, errors).add(
          new ErrorMessage(ErrorLevel.ERROR,
              Messages.NO_ZIP_FILE_WAS_UPLOADED_OR_ZIP_NOT_EXIST.getErrorMessage()));
    }
  }

  /**
   * Validate content zip data.
   *
   * @param contentMap the content map
   * @param errors     the errors
   */
  public static void validateContentZipData(FileContentHandler contentMap,
                                            Map<String, List<ErrorMessage>> errors) {
    MDC.put(LoggerConstants.ERROR_DESCRIPTION, LoggerErrorDescription.INVALID_ZIP);
    if (contentMap == null) {
      ErrorMessage.ErrorMessageUtil.addMessage(SdcCommon.UPLOAD_FILE, errors).add(
          new ErrorMessage(ErrorLevel.ERROR,
              Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage()));

    } else if (contentMap.getFileList().size() == 0) {
      ErrorMessage.ErrorMessageUtil.addMessage(SdcCommon.UPLOAD_FILE, errors)
          .add(new ErrorMessage(ErrorLevel.ERROR, Messages.INVALID_ZIP_FILE.getErrorMessage()));
    }
  }


  /**
   * Filter non trap or poll artifacts map.
   *
   * @param artifacts the artifacts
   * @return the map
   */
  public static Map<ArtifactType, String> filterNonTrapOrPollArtifacts(
      Collection<MibEntity> artifacts) {
    Map<ArtifactType, String> artifactTypeToFilename = new HashMap<>();

    for (MibEntity entity : artifacts) {
      if (isTrapOrPoll(entity.getType())) {
        artifactTypeToFilename.put(entity.getType(), entity.getArtifactName());
      }
    }

    return artifactTypeToFilename;
  }


  private static boolean isTrapOrPoll(ArtifactType type) {
    return type.equals(ArtifactType.SNMP_POLL) || type.equals(ArtifactType.SNMP_TRAP);
  }


  /**
   * Sets errors into logger.
   *
   * @param errors            the errors
   * @param serviceName       the service name
   * @param targetServiceName the target service name
   */
  public static void setErrorsIntoLogger(Map<String, List<ErrorMessage>> errors,
                                         LoggerServiceName serviceName, String targetServiceName) {
    MdcDataErrorMessage mdcDataErrorMessage =
        new MdcDataErrorMessage(targetServiceName, LoggerConstants.TARGET_ENTITY_DB,
            ErrorLevel.ERROR.name(), null, null);
    mdcDataErrorMessage.setMdcValues();

    if (MapUtils.isEmpty(errors)) {
      return;
    }

    for (Map.Entry<String, List<ErrorMessage>> listEntry : errors.entrySet()) {
      List<ErrorMessage> errorList = listEntry.getValue();
      for (ErrorMessage message : errorList) {
        logger.error(message.getMessage());
      }
    }
  }

  /**
   * Sets errors into logger.
   *
   * @param errors            the errors
   * @param serviceName       the service name
   * @param targetServiceName the target service name
   */
  public static void setErrorsIntoLogger(Collection<ErrorCode> errors,
                                         LoggerServiceName serviceName, String targetServiceName) {
    MdcDataErrorMessage mdcDataErrorMessage =
        new MdcDataErrorMessage(targetServiceName, LoggerConstants.TARGET_ENTITY_DB,
            ErrorLevel.ERROR.name(), null, null);
    mdcDataErrorMessage.setMdcValues();

    if (CollectionUtils.isEmpty(errors)) {
      return;
    }

    for (ErrorCode error : errors) {
      logger.error(error.message());
    }
  }

}
