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

import static javax.ws.rs.core.Response.Status.EXPECTATION_FAILED;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder.getErrorWithParameters;
import static org.openecomp.sdc.common.errors.Messages.ERROR_HAS_OCCURRED_WHILE_PERSISTING_THE_ARTIFACT;
import static org.openecomp.sdc.common.errors.Messages.ERROR_HAS_OCCURRED_WHILE_REDUCING_THE_ARTIFACT_SIZE;
import static org.openecomp.sdc.common.errors.Messages.NO_FILE_WAS_UPLOADED_OR_FILE_NOT_EXIST;
import static org.openecomp.sdc.common.errors.Messages.PACKAGE_PROCESS_ERROR;
import static org.openecomp.sdc.common.errors.Messages.UNEXPECTED_PROBLEM_HAPPENED_WHILE_GETTING;

import java.io.ByteArrayInputStream;
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
import javax.activation.DataHandler;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
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
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspUploadStatusType;
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
import org.springframework.stereotype.Service;

@Named
@Service("orchestrationTemplateCandidate")
@Scope(value = "prototype")
public class OrchestrationTemplateCandidateImpl implements OrchestrationTemplateCandidate {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrchestrationTemplateCandidateImpl.class);
    private final OrchestrationTemplateCandidateManager candidateManager;
    private final VendorSoftwareProductManager vendorSoftwareProductManager;
    private final ActivityLogManager activityLogManager;
    private final ArtifactStorageManager artifactStorageManager;
    private final PackageSizeReducer packageSizeReducer;
    private final OrchestrationTemplateCandidateUploadManager orchestrationTemplateCandidateUploadManager;

    @Autowired
    public OrchestrationTemplateCandidateImpl(final OrchestrationTemplateCandidateUploadManager orchestrationTemplateCandidateUploadManager) {
        this.candidateManager = OrchestrationTemplateCandidateManagerFactory.getInstance().createInterface();
        this.vendorSoftwareProductManager = VspManagerFactory.getInstance().createInterface();
        this.activityLogManager = ActivityLogManagerFactory.getInstance().createInterface();
        LOGGER.info("Instantiating artifactStorageManager");
        final StorageFactory storageFactory = new StorageFactory();
        this.artifactStorageManager = storageFactory.createArtifactStorageManager();
        LOGGER.info("Instantiating packageSizeReducer");
        this.packageSizeReducer = storageFactory.createPackageSizeReducer().orElse(null);
        this.orchestrationTemplateCandidateUploadManager = orchestrationTemplateCandidateUploadManager;
    }

    // Constructor used in test to avoid mock static
    public OrchestrationTemplateCandidateImpl(final OrchestrationTemplateCandidateManager candidateManager,
                                              final VendorSoftwareProductManager vendorSoftwareProductManager,
                                              final ActivityLogManager activityLogManager,
                                              final ArtifactStorageManager artifactStorageManager,
                                              final PackageSizeReducer packageSizeReducer,
                                              final OrchestrationTemplateCandidateUploadManager orchestrationTemplateCandidateUploadManager) {
        this.candidateManager = candidateManager;
        this.vendorSoftwareProductManager = vendorSoftwareProductManager;
        this.activityLogManager = activityLogManager;
        this.artifactStorageManager = artifactStorageManager;
        this.packageSizeReducer = packageSizeReducer;
        this.orchestrationTemplateCandidateUploadManager = orchestrationTemplateCandidateUploadManager;
    }

    @Override
    public Response upload(String vspId, String versionId, final Attachment fileToUpload, final String user) {
        vspId = ValidationUtils.sanitizeInputString(vspId);
        versionId = ValidationUtils.sanitizeInputString(versionId);
        final Response response;
        VspUploadStatusDto vspUploadStatus = null;
        try {
            vspUploadStatus = orchestrationTemplateCandidateUploadManager.startUpload(vspId, versionId, user);
            final byte[] fileToUploadBytes;
            final DataHandler dataHandler = fileToUpload.getDataHandler();
            final var filename = ValidationUtils.sanitizeInputString(dataHandler.getName());
            ArtifactInfo artifactInfo = null;
            if (artifactStorageManager.isEnabled()) {
                artifactInfo = handleArtifactStorage(vspId, versionId, filename, dataHandler);
                fileToUploadBytes = artifactInfo.getBytes();
            } else {
                fileToUploadBytes = fileToUpload.getObject(byte[].class);
            }

            final var onboardingPackageProcessor =
                new OnboardingPackageProcessor(filename, fileToUploadBytes, new CnfPackageValidator(), artifactInfo);
            final ErrorMessage[] errorMessages = onboardingPackageProcessor.getErrorMessages().toArray(new ErrorMessage[0]);
            if (onboardingPackageProcessor.hasErrors()) {
                return Response.status(NOT_ACCEPTABLE).entity(buildUploadResponseWithError(errorMessages)).build();
            }
            final var onboardPackageInfo = onboardingPackageProcessor.getOnboardPackageInfo().orElse(null);
            if (onboardPackageInfo == null) {
                final UploadFileResponseDto uploadFileResponseDto = buildUploadResponseWithError(
                    new ErrorMessage(ErrorLevel.ERROR, PACKAGE_PROCESS_ERROR.formatMessage(filename)));
                return Response.ok(uploadFileResponseDto).build();
            }
            final var version = new Version(versionId);
            final var vspDetails = vendorSoftwareProductManager.getVsp(vspId, version);
            response = processOnboardPackage(onboardPackageInfo, vspDetails, errorMessages);
            final UploadFileResponseDto entity = (UploadFileResponseDto) response.getEntity();
            if (artifactStorageManager.isEnabled()) {
                if (!entity.getErrors().isEmpty()) {
                    artifactStorageManager.delete(artifactInfo);
                } else {
                    artifactStorageManager.put(vspId, versionId + ".reduced", new ByteArrayInputStream(fileToUploadBytes));
                }
            }
            orchestrationTemplateCandidateUploadManager
                .finishUpload(vspId, versionId, vspUploadStatus.getLockId(), VspUploadStatusType.SUCCESS, user);
        } catch (final Exception ex) {
            if (vspUploadStatus != null) {
                orchestrationTemplateCandidateUploadManager
                    .finishUpload(vspId, versionId, vspUploadStatus.getLockId(), VspUploadStatusType.ERROR, user);
            }
            final ErrorMessage errorMessage = new ErrorMessage(ErrorLevel.ERROR, ex.getMessage());
            return Response.status(INTERNAL_SERVER_ERROR).entity(buildUploadResponseWithError(errorMessage)).build();
        }
        return response;
    }

    private ArtifactInfo handleArtifactStorage(final String vspId, final String versionId, final String filename,
                                               final DataHandler artifactDataHandler) {
        final Path tempArtifactPath;
        try {
            final ArtifactStorageConfig storageConfiguration = artifactStorageManager.getStorageConfiguration();

            final Path folder = Path.of(storageConfiguration.getTempPath()).resolve(vspId).resolve(versionId);
            tempArtifactPath = folder.resolve(UUID.randomUUID().toString());
            Files.createDirectories(folder);
            try (final InputStream packageInputStream = artifactDataHandler.getInputStream();
                final var fileOutputStream = new FileOutputStream(tempArtifactPath.toFile())) {
                packageInputStream.transferTo(fileOutputStream);
            }
        } catch (final Exception e) {
            throw new ArtifactStorageException(UNEXPECTED_PROBLEM_HAPPENED_WHILE_GETTING.formatMessage(filename));
        }
        final ArtifactInfo artifactInfo;
        try (final InputStream inputStream = Files.newInputStream(tempArtifactPath)) {
            artifactInfo = artifactStorageManager.upload(vspId, versionId, inputStream);
        } catch (final Exception e) {
            LOGGER.error("Package Size Reducer not configured", e);
            throw new ArtifactStorageException(ERROR_HAS_OCCURRED_WHILE_PERSISTING_THE_ARTIFACT.formatMessage(filename));
        }
        try {
            artifactInfo.setBytes(packageSizeReducer.reduce(tempArtifactPath));
            Files.delete(tempArtifactPath);
        } catch (final Exception e) {
            LOGGER.error("Package Size Reducer not configured", e);
            throw new ArtifactStorageException(ERROR_HAS_OCCURRED_WHILE_REDUCING_THE_ARTIFACT_SIZE.formatMessage(filename));
        }
        return artifactInfo;
    }

    private Response processOnboardPackage(final OnboardPackageInfo onboardPackageInfo, final VspDetails vspDetails,
                                           final ErrorMessage... errorMessages) {
        final UploadFileResponse uploadFileResponse = candidateManager.upload(vspDetails, onboardPackageInfo);
        final UploadFileResponseDto uploadFileResponseDto = new MapUploadFileResponseToUploadFileResponseDto()
            .applyMapping(uploadFileResponse, UploadFileResponseDto.class);
        if (errorMessages.length > 0) {
            uploadFileResponseDto.setErrors(getErrorMap(errorMessages));
        }
        return Response.ok(uploadFileResponseDto).build();
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
    public Response get(String vspId, String versionId, String user) throws IOException {
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
                return Response.status(NOT_FOUND).build();
            }
            fileName = "Processed." + zipFile.get().getLeft();
        }
        Response.ResponseBuilder response = Response.ok(zipFile.get().getRight());
        response.header("Content-Disposition", "attachment; filename=" + fileName);
        return response.build();
    }

    @Override
    public Response abort(String vspId, String versionId) {
        candidateManager.abort(vspId, new Version(versionId));
        return Response.ok().build();
    }

    @Override
    public Response process(String vspId, String versionId, String user) {
        Version version = new Version(versionId);
        OrchestrationTemplateActionResponse response = candidateManager.process(vspId, version);
        activityLogManager.logActivity(new ActivityLogEntity(vspId, version, ActivityType.Upload_Network_Package, user, true, "", ""));
        OrchestrationTemplateActionResponseDto responseDto = copyOrchestrationTemplateActionResponseToDto(response);
        return Response.ok(responseDto).build();
    }

    @Override
    public Response updateFilesDataStructure(String vspId, String versionId, FileDataStructureDto fileDataStructureDto, String user) {
        FilesDataStructure fileDataStructure = copyFilesDataStructureDtoToFilesDataStructure(fileDataStructureDto);
        ValidationResponse response = candidateManager.updateFilesDataStructure(vspId, new Version(versionId), fileDataStructure);
        if (!response.isValid()) {
            return Response.status(EXPECTATION_FAILED)
                .entity(new MapValidationResponseToDto().applyMapping(response, ValidationResponseDto.class))
                .build();
        }
        return Response.ok(fileDataStructureDto).build();
    }

    @Override
    public Response getFilesDataStructure(String vspId, String versionId, String user) {
        Optional<FilesDataStructure> filesDataStructure = candidateManager.getFilesDataStructure(vspId, new Version(versionId));
        if (filesDataStructure.isEmpty()) {
            filesDataStructure = vendorSoftwareProductManager.getOrchestrationTemplateStructure(vspId, new Version(versionId));
        }
        FileDataStructureDto fileDataStructureDto = filesDataStructure
            .map(dataStructure -> new MapFilesDataStructureToDto().applyMapping(dataStructure, FileDataStructureDto.class))
            .orElse(new FileDataStructureDto());
        return Response.ok(fileDataStructureDto).build();
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
