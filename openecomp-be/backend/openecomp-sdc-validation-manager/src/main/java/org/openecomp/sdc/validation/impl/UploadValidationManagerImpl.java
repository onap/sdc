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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.validation.api.ValidationManager;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.common.zip.ZipUtils;
import org.openecomp.sdc.common.zip.exception.ZipException;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;
import org.openecomp.sdc.heat.services.tree.HeatTreeManager;
import org.openecomp.sdc.heat.services.tree.HeatTreeManagerUtil;
import org.openecomp.sdc.validation.UploadValidationManager;
import org.openecomp.sdc.validation.types.ValidationFileResponse;
import org.openecomp.sdc.validation.util.ValidationManagerUtil;

/**
 * Created by TALIO on 4/20/2016.
 */
public class UploadValidationManagerImpl implements UploadValidationManager {

    private static FileContentHandler getFileContentMapFromZip(byte[] uploadFileData) throws IOException {
        final Map<String, byte[]> zipFileAndByteMap;
        try {
            zipFileAndByteMap = ZipUtils.readZip(uploadFileData, true);
        } catch (final ZipException e) {
            throw new IOException(e);
        }
        final boolean zipHasFolders = zipFileAndByteMap.values().stream().anyMatch(Objects::isNull);
        if (zipHasFolders) {
            throw new CoreException((new ErrorCode.ErrorCodeBuilder()).withMessage(Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage())
                .withId(Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage()).withCategory(ErrorCategory.APPLICATION).build());
        }
        final FileContentHandler mapFileContent = new FileContentHandler();
        zipFileAndByteMap.entrySet().stream().filter(entry -> entry.getValue() != null)
            .forEach(zipEntry -> mapFileContent.addFile(zipEntry.getKey(), zipEntry.getValue()));
        return mapFileContent;
    }

    @Override
    public ValidationFileResponse validateFile(String type, InputStream fileToValidate) throws IOException {
        ValidationFileResponse validationFileResponse = new ValidationFileResponse();
        HeatTreeManager tree;
        ValidationStructureList validationStructureList = new ValidationStructureList();
        if (type.equalsIgnoreCase("heat")) {
            FileContentHandler content = getFileContent(fileToValidate);
            if (!content.containsFile(SdcCommon.MANIFEST_NAME)) {
                throw new CoreException((new ErrorCode.ErrorCodeBuilder()).withMessage(Messages.MANIFEST_NOT_EXIST.getErrorMessage())
                    .withId(Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage()).withCategory(ErrorCategory.APPLICATION).build());
            }
            Map<String, List<ErrorMessage>> errors = validateHeatUploadData(content);
            tree = HeatTreeManagerUtil.initHeatTreeManager(content);
            tree.createTree();
            if (MapUtils.isNotEmpty(errors)) {
                tree.addErrors(errors);
                validationStructureList.setImportStructure(tree.getTree());
            }
        } else {
            throw new RuntimeException("invalid type:" + type);
        }
        validationFileResponse.setValidationData(validationStructureList);
        return validationFileResponse;
    }

    private Map<String, List<ErrorMessage>> validateHeatUploadData(FileContentHandler fileContentMap) {
        ValidationManager validationManager = ValidationManagerUtil.initValidationManager(fileContentMap);
        return validationManager.validate();
    }

    private FileContentHandler getFileContent(InputStream is) throws IOException {
        return getFileContentMapFromZip(FileUtils.toByteArray(is));
    }
}
