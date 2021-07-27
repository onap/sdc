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
import static org.openecomp.sdc.common.errors.Messages.EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING_FULL_PATH;
import static org.openecomp.sdc.common.errors.Messages.NO_FILE_WAS_UPLOADED_OR_FILE_NOT_EXIST;
import static org.openecomp.sdc.common.errors.Messages.PACKAGE_PROCESS_ERROR;
import static org.openecomp.sdc.common.errors.Messages.UNEXPECTED_PROBLEM_HAPPENED_WHILE_GETTING;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.openecomp.sdc.be.csar.storage.CsarPackageReducerConfiguration;
import org.openecomp.sdc.be.csar.storage.CsarSizeReducer;
import org.openecomp.sdc.be.csar.storage.PackageSizeReducer;
import org.openecomp.sdc.be.csar.storage.PersistentVolumeArtifactStorageConfig;
import org.openecomp.sdc.be.csar.storage.PersistentVolumeArtifactStorageManager;
import org.openecomp.sdc.be.exception.BusinessException;
import org.openecomp.sdc.common.CommonConfigurationManager;
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
import org.openecomp.sdcrests.vsp.rest.OrchestrationTemplateCandidate;
import org.openecomp.sdcrests.vsp.rest.mapping.MapFilesDataStructureToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapUploadFileResponseToUploadFileResponseDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapValidationResponseToDto;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Named
@Service("orchestrationTemplateCandidate")
@Scope(value = "prototype")
public class OrchestrationTemplateCandidateImpl implements OrchestrationTemplateCandidate {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrchestrationTemplateCandidateImpl.class);
    private static final String EXTERNAL_CSAR_STORE = "externalCsarStore";
    private final OrchestrationTemplateCandidateManager candidateManager;
    private final VendorSoftwareProductManager vendorSoftwareProductManager;
    private final ActivityLogManager activityLogManager;
    private final ArtifactStorageManager artifactStorageManager;
    private final PackageSizeReducer packageSizeReducer;

    public OrchestrationTemplateCandidateImpl() {
        this.candidateManager = OrchestrationTemplateCandidateManagerFactory.getInstance().createInterface();
        this.vendorSoftwareProductManager = VspManagerFactory.getInstance().createInterface();
        this.activityLogManager = ActivityLogManagerFactory.getInstance().createInterface();
        LOGGER.info("Instantiating artifactStorageManager");
        this.artifactStorageManager = new PersistentVolumeArtifactStorageManager(readArtifactStorageConfiguration());
        LOGGER.info("Instantiating packageSizeReducer");
        this.packageSizeReducer = new CsarSizeReducer(readPackageReducerConfiguration());
    }

    // Constructor used in test to avoid mock static
    public OrchestrationTemplateCandidateImpl(final OrchestrationTemplateCandidateManager candidateManager,
                                              final VendorSoftwareProductManager vendorSoftwareProductManager,
                                              final ActivityLogManager activityLogManager,
                                              final ArtifactStorageManager artifactStorageManager,
                                              final PackageSizeReducer packageSizeReducer) {
        this.candidateManager = candidateManager;
        this.vendorSoftwareProductManager = vendorSoftwareProductManager;
        this.activityLogManager = activityLogManager;
        this.artifactStorageManager = artifactStorageManager;
        this.packageSizeReducer = packageSizeReducer;
    }

    private CsarPackageReducerConfiguration readPackageReducerConfiguration() {
        final var commonConfigurationManager = CommonConfigurationManager.getInstance();
        final List<String> foldersToStrip = commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, "foldersToStrip", new ArrayList<>());
        final int sizeLimit = commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, "sizeLimit", 1000000);
        final int thresholdEntries = commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, "thresholdEntries", 10000);
        LOGGER.info("Folders to strip: '{}'", String.join(", ", foldersToStrip));
        final Set<Path> foldersToStripPathSet = foldersToStrip.stream().map(Path::of).collect(Collectors.toSet());
        return new CsarPackageReducerConfiguration(foldersToStripPathSet, sizeLimit, thresholdEntries);
    }

    private ArtifactStorageConfig readArtifactStorageConfiguration() {
        final var commonConfigurationManager = CommonConfigurationManager.getInstance();
        final boolean isEnabled = commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, "storeCsarsExternally", false);
        LOGGER.info("ArtifactConfig.isEnabled: '{}'", isEnabled);
        final String storagePathString = commonConfigurationManager.getConfigValue(EXTERNAL_CSAR_STORE, "fullPath", null);
        LOGGER.info("ArtifactConfig.storagePath: '{}'", storagePathString);
        if (isEnabled && storagePathString == null) {
            throw new OrchestrationTemplateCandidateException(EXTERNAL_CSAR_STORE_CONFIGURATION_FAILURE_MISSING_FULL_PATH.getErrorMessage());
        }
        final var storagePath = storagePathString == null ? null : Path.of(storagePathString);
        return new PersistentVolumeArtifactStorageConfig(isEnabled, storagePath);
    }

    @Override
    public Response upload(final String vspId, final String versionId, final Attachment fileToUpload, final String user) {
        final byte[] fileToUploadBytes;
        final var filename = ValidationUtils.sanitizeInputString(fileToUpload.getDataHandler().getName());
        ArtifactInfo artifactInfo = null;
        if (artifactStorageManager.isEnabled()) {
            final InputStream packageInputStream;
            try {
                packageInputStream = fileToUpload.getDataHandler().getInputStream();
            } catch (final IOException e) {
                return Response.status(INTERNAL_SERVER_ERROR).entity(buildUploadResponseWithError(
                    new ErrorMessage(ErrorLevel.ERROR, UNEXPECTED_PROBLEM_HAPPENED_WHILE_GETTING.formatMessage(filename)))).build();
            }
            try {
                artifactInfo = artifactStorageManager.upload(vspId, versionId, packageInputStream);
            } catch (final BusinessException e) {
                return Response.status(INTERNAL_SERVER_ERROR).entity(buildUploadResponseWithError(
                    new ErrorMessage(ErrorLevel.ERROR, ERROR_HAS_OCCURRED_WHILE_PERSISTING_THE_ARTIFACT.formatMessage(filename)))).build();
            }
            try {
                fileToUploadBytes = packageSizeReducer.reduce(artifactInfo.getPath());
            } catch (final BusinessException e) {
                return Response.status(INTERNAL_SERVER_ERROR).entity(buildUploadResponseWithError(
                        new ErrorMessage(ErrorLevel.ERROR, ERROR_HAS_OCCURRED_WHILE_REDUCING_THE_ARTIFACT_SIZE.formatMessage(artifactInfo.getPath()))))
                    .build();
            }
        } else {
            fileToUploadBytes = fileToUpload.getObject(byte[].class);
        }

        final var onboardingPackageProcessor = new OnboardingPackageProcessor(filename, fileToUploadBytes, new CnfPackageValidator(), artifactInfo);
        final ErrorMessage[] errorMessages = onboardingPackageProcessor.getErrorMessages().toArray(new ErrorMessage[0]);
        if (onboardingPackageProcessor.hasErrors()) {
            return Response.status(NOT_ACCEPTABLE).entity(buildUploadResponseWithError(errorMessages)).build();
        }
        final var onboardPackageInfo = onboardingPackageProcessor.getOnboardPackageInfo().orElse(null);
        if (onboardPackageInfo == null) {
            final UploadFileResponseDto uploadFileResponseDto = buildUploadResponseWithError(
                new ErrorMessage(ErrorLevel.ERROR, PACKAGE_PROCESS_ERROR.formatMessage(filename)));
            return Response.ok(uploadFileResponseDto)
                .build();
        }
        final var version = new Version(ValidationUtils.sanitizeInputString(versionId));
        final var vspDetails = new VspDetails(ValidationUtils.sanitizeInputString(vspId), version);
        return processOnboardPackage(onboardPackageInfo, vspDetails, errorMessages);
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
            if (!zipFile.isPresent()) {
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
            return Response.status(EXPECTATION_FAILED).entity(new MapValidationResponseToDto().applyMapping(response, ValidationResponseDto.class))
                .build();
        }
        return Response.ok(fileDataStructureDto).build();
    }

    @Override
    public Response getFilesDataStructure(String vspId, String versionId, String user) {
        Optional<FilesDataStructure> filesDataStructure = candidateManager.getFilesDataStructure(vspId, new Version(versionId));
        if (!filesDataStructure.isPresent()) {
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
