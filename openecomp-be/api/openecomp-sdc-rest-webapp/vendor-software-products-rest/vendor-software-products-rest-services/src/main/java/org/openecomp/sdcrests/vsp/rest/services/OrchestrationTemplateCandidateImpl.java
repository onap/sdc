/*
 * Copyright © 2016-2018 European Support Limited
 * Copyright © 2021 Nokia
 * Copyright © 2021 Nordix Foundation
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
 * ============LICENSE_END=========================================================
 * Modifications copyright (c) 2019 Nokia
 * Modifications copyright (c) 2021 Nordix Foundation
 * ================================================================================
 */
package org.openecomp.sdcrests.vsp.rest.services;


import static org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder.getErrorWithParameters;
import static org.openecomp.sdc.common.errors.Messages.ERROR_HAS_OCCURRED_WHILE_PERSISTING_THE_ARTIFACT;
import static org.openecomp.sdc.common.errors.Messages.ERROR_HAS_OCCURRED_WHILE_REDUCING_THE_ARTIFACT_SIZE;
import static org.openecomp.sdc.common.errors.Messages.NO_FILE_WAS_UPLOADED_OR_FILE_NOT_EXIST;
import static org.openecomp.sdc.common.errors.Messages.PACKAGE_PROCESS_ERROR;
import static org.openecomp.sdc.common.errors.Messages.PACKAGE_REDUCER_NOT_CONFIGURED;
import static org.openecomp.sdc.common.errors.Messages.UNEXPECTED_PROBLEM_HAPPENED_WHILE_GETTING;
import static org.openecomp.sdcrests.vsp.rest.exception.OrchestrationTemplateCandidateUploadManagerExceptionSupplier.vspUploadAlreadyInProgress;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Named;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.activitylog.ActivityLogManagerFactory;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.activitylog.dao.type.ActivityType;
import org.openecomp.sdc.be.csar.storage.ArtifactInfo;
import org.openecomp.sdc.be.csar.storage.ArtifactStorageConfig;
import org.openecomp.sdc.be.csar.storage.ArtifactStorageManager;
import org.openecomp.sdc.be.csar.storage.PackageSizeReducer;
import org.openecomp.sdc.be.csar.storage.StorageFactory;
import org.openecomp.sdc.be.csar.storage.exception.ArtifactStorageException;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManager;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.VspManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatus;
import org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.OnboardingPackageProcessor;
import org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.validation.CnfPackageValidator;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.types.OrchestrationTemplateActionResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.FileDataStructureDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.OrchestrationTemplateActionResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.UploadFileResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ValidationResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspUploadStatusDto;
import org.openecomp.sdcrests.vsp.rest.OrchestrationTemplateCandidate;
import org.openecomp.sdcrests.vsp.rest.mapping.MapFilesDataStructureToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapUploadFileResponseToUploadFileResponseDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapValidationResponseToDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Named
@Service("orchestrationTemplateCandidate")
@Scope(value = "prototype")
public class OrchestrationTemplateCandidateImpl implements OrchestrationTemplateCandidate {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrchestrationTemplateCandidateImpl.class);
    private final OrchestrationTemplateCandidateManager candidateManager;
    private final VendorSoftwareProductManager vendorSoftwareProductManager;
    private final ActivityLogManager activityLogManager;
    private final OrchestrationTemplateCandidateUploadManager orchestrationTemplateCandidateUploadManager;
    private final StorageFactory storageFactory;

    @Autowired
    public OrchestrationTemplateCandidateImpl(final OrchestrationTemplateCandidateUploadManager orchestrationTemplateCandidateUploadManager) {
        this.candidateManager = OrchestrationTemplateCandidateManagerFactory.getInstance().createInterface();
        this.vendorSoftwareProductManager = VspManagerFactory.getInstance().createInterface();
        this.activityLogManager = ActivityLogManagerFactory.getInstance().createInterface();
        this.storageFactory = new StorageFactory();
        this.orchestrationTemplateCandidateUploadManager = orchestrationTemplateCandidateUploadManager;
    }

    // Constructor used in test to avoid mock static
    public OrchestrationTemplateCandidateImpl(final OrchestrationTemplateCandidateManager candidateManager,
                                              final VendorSoftwareProductManager vendorSoftwareProductManager,
                                              final ActivityLogManager activityLogManager,
                                              final OrchestrationTemplateCandidateUploadManager orchestrationTemplateCandidateUploadManager,
                                              final StorageFactory storageFactory) {
        this.candidateManager = candidateManager;
        this.vendorSoftwareProductManager = vendorSoftwareProductManager;
        this.activityLogManager = activityLogManager;
        this.storageFactory = storageFactory;
        this.orchestrationTemplateCandidateUploadManager = orchestrationTemplateCandidateUploadManager;
    }

    @Override
    public ResponseEntity upload(String vspId,
                                String versionId,
                                MultipartFile fileToUpload,
                                String user) {
        LOGGER.debug("STARTED -> OrchestrationTemplateCandidateImpl.upload");

        vspId = ValidationUtils.sanitizeInputString(vspId);
        versionId = ValidationUtils.sanitizeInputString(versionId);

        final ResponseEntity response;
        VspUploadStatusDto vspUploadStatus = null;

        try {
            vspUploadStatus = getVspUploadStatus(vspId, versionId, user);

            if (vspUploadStatus.getStatus() != VspUploadStatus.UPLOADING) {
                throw vspUploadAlreadyInProgress(vspId, versionId).get();
            }

            final byte[] fileToUploadBytes;
            final var filename = ValidationUtils.sanitizeInputString(fileToUpload.getOriginalFilename());
            ArtifactInfo artifactInfo = null;

            final ArtifactStorageManager artifactStorageManager = storageFactory.createArtifactStorageManager();

            try {
                if (artifactStorageManager.isEnabled()) {
                    try (InputStream inputStream = fileToUpload.getInputStream()) {
                        artifactInfo = handleArtifactStorage(vspId, versionId, filename, inputStream, artifactStorageManager);
                        fileToUploadBytes = artifactInfo.getBytes();
                    }
                } else {
                    fileToUploadBytes = fileToUpload.getBytes();
                }
            } catch (IOException ioEx) {
                throw new RuntimeException("Error reading uploaded file", ioEx);
            }

            vspUploadStatus = orchestrationTemplateCandidateUploadManager.putUploadInValidation(vspId, versionId, user);

            final var onboardingPackageProcessor =
                new OnboardingPackageProcessor(filename, fileToUploadBytes, new CnfPackageValidator(), artifactInfo);

            final ErrorMessage[] errorMessages = onboardingPackageProcessor.getErrorMessages().toArray(new ErrorMessage[0]);

            if (onboardingPackageProcessor.hasErrors()) {
                orchestrationTemplateCandidateUploadManager
                    .putUploadAsFinished(vspId, versionId, vspUploadStatus.getLockId(), VspUploadStatus.ERROR, user);

                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                                    .body(buildUploadResponseWithError(errorMessages));
            }

            final var onboardPackageInfo = onboardingPackageProcessor.getOnboardPackageInfo().orElse(null);

            if (onboardPackageInfo == null) {
                final UploadFileResponseDto uploadFileResponseDto = buildUploadResponseWithError(
                    new ErrorMessage(ErrorLevel.ERROR, PACKAGE_PROCESS_ERROR.formatMessage(filename)));

                orchestrationTemplateCandidateUploadManager
                    .putUploadAsFinished(vspId, versionId, vspUploadStatus.getLockId(), VspUploadStatus.ERROR, user);

                return ResponseEntity.ok(uploadFileResponseDto);
            }

            final var version = new Version(versionId);
            final var vspDetails = vendorSoftwareProductManager.getVsp(vspId, version);

            vspUploadStatus = orchestrationTemplateCandidateUploadManager.putUploadInProcessing(vspId, versionId, user);
            response = processOnboardPackage(onboardPackageInfo, vspDetails, errorMessages);

            final UploadFileResponseDto entity = (UploadFileResponseDto) response.getBody();
            final Map<String, List<ErrorMessage>> errors = entity.getErrors();

            if (MapUtils.isNotEmpty(errors)) {
                if (artifactStorageManager.isEnabled()) {
                    artifactStorageManager.delete(artifactInfo);
                }

                orchestrationTemplateCandidateUploadManager
                    .putUploadAsFinished(vspId, versionId, vspUploadStatus.getLockId(), VspUploadStatus.ERROR, user);

                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                                    .body(buildUploadResponseWithError(
                                        errors.values().stream().flatMap(List::stream).toArray(ErrorMessage[]::new)));
            }

            if (artifactStorageManager.isEnabled()) {
                artifactStorageManager.put(vspId, versionId + ".reduced", new ByteArrayInputStream(fileToUploadBytes));
            }

            orchestrationTemplateCandidateUploadManager
                .putUploadAsFinished(vspId, versionId, vspUploadStatus.getLockId(), VspUploadStatus.SUCCESS, user);

        } catch (final Exception ex) {
            try {
                if (vspUploadStatus == null) {
                    vspUploadStatus = orchestrationTemplateCandidateUploadManager
                        .findLatestStatus(vspId, versionId, user)
                        .orElse(null);
                }

                if (vspUploadStatus != null && vspUploadStatus.getLockId() != null) {
                    orchestrationTemplateCandidateUploadManager
                        .putUploadAsFinished(vspId, versionId, vspUploadStatus.getLockId(), VspUploadStatus.ERROR, user);
                } else {
                    LOGGER.warn("Upload status or lockId is null. Skipping putUploadAsFinished.");
                }

            } catch (Exception cleanupEx) {
                LOGGER.error("Error while calling putUploadAsFinished in exception handler", cleanupEx);
            }

            throw (ex instanceof RuntimeException) ? (RuntimeException) ex
                                                : new RuntimeException("Error occurred during upload", ex);
        }

        LOGGER.debug("FINISHED -> OrchestrationTemplateCandidateImpl.upload");
        return response;
    }

    private VspUploadStatusDto getVspUploadStatus(final String vspId, final String versionId, final String user) {
        final Optional<VspUploadStatusDto> vspUploadStatusOpt =
            orchestrationTemplateCandidateUploadManager.findLatestStatus(vspId, versionId, user);
        if (vspUploadStatusOpt.isEmpty() || vspUploadStatusOpt.get().isComplete()) {
            return orchestrationTemplateCandidateUploadManager.putUploadInProgress(vspId, versionId, user);
        }

        return vspUploadStatusOpt.get();
    }

    private ArtifactInfo handleArtifactStorage(final String vspId,
                                           final String versionId,
                                           final String filename,
                                           final InputStream inputStream,
                                           final ArtifactStorageManager artifactStorageManager) {
        final PackageSizeReducer packageSizeReducer = storageFactory.createPackageSizeReducer().orElse(null);
        if (packageSizeReducer == null) {
            throw new ArtifactStorageException(PACKAGE_REDUCER_NOT_CONFIGURED.getErrorMessage());
        }

        Path tempArtifactPath = null;
        try {
            final ArtifactStorageConfig storageConfiguration = artifactStorageManager.getStorageConfiguration();
            final Path folder = Path.of(storageConfiguration.getTempPath()).resolve(vspId).resolve(versionId);
            Files.createDirectories(folder);

            tempArtifactPath = folder.resolve(UUID.randomUUID().toString());
            LOGGER.debug("STARTED -> Transfer to '{}'", tempArtifactPath);

            // Write the received InputStream to a temp file
            try (var outputStream = new FileOutputStream(tempArtifactPath.toFile())) {
                inputStream.transferTo(outputStream);
            }

            LOGGER.debug("FINISHED -> Transfer to '{}'", tempArtifactPath);
        } catch (final Exception e) {
            deleteTempFile(tempArtifactPath);
            LOGGER.error("Error transferring input stream", e);
            throw new ArtifactStorageException(UNEXPECTED_PROBLEM_HAPPENED_WHILE_GETTING.formatMessage(filename));
        }

        ArtifactInfo artifactInfo;
        try (final InputStream artifactInputStream = new FileInputStream(tempArtifactPath.toFile())) {
            artifactInfo = artifactStorageManager.upload(vspId, versionId, artifactInputStream);
        } catch (final Exception e) {
            deleteTempFile(tempArtifactPath);
            LOGGER.error("Failed to upload artifact", e);
            throw new ArtifactStorageException(ERROR_HAS_OCCURRED_WHILE_PERSISTING_THE_ARTIFACT.formatMessage(filename));
        }

        try {
            LOGGER.debug("STARTED -> reducing '{}'", tempArtifactPath);
            artifactInfo.setBytes(packageSizeReducer.reduce(tempArtifactPath));
            LOGGER.debug("FINISHED -> reducing '{}'", tempArtifactPath);
        } catch (final Exception e) {
            deleteTempFile(tempArtifactPath);
            LOGGER.debug("ERROR -> reducing '{}'", tempArtifactPath, e);
            throw new ArtifactStorageException(ERROR_HAS_OCCURRED_WHILE_REDUCING_THE_ARTIFACT_SIZE.formatMessage(filename), e);
        }

        deleteTempFile(tempArtifactPath);
        return artifactInfo;
    }

    private void deleteTempFile(final Path tempArtifactPath) {
        if (Files.exists(tempArtifactPath)) {
            try {
                Files.delete(tempArtifactPath);
            } catch (final Exception e) {
                LOGGER.warn("Could not delete temporary package at '{}'", tempArtifactPath, e);
            }
        }
    }

    private ResponseEntity processOnboardPackage(final OnboardPackageInfo onboardPackageInfo, final VspDetails vspDetails,
                                           final ErrorMessage... errorMessages) {
        final UploadFileResponse uploadFileResponse = candidateManager.upload(vspDetails, onboardPackageInfo);
        final UploadFileResponseDto uploadFileResponseDto = new MapUploadFileResponseToUploadFileResponseDto()
            .applyMapping(uploadFileResponse, UploadFileResponseDto.class);
        if (errorMessages.length > 0) {
            uploadFileResponseDto.setErrors(getErrorMap(errorMessages));
        }
        //return Response.ok(uploadFileResponseDto).build();
        return ResponseEntity.ok().body(uploadFileResponseDto);
    }

    private Map<String, List<ErrorMessage>> getErrorMap(ErrorMessage[] errorMessages) {
        final Map<String, List<ErrorMessage>> errorMap = new HashMap<>();
        final List<ErrorMessage> errorMessageList = new ArrayList<>();
        Collections.addAll(errorMessageList, errorMessages);
        errorMap.put(SdcCommon.UPLOAD_FILE, errorMessageList);
        return errorMap;
    }

    private UploadFileResponseDto buildUploadResponseWithError(final ErrorMessage... errorMessages) {
        final UploadFileResponseDto uploadFileResponseDto = new UploadFileResponseDto();
        uploadFileResponseDto.setErrors(getErrorMap(errorMessages));
        return uploadFileResponseDto;
    }

    @Override
    public ResponseEntity get(String vspId, String versionId, String user) throws IOException {
        Optional<Pair<String, byte[]>> zipFile = candidateManager.get(vspId, new Version(versionId));
        String fileName;
        if (zipFile.isPresent()) {
            fileName = "Candidate." + zipFile.get().getLeft();
        } else {
            zipFile = vendorSoftwareProductManager.get(vspId, new Version((versionId)));
            if (zipFile.isEmpty()) {
                ErrorMessage errorMessage = new ErrorMessage(ErrorLevel.ERROR,
                    getErrorWithParameters(NO_FILE_WAS_UPLOADED_OR_FILE_NOT_EXIST.getErrorMessage(), ""));
                LOGGER.error(errorMessage.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            fileName = "Processed." + zipFile.get().getLeft();
        }
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + fileName)
                .body(zipFile.get().getRight());
        //return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity abort(String vspId, String versionId) {
        candidateManager.abort(vspId, new Version(versionId));
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity process(String vspId, String versionId, String user) {
        Version version = new Version(versionId);
        OrchestrationTemplateActionResponse response = candidateManager.process(vspId, version);
        activityLogManager.logActivity(new ActivityLogEntity(vspId, version, ActivityType.Upload_Network_Package, user, true, "", ""));
        OrchestrationTemplateActionResponseDto responseDto = copyOrchestrationTemplateActionResponseToDto(response);
        return ResponseEntity.ok(responseDto);
    }

    @Override
    public ResponseEntity updateFilesDataStructure(String vspId, String versionId, FileDataStructureDto fileDataStructureDto, String user) {
        FilesDataStructure fileDataStructure = copyFilesDataStructureDtoToFilesDataStructure(fileDataStructureDto);
        ValidationResponse response = candidateManager.updateFilesDataStructure(vspId, new Version(versionId), fileDataStructure);
        if (!response.isValid()) {
//            return Response.status(EXPECTATION_FAILED)
//                .entity(new MapValidationResponseToDto().applyMapping(response, ValidationResponseDto.class))
//                .build();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body(new MapValidationResponseToDto().applyMapping(response, ValidationResponseDto.class));

        }
        return ResponseEntity.ok(fileDataStructureDto);
    }

    @Override
    public ResponseEntity getFilesDataStructure(String vspId, String versionId, String user) {
        Optional<FilesDataStructure> filesDataStructure = candidateManager.getFilesDataStructure(vspId, new Version(versionId));
        if (filesDataStructure.isEmpty()) {
            filesDataStructure = vendorSoftwareProductManager.getOrchestrationTemplateStructure(vspId, new Version(versionId));
        }
        FileDataStructureDto fileDataStructureDto = filesDataStructure
            .map(dataStructure -> new MapFilesDataStructureToDto().applyMapping(dataStructure, FileDataStructureDto.class))
            .orElse(new FileDataStructureDto());
        return ResponseEntity.ok().body(fileDataStructureDto);
    }

    private OrchestrationTemplateActionResponseDto copyOrchestrationTemplateActionResponseToDto(OrchestrationTemplateActionResponse response) {
        OrchestrationTemplateActionResponseDto result = new OrchestrationTemplateActionResponseDto();
        result.setErrors(response.getErrors());
        result.setFileNames(response.getFileNames());
        result.setStatus(response.getStatus());
        return result;
    }

    private FilesDataStructure copyFilesDataStructureDtoToFilesDataStructure(FileDataStructureDto fileDataStructureDto) {
        FilesDataStructure filesDataStructure = new FilesDataStructure();
        filesDataStructure.setArtifacts(fileDataStructureDto.getArtifacts());
        filesDataStructure.setModules(fileDataStructureDto.getModules());
        filesDataStructure.setNested(fileDataStructureDto.getNested());
        filesDataStructure.setUnassigned(fileDataStructureDto.getUnassigned());
        return filesDataStructure;
    }
}
