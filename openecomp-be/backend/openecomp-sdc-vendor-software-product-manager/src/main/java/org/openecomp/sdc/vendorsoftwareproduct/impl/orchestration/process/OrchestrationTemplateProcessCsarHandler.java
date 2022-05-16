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
package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.process;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.impl.AbstractToscaSolConverter;
import org.openecomp.core.impl.ToscaConverterImpl;
import org.openecomp.core.impl.ToscaModelConverter;
import org.openecomp.core.impl.ToscaSolConverterVnf;
import org.openecomp.core.impl.ToscaSolModelDrivenConverterPnf;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.core.validation.util.MessageContainerUtil;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;
import org.openecomp.sdc.heat.services.tree.ToscaTreeManager;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.csar.AsdPackageHelper;
import org.openecomp.sdc.tosca.csar.ManifestUtils;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CandidateServiceFactory;
import org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.OrchestrationUtil;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.services.impl.etsi.ETSIService;
import org.openecomp.sdc.vendorsoftwareproduct.services.impl.etsi.ETSIServiceImpl;
import org.openecomp.sdc.vendorsoftwareproduct.types.OrchestrationTemplateActionResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrchestrationTemplateProcessCsarHandler implements OrchestrationTemplateProcessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrchestrationTemplateProcessCsarHandler.class);
    private static final String SDC_ONBOARDED_PACKAGE_DIR_ETSI = "Deployment/" + ArtifactTypeEnum.ETSI_PACKAGE.getType() + "/";
    private static final String SDC_ONBOARDED_PACKAGE_DIR_ASD = "Deployment/" + ArtifactTypeEnum.ASD_PACKAGE.getType() + "/";
    private static final String EXT_SEPARATOR = ".";
    private final CandidateService candidateService = CandidateServiceFactory.getInstance().createInterface();
    private final ToscaTreeManager toscaTreeManager = new ToscaTreeManager();
    private final ETSIService etsiService;
    private final AsdPackageHelper asdPackageHelper;

    public OrchestrationTemplateProcessCsarHandler() {
        etsiService = new ETSIServiceImpl();
        this.asdPackageHelper = new AsdPackageHelper(new ManifestUtils());
    }

    @Override
    public OrchestrationTemplateActionResponse process(VspDetails vspDetails, OrchestrationTemplateCandidateData candidateData) {
        UploadFileResponse uploadFileResponse = new UploadFileResponse();
        Optional<FileContentHandler> fileContent = OrchestrationUtil
            .getFileContentMap(OnboardingTypesEnum.CSAR, uploadFileResponse, candidateData.getContentData().array());
        OrchestrationTemplateActionResponse response = new OrchestrationTemplateActionResponse();
        if (fileContent.isPresent()) {
            try {
                FileContentHandler fileContentHandler = fileContent.get();
                processCsar(vspDetails, fileContentHandler, candidateData, response);
            } catch (CoreException e) {
                LOGGER.error(e.getMessage(), e);
                response.addErrorMessageToMap(e.code().id(), e.code().message(), ErrorLevel.ERROR);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                response.addErrorMessageToMap(SdcCommon.PROCESS_FILE, e.getMessage(), ErrorLevel.ERROR);
            }
        } else {
            if (!uploadFileResponse.getErrors().isEmpty()) {
                response.addStructureErrors(uploadFileResponse.getErrors());
            }
        }
        return response;
    }

    private void processCsar(VspDetails vspDetails, FileContentHandler fileContentHandler, OrchestrationTemplateCandidateData candidateData,
                             OrchestrationTemplateActionResponse response) throws IOException {
        response.setFileNames(new ArrayList<>(fileContentHandler.getFileList()));
        Map<String, List<ErrorMessage>> errors = validateCsar(fileContentHandler);
        toscaTreeManager.createTree();
        if (!isValid(errors)) {
            response.addStructureErrors(errors);
            toscaTreeManager.addErrors(errors);
            candidateService
                .updateValidationData(vspDetails.getId(), vspDetails.getVersion(), new ValidationStructureList(toscaTreeManager.getTree()));
            return;
        }
        HeatStructureTree tree = toscaTreeManager.getTree();
        final var orchestrationUtil = new OrchestrationUtil();
        orchestrationUtil.backupComponentsQuestionnaireBeforeDelete(vspDetails.getId(), vspDetails.getVersion(),
            new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
        Optional<ByteArrayInputStream> zipByteArrayInputStream = candidateService
            .fetchZipFileByteArrayInputStream(vspDetails.getId(), candidateData, null, OnboardingTypesEnum.CSAR, errors);
        orchestrationUtil.deleteUploadDataAndContent(vspDetails.getId(), vspDetails.getVersion());
        zipByteArrayInputStream.ifPresent(
            byteArrayInputStream -> orchestrationUtil.saveUploadData(vspDetails, candidateData, byteArrayInputStream, fileContentHandler, tree));
        final var toscaServiceModel = convertToToscaServiceModel(vspDetails.getModelIdList(), fileContentHandler, candidateData);
        orchestrationUtil
            .saveServiceModel(vspDetails.getId(), vspDetails.getVersion(), toscaServiceModel, toscaServiceModel);
        candidateService.deleteOrchestrationTemplateCandidate(vspDetails.getId(), vspDetails.getVersion());
    }

    private ToscaServiceModel convertToToscaServiceModel(final List<String> modelList, final FileContentHandler fileContentHandler,
                                                         final OrchestrationTemplateCandidateData candidateData) throws IOException {
        if (CollectionUtils.isNotEmpty(modelList)) {
            return handleToscaModelConversion(modelList, fileContentHandler, candidateData);
        }
        if (etsiService.isEtsiPackage(fileContentHandler) || asdPackageHelper.isAsdPackage(fileContentHandler)) {
            return getToscaServiceModelSol004(fileContentHandler, candidateData);
        }
        return new ToscaConverterImpl().convert(fileContentHandler);
    }

    private ToscaServiceModel handleToscaModelConversion(final List<String> modelList, final FileContentHandler fileContentHandler,
                                                         final OrchestrationTemplateCandidateData candidateData) throws IOException {
        addOriginalOnboardedPackage(fileContentHandler, candidateData);
        final var toscaServiceModel = new ToscaModelConverter().convert(fileContentHandler);
        toscaServiceModel.setModelList(modelList);
        return toscaServiceModel;
    }

    private ToscaServiceModel getToscaServiceModelSol004(final FileContentHandler fileContentHandler,
                                                         final OrchestrationTemplateCandidateData candidateData) throws IOException {
        addOriginalOnboardedPackage(fileContentHandler, candidateData);
        final ResourceTypeEnum resourceType = etsiService.getResourceType(fileContentHandler);
        return instantiateToscaConverterFor(resourceType).convert(fileContentHandler);
    }

    private void addOriginalOnboardedPackage(final FileContentHandler fileContentHandler, final OrchestrationTemplateCandidateData candidateData) {
        if (OnboardingTypesEnum.CSAR.getType().equalsIgnoreCase(candidateData.getFileSuffix())) {
            if (asdPackageHelper.isAsdPackage(fileContentHandler)) {
                fileContentHandler
                    .addFile(SDC_ONBOARDED_PACKAGE_DIR_ASD + candidateData.getOriginalFileName() + EXT_SEPARATOR + candidateData.getOriginalFileSuffix(),
                        candidateData.getOriginalFileContentData().array());
            } else {
                fileContentHandler
                    .addFile(SDC_ONBOARDED_PACKAGE_DIR_ETSI + candidateData.getOriginalFileName() + EXT_SEPARATOR + candidateData.getOriginalFileSuffix(),
                        candidateData.getOriginalFileContentData().array());
            }
        } else {
            fileContentHandler.addFile(SDC_ONBOARDED_PACKAGE_DIR_ETSI + candidateData.getFileName() + EXT_SEPARATOR + candidateData.getFileSuffix(),
                candidateData.getContentData().array());
        }
    }

    private AbstractToscaSolConverter instantiateToscaConverterFor(ResourceTypeEnum resourceType) {
        if (resourceType == ResourceTypeEnum.PNF) {
            return new ToscaSolModelDrivenConverterPnf();
        }
        // default is VF
        return new ToscaSolConverterVnf();
    }

    private void addFiles(FileContentHandler fileContentHandler) {
        for (Map.Entry<String, byte[]> fileEntry : fileContentHandler.getFiles().entrySet()) {
            toscaTreeManager.addFile(fileEntry.getKey(), fileEntry.getValue());
        }
    }

    private Map<String, List<ErrorMessage>> validateCsar(FileContentHandler fileContentHandler) {
        Map<String, List<ErrorMessage>> errors = new HashMap<>();
        addFiles(fileContentHandler);
        toscaTreeManager.createTree();
        toscaTreeManager.addErrors(errors);
        //todo - add tosca validation here to the existing validation framework
        return errors;
    }

    private boolean isValid(Map<String, List<ErrorMessage>> errors) {
        return MapUtils.isEmpty(MessageContainerUtil.getMessageByLevel(ErrorLevel.ERROR, errors));
    }
}
