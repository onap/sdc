/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */
package org.openecomp.sdc.vendorsoftwareproduct.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.enrichment.types.MonitoringUploadType;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentMonitoringUploadEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.OrchestrationTemplateActionResponse;

public class VendorSoftwareProductUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(VendorSoftwareProductUtils.class);

    private VendorSoftwareProductUtils() {
    }

    /**
     * Add file names to upload file response.
     *
     * @param fileContentMap     the file content map
     * @param uploadFileResponse the upload file response
     */
    public static void addFileNamesToUploadFileResponse(FileContentHandler fileContentMap, OrchestrationTemplateActionResponse uploadFileResponse) {
        uploadFileResponse.setFileNames(new ArrayList<>());
        for (String filename : fileContentMap.getFileList()) {
            if (!new File(filename).isDirectory()) {
                uploadFileResponse.addNewFileToList(filename);
            }
        }
        uploadFileResponse.removeFileFromList(SdcCommon.MANIFEST_NAME);
    }

    /**
     * Validate content zip data.
     *
     * @param contentMap the content map
     * @param errors     the errors
     */
    public static void validateContentZipData(FileContentHandler contentMap, Map<String, List<ErrorMessage>> errors) {
        if (contentMap.getFileList().isEmpty()) {
            ErrorMessage.ErrorMessageUtil.addMessage(SdcCommon.UPLOAD_FILE, errors)
                .add(new ErrorMessage(ErrorLevel.ERROR, Messages.INVALID_ZIP_FILE.getErrorMessage()));
        }
    }

    /**
     * Maps all artifacts by type.
     *
     * @param artifacts the artifacts
     * @return the map
     */
    public static Map<MonitoringUploadType, String> mapArtifactsByType(Collection<ComponentMonitoringUploadEntity> artifacts) {
        Map<MonitoringUploadType, String> artifactTypeToFilename = new EnumMap<>(MonitoringUploadType.class);
        for (ComponentMonitoringUploadEntity entity : artifacts) {
            artifactTypeToFilename.put(entity.getType(), entity.getArtifactName());
        }
        return artifactTypeToFilename;
    }

    /**
     * Sets errors into logger.
     *
     * @param errors the errors
     */
    public static void setErrorsIntoLogger(Map<String, List<ErrorMessage>> errors) {
        if (MapUtils.isEmpty(errors)) {
            return;
        }
        for (Map.Entry<String, List<ErrorMessage>> listEntry : errors.entrySet()) {
            List<ErrorMessage> errorList = listEntry.getValue();
            for (ErrorMessage message : errorList) {
                LOGGER.error(message.getMessage());
            }
        }
    }

    /**
     * Sets errors into logger.
     *
     * @param errors the errors
     */
    public static void setErrorsIntoLogger(Collection<ErrorCode> errors) {
        if (CollectionUtils.isEmpty(errors)) {
            return;
        }
        for (ErrorCode error : errors) {
            LOGGER.error(error.message());
        }
    }
}
