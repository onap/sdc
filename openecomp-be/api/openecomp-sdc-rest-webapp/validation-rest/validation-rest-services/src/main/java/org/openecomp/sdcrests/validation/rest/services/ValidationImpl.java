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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdcrests.validation.rest.services;

import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.validation.api.ValidationManager;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.zip.ZipUtils;
import org.openecomp.sdc.common.zip.exception.ZipException;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;
import org.openecomp.sdc.heat.services.tree.HeatTreeManager;
import org.openecomp.sdc.heat.services.tree.HeatTreeManagerUtil;
import org.openecomp.sdc.validation.UploadValidationManager;
import org.openecomp.sdc.validation.types.ValidationFileResponse;
import org.openecomp.sdc.validation.util.ValidationManagerUtil;
import org.openecomp.sdcrests.validation.rest.Validation;
import org.openecomp.sdcrests.validation.rest.mapping.MapValidationFileResponseToValidationFileResponseDto;
import org.openecomp.sdcrests.validation.types.ValidationFileResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Named;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Named
@Service("validation")
@Scope(value = "prototype")
public class ValidationImpl implements Validation {

    private final UploadValidationManager uploadValidationManager;

    @Autowired
    public ValidationImpl(@Qualifier("uploadValidationManager") UploadValidationManager uploadValidationManager) {
        this.uploadValidationManager = uploadValidationManager;
    }

    @Override
    public ResponseEntity validateFile(String type, MultipartFile fileToValidate) {
        ValidationFileResponse validationFileResponse;
        try {
            validationFileResponse = uploadValidationManager.validateFile(type, fileToValidate);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        ValidationFileResponseDto validationFileResponseDto = new MapValidationFileResponseToValidationFileResponseDto()
                .applyMapping(validationFileResponse, ValidationFileResponseDto.class);
        return ResponseEntity.ok(validationFileResponseDto);
    }


    public ValidationFileResponse validateInputFile(String type, MultipartFile fileToValidate) throws IOException {
        ValidationFileResponse validationFileResponse = new ValidationFileResponse();
        ValidationStructureList validationStructureList = new ValidationStructureList();

        if (type.equalsIgnoreCase("heat")) {
            // Convert MultipartFile to InputStream, which was previously used in the original code
            FileContentHandler content = this.getFileContent(fileToValidate.getInputStream());

            if (!content.containsFile("MANIFEST.json")) {
                throw new CoreException((new ErrorCode.ErrorCodeBuilder())
                        .withMessage(Messages.MANIFEST_NOT_EXIST.getErrorMessage())
                        .withId(Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage())
                        .withCategory(ErrorCategory.APPLICATION)
                        .build());
            } else {
                Map<String, List<ErrorMessage>> errors = this.validateHeatUploadData(content);
                HeatTreeManager tree = HeatTreeManagerUtil.initHeatTreeManager(content);
                tree.createTree();

                if (MapUtils.isNotEmpty(errors)) {
                    tree.addErrors(errors);
                    validationStructureList.setImportStructure(tree.getTree());
                }

                validationFileResponse.setValidationData(validationStructureList);
                return validationFileResponse;
            }
        } else {
            throw new RuntimeException("invalid type:" + type);
        }
    }

    private FileContentHandler getFileContent(InputStream is) throws IOException {
        return getFileContentMapFromZip(FileUtils.toByteArray(is));
    }

    private Map<String, List<ErrorMessage>> validateHeatUploadData(FileContentHandler fileContentMap) {
        ValidationManager validationManager = ValidationManagerUtil.initValidationManager(fileContentMap);
        return validationManager.validate();
    }

    private static FileContentHandler getFileContentMapFromZip(byte[] uploadFileData) throws IOException {
        Map<String, byte[]> zipFileAndByteMap;

        try {
            zipFileAndByteMap = ZipUtils.readZip(uploadFileData, true);
        } catch (ZipException e) {
            throw new IOException(e);
        }

        // Check if any value in the zipFileAndByteMap is null (indicating folders inside zip)
        boolean zipHasFolders = zipFileAndByteMap.values().stream().anyMatch(Objects::isNull);
        if (zipHasFolders) {
            throw new CoreException(
                    new ErrorCode.ErrorCodeBuilder()
                            .withMessage(Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage())
                            .withId(Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage())
                            .withCategory(ErrorCategory.APPLICATION)
                            .build()
            );
        }

        // Create and populate FileContentHandler by filtering out null entries
        FileContentHandler mapFileContent = new FileContentHandler();
        zipFileAndByteMap.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .forEach(entry -> mapFileContent.addFile(entry.getKey(), entry.getValue()));

        return mapFileContent;
    }
}
