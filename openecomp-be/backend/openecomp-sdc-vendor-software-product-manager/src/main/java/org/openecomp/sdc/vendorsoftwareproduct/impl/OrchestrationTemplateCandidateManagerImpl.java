/*
 * Copyright © 2016-2018 European Support Limited
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
package org.openecomp.sdc.vendorsoftwareproduct.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.OrchestrationTemplateNotFoundErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.OrchestrationTemplateFileHandler;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.OrchestrationUploadFactory;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.process.OrchestrationProcessFactory;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackage;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.types.OrchestrationTemplateActionResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.versioning.dao.types.Version;

public class OrchestrationTemplateCandidateManagerImpl implements OrchestrationTemplateCandidateManager {

    private final VendorSoftwareProductInfoDao vspInfoDao;
    private final CandidateService candidateService;

    public OrchestrationTemplateCandidateManagerImpl(VendorSoftwareProductInfoDao vspInfoDao, CandidateService candidateService) {
        this.vspInfoDao = vspInfoDao;
        this.candidateService = candidateService;
    }

    @Override
    public UploadFileResponse upload(final VspDetails vspDetails, final OnboardPackageInfo onboardPackageInfo) {
        final OnboardPackage onboardPackage = onboardPackageInfo.getOnboardPackage();
        final OrchestrationTemplateFileHandler orchestrationTemplateFileHandler = OrchestrationUploadFactory
            .createOrchestrationTemplateFileHandler(onboardPackageInfo.getPackageType());
        final UploadFileResponse uploadFileResponse = orchestrationTemplateFileHandler.upload(vspDetails, onboardPackageInfo, candidateService);
        uploadFileResponse.setNetworkPackageName(onboardPackage.getFilename());
        return uploadFileResponse;
    }

    @Override
    public OrchestrationTemplateActionResponse process(String vspId, Version version) {
        OrchestrationTemplateCandidateData candidate = candidateService.getOrchestrationTemplateCandidate(vspId, version)
            .orElseThrow(() -> new CoreException(new OrchestrationTemplateNotFoundErrorBuilder(vspId).build()));
        return OrchestrationProcessFactory.getInstance(candidate.getFileSuffix())
            .map(processor -> processor.process(getVspDetails(vspId, version), candidate)).orElse(new OrchestrationTemplateActionResponse());
    }

    @Override
    public Optional<FilesDataStructure> getFilesDataStructure(String vspId, Version version) {
        return candidateService.getOrchestrationTemplateCandidateFileDataStructure(vspId, version);
    }

    @Override
    public ValidationResponse updateFilesDataStructure(String vspId, Version version, FilesDataStructure fileDataStructure) {
        ValidationResponse response = new ValidationResponse();
        Optional<List<ErrorMessage>> validateErrors = candidateService.validateFileDataStructure(fileDataStructure);
        if (validateErrors.isPresent()) {
            List<ErrorMessage> errorMessages = validateErrors.get();
            if (CollectionUtils.isNotEmpty(errorMessages)) {
                Map<String, List<ErrorMessage>> errorsMap = Collections.singletonMap(SdcCommon.UPLOAD_FILE, errorMessages);
                response.setUploadDataErrors(errorsMap);
                return response;
            }
        }
        candidateService.updateOrchestrationTemplateCandidateFileDataStructure(vspId, version, fileDataStructure);
        return response;
    }

    @Override
    public Optional<Pair<String, byte[]>> get(String vspId, Version version) throws IOException {
        VspDetails vspDetails = getVspDetails(vspId, version);
        Optional<OrchestrationTemplateCandidateData> candidateDataEntity = candidateService.getOrchestrationTemplateCandidate(vspId, version);
        if (!candidateDataEntity.isPresent()) {
            return Optional.empty();
        }
        if (CommonUtil.isFileOriginFromZip(candidateDataEntity.get().getFileSuffix())) {
            FilesDataStructure structure = JsonUtil.json2Object(candidateDataEntity.get().getFilesDataStructure(), FilesDataStructure.class);
            String manifest = candidateService.createManifest(vspDetails, structure);
            OnboardingTypesEnum type = OnboardingTypesEnum.getOnboardingTypesEnum(candidateDataEntity.get().getFileSuffix());
            return Optional.of(new ImmutablePair<>(OnboardingTypesEnum.ZIP.toString(),
                candidateService.replaceManifestInZip(candidateDataEntity.get().getContentData(), manifest, type)));
        }
        return Optional.of(new ImmutablePair<>(candidateDataEntity.get().getFileSuffix(), candidateDataEntity.get().getContentData().array()));
    }

    @Override
    public Optional<OrchestrationTemplateCandidateData> getInfo(String vspId, Version version) {
        return candidateService.getOrchestrationTemplateCandidateInfo(vspId, version);
    }

    @Override
    public void abort(String vspId, Version version) {
        candidateService.deleteOrchestrationTemplateCandidate(vspId, version);
    }

    private VspDetails getVspDetails(String vspId, Version version) {
        return vspInfoDao.get(new VspDetails(vspId, version));
    }
}
