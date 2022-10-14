/*
 * Copyright © 2018 European Support Limited
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
 * ================================================================================
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.healing.healers;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.utils.fileutils.FileUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenAdaptorFactory;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OnboardingMethod;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CandidateServiceFactory;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.services.utils.CandidateEntityBuilder;
import org.openecomp.sdc.versioning.dao.types.Version;

public class NetworkPackageHealer implements Healer {

    private static final byte[] EMPTY_DATA_BYTES = "{}".getBytes();
    private static final String MISSING_ORCHESTRATION_TEMPLATE_CANDIDATE_ERROR = "Vsp with invalid structure: does not contain element OrchestrationTemplateCandidate";
    private static final String MISSING_ORCHESTRATION_TEMPLATE_CANDIDATE_CONTENT_ERROR =
        "Vsp with invalid structure: does not contain element OrchestrationTemplateCandidateContent" + " under OrchestrationTemplateCandidate";
    private static final String MISSING_VSP_MODEL_ERROR = "Vsp with invalid structure: does not contain element VspModel";
    private static final String MISSING_ORCHESTRATION_TEMPLATE_ERROR =
        "Vsp with invalid structure: does not contain element OrchestrationTemplate" + " under VspModel element";
    private static final String MISSING_ORCHESTRATION_TEMPLATE_VALIDATE_DATA_ERROR =
        "Vsp with invalid structure: does not contain element OrchestrationTemplateValidationData" + " under OrchestrationTemplate element";
    private final VendorSoftwareProductInfoDao vspInfoDao;
    private final ZusammenAdaptor zusammenAdaptor;
    private final CandidateEntityBuilder candidateEntityBuilder;

    public NetworkPackageHealer() {
        this(VendorSoftwareProductInfoDaoFactory.getInstance().createInterface(), ZusammenAdaptorFactory.getInstance().createInterface(),
            CandidateServiceFactory.getInstance().createInterface());
    }

    private NetworkPackageHealer(VendorSoftwareProductInfoDao vspInfoDao, ZusammenAdaptor zusammenAdaptor, CandidateService candidateService) {
        this(vspInfoDao, zusammenAdaptor, new CandidateEntityBuilder(candidateService));
    }

    NetworkPackageHealer(VendorSoftwareProductInfoDao vspInfoDao, ZusammenAdaptor zusammenAdaptor, CandidateEntityBuilder candidateEntityBuilder) {
        this.vspInfoDao = vspInfoDao;
        this.zusammenAdaptor = zusammenAdaptor;
        this.candidateEntityBuilder = candidateEntityBuilder;
    }

    @Override
    public boolean isHealingNeeded(String itemId, Version version) {
        return OnboardingMethod.NetworkPackage.name().equals(vspInfoDao.get(new VspDetails(itemId, version)).getOnboardingMethod())
            && isVspMissingAddedElements(itemId, version);
    }

    @Override
    public void heal(String itemId, Version version) throws Exception {
        SessionContext context = createSessionContext();
        ElementContext elementContext = new ElementContext(itemId, version.getId());
        Element candidateElement = getElement(context, elementContext, null, ElementType.OrchestrationTemplateCandidate,
            MISSING_ORCHESTRATION_TEMPLATE_CANDIDATE_ERROR);
        Collection<Element> candidateSubs = zusammenAdaptor.listElementData(context, elementContext, candidateElement.getElementId());
        Id candidateValidationElementId = creatIfAbsentCandidateValidationElementId(candidateSubs, context, elementContext, candidateElement);
        Element orchestrationTemplateElement = getOrchestrationTemplateElement(context, elementContext);
        Collection<Element> orchestrationTemplateSubs = zusammenAdaptor
            .listElementData(context, elementContext, orchestrationTemplateElement.getElementId());
        Id structureElementId = createIfAbsentStructureElementId(orchestrationTemplateSubs, context, elementContext, orchestrationTemplateElement);
        Element orchestrationTemplateValidationElement = getOrchestrationTemplateValidationElement(orchestrationTemplateSubs);
        OrchestrationTemplateEntity orchestrationTemplateEntity = getOrchestrationTemplateEntity(orchestrationTemplateElement,
            orchestrationTemplateValidationElement);
        if (StringUtils.isEmpty(orchestrationTemplateEntity.getFileSuffix())) {
            return;
        }
        Element candidateContentElement = getCandidateContentElement(candidateSubs);
        VspDetails vspDetails = vspInfoDao.get(new VspDetails(itemId, version));
        if (isEqual(orchestrationTemplateEntity, getCandidateData(candidateElement, candidateContentElement))) {
            if (isProcessedEntityValid(orchestrationTemplateEntity)) {
                emptyStructureElementAndSub(context, elementContext, candidateElement.getElementId(), ElementType.OrchestrationTemplateCandidate,
                    candidateContentElement.getElementId(), ElementType.OrchestrationTemplateCandidateContent);
                populateOrchestrationTemplateStructure(orchestrationTemplateElement.getData(), structureElementId, vspDetails, context,
                    elementContext);
            } else {
                emptyStructureElementAndSub(context, elementContext, orchestrationTemplateElement.getElementId(), ElementType.OrchestrationTemplate,
                    orchestrationTemplateValidationElement.getElementId(), ElementType.OrchestrationTemplateValidationData);
                populateCandidateValidationData(context, elementContext, candidateValidationElementId,
                    orchestrationTemplateEntity.getValidationData());
            }
        } else {
            populateOrchestrationTemplateStructure(orchestrationTemplateElement.getData(), structureElementId, vspDetails, context, elementContext);
        }
    }

    private boolean isVspMissingAddedElements(String vspId, Version version) {
        SessionContext context = createSessionContext();
        ElementContext elementContext = new ElementContext(vspId, version.getId());
        return zusammenAdaptor.listElementsByName(context, elementContext, null, ElementType.OrchestrationTemplateCandidate.name()).stream()
            .noneMatch(candidateSub -> ElementType.OrchestrationTemplateCandidateValidationData.name().equals(candidateSub.getInfo().getName()));
    }

    private boolean isEqual(OrchestrationTemplateEntity orchestrationTemplateEntity,
                            OrchestrationTemplateCandidateData orchestrationTemplateCandidateData) {
        return orchestrationTemplateEntity.getFileName().equals(orchestrationTemplateCandidateData.getFileName()) && orchestrationTemplateEntity
            .getFileSuffix().equals(orchestrationTemplateCandidateData.getFileSuffix());
    }

    private boolean isProcessedEntityValid(OrchestrationTemplateEntity orchestrationTemplateEntity) {
        return !orchestrationTemplateEntity.getValidationData().contains(ErrorLevel.ERROR.name());
    }

    private void populateCandidateValidationData(SessionContext context, ElementContext elementContext, Id candidateValidationElementId,
                                                 String validationData) {
        ZusammenElement candidateValidationElement = buildStructuralElement(ElementType.OrchestrationTemplateCandidateValidationData, Action.UPDATE);
        candidateValidationElement.setElementId(candidateValidationElementId);
        candidateValidationElement.setData(new ByteArrayInputStream(validationData.getBytes()));
        zusammenAdaptor.saveElement(context, elementContext, candidateValidationElement, "Healed Orchestration Template Candidate Validation data");
    }

    private void populateOrchestrationTemplateStructure(InputStream processedFile, Id structureElementId, VspDetails vspDetails,
                                                        SessionContext context, ElementContext elementContext) throws Exception {
        byte[] byteData = FileUtils.toByteArray(processedFile);
        FileContentHandler contentMap = CommonUtil.validateAndUploadFileContent(OnboardingTypesEnum.ZIP, byteData);
        OrchestrationTemplateCandidateData orchestrationTemplateEntityData = candidateEntityBuilder
            .buildCandidateEntityFromZip(vspDetails, byteData, contentMap, null);
        String fileDataStructure = orchestrationTemplateEntityData.getFilesDataStructure();
        ZusammenElement orchestrationTemplateStructure = buildStructuralElement(ElementType.OrchestrationTemplateStructure, Action.UPDATE);
        orchestrationTemplateStructure.setElementId(structureElementId);
        orchestrationTemplateStructure.setData(new ByteArrayInputStream(fileDataStructure.getBytes()));
        zusammenAdaptor.saveElement(context, elementContext, orchestrationTemplateStructure, "Healed Orchestration Template Structure");
    }

    private Element getOrchestrationTemplateElement(SessionContext context, ElementContext elementContext) {
        ElementInfo vspModelElement = zusammenAdaptor.getElementInfoByName(context, elementContext, null, ElementType.VspModel.name())
            .orElseThrow(() -> new CoreException(new ErrorCode.ErrorCodeBuilder().withMessage(MISSING_VSP_MODEL_ERROR).build()));
        return getElement(context, elementContext, vspModelElement.getId(), ElementType.OrchestrationTemplate, MISSING_ORCHESTRATION_TEMPLATE_ERROR);
    }

    private Element getOrchestrationTemplateValidationElement(Collection<Element> orchestrationTemplateSubs) {
        return orchestrationTemplateSubs.stream().filter(
                orchestrationTemplateSub -> ElementType.OrchestrationTemplateValidationData.name().equals(orchestrationTemplateSub.getInfo().getName()))
            .findFirst().orElseThrow(
                () -> new CoreException(new ErrorCode.ErrorCodeBuilder().withMessage(MISSING_ORCHESTRATION_TEMPLATE_VALIDATE_DATA_ERROR).build()));
    }

    private Element getCandidateContentElement(Collection<Element> candidateSubs) {
        return candidateSubs.stream()
            .filter(candidateSub -> ElementType.OrchestrationTemplateCandidateContent.name().equals(candidateSub.getInfo().getName())).findFirst()
            .orElseThrow(() -> new CoreException(
                new ErrorCode.ErrorCodeBuilder().withMessage(MISSING_ORCHESTRATION_TEMPLATE_CANDIDATE_CONTENT_ERROR).build()));
    }

    private Id createIfAbsentStructureElementId(Collection<Element> orchestrationTemplateSubs, SessionContext context, ElementContext elementContext,
                                                Element orchestrationTemplateElement) {
        return orchestrationTemplateSubs.stream().filter(
                orchestrationTemplateSub -> ElementType.OrchestrationTemplateStructure.name().equals(orchestrationTemplateSub.getInfo().getName()))
            .findFirst().map(Element::getElementId).orElse(addStructureSubElement(context, elementContext, ElementType.OrchestrationTemplateStructure,
                orchestrationTemplateElement.getElementId()));
    }

    private Id creatIfAbsentCandidateValidationElementId(Collection<Element> candidateSubs, SessionContext context, ElementContext elementContext,
                                                         Element candidateElement) {
        return candidateSubs.stream()
            .filter(candidateSub -> ElementType.OrchestrationTemplateCandidateValidationData.name().equals(candidateSub.getInfo().getName()))
            .findFirst().map(Element::getElementId).orElse(
                addStructureSubElement(context, elementContext, ElementType.OrchestrationTemplateCandidateValidationData,
                    candidateElement.getElementId()));
    }

    private Element getElement(SessionContext context, ElementContext elementContext, Id parentElementId, ElementType elementType,
                               String errorMessage) {
        return zusammenAdaptor.getElementByName(context, elementContext, parentElementId, elementType.name())
            .orElseThrow(() -> new CoreException(new ErrorCode.ErrorCodeBuilder().withMessage(errorMessage).build()));
    }

    private OrchestrationTemplateEntity getOrchestrationTemplateEntity(Element orchestrationTemplateElement, Element validationDataElement) {
        OrchestrationTemplateEntity orchestrationTemplateEntity = new OrchestrationTemplateEntity();
        if (isNotEmpty(orchestrationTemplateElement.getData())) {
            orchestrationTemplateEntity.setContentData(ByteBuffer.wrap(FileUtils.toByteArray(orchestrationTemplateElement.getData())));
        }
        orchestrationTemplateEntity.setFileSuffix(validationDataElement.getInfo().getProperty(InfoPropertyName.FILE_SUFFIX.getVal()));
        orchestrationTemplateEntity.setFileName(validationDataElement.getInfo().getProperty(InfoPropertyName.FILE_NAME.getVal()));
        if (isNotEmpty(validationDataElement.getData())) {
            orchestrationTemplateEntity.setValidationData(new String(FileUtils.toByteArray(validationDataElement.getData())));
        }
        return orchestrationTemplateEntity;
    }

    private OrchestrationTemplateCandidateData getCandidateData(Element candidateElement, Element candidateContentElement) {
        OrchestrationTemplateCandidateData candidateData = new OrchestrationTemplateCandidateData();
        candidateData.setFilesDataStructure(new String(FileUtils.toByteArray(candidateElement.getData())));
        candidateData.setContentData(ByteBuffer.wrap(FileUtils.toByteArray(candidateContentElement.getData())));
        candidateData.setFileSuffix(candidateContentElement.getInfo().getProperty(InfoPropertyName.FILE_SUFFIX.getVal()));
        candidateData.setFileName(candidateContentElement.getInfo().getProperty(InfoPropertyName.FILE_NAME.getVal()));
        return candidateData;
    }

    private Id addStructureSubElement(SessionContext context, ElementContext elementContext, ElementType elementType, Id parentElementId) {
        ZusammenElement newElement = buildStructuralElement(elementType, Action.CREATE);
        ZusammenElement parentElement = buildElement(parentElementId, Action.IGNORE);
        parentElement.addSubElement(newElement);
        return zusammenAdaptor.saveElement(context, elementContext, parentElement,
                String.format("Add element %s under element id %s", elementType.name(), parentElementId)).getSubElements().iterator().next()
            .getElementId();
    }

    private void emptyStructureElementAndSub(SessionContext context, ElementContext elementContext, Id elementId, ElementType elementType,
                                             Id subElementId, ElementType subElementType) {
        ZusammenElement subElement = buildStructuralElement(subElementType, Action.UPDATE);
        subElement.setElementId(subElementId);
        subElement.setData(new ByteArrayInputStream(EMPTY_DATA_BYTES));
        ZusammenElement element = buildStructuralElement(elementType, Action.UPDATE);
        element.setElementId(elementId);
        element.setData(new ByteArrayInputStream(EMPTY_DATA_BYTES));
        element.addSubElement(subElement);
        zusammenAdaptor.saveElement(context, elementContext, element,
            String.format("Empty element %s and its sub element %s", elementType.name(), subElementType.name()));
    }

    private boolean isNotEmpty(InputStream elementData) {
        byte[] byteElementData;
        if (Objects.isNull(elementData)) {
            return false;
        }
        try {
            byteElementData = IOUtils.toByteArray(elementData);
        } catch (IOException e) {
            return true;
        }
        return !ArrayUtils.isEmpty(byteElementData);
    }

    public enum InfoPropertyName {
        FILE_SUFFIX("fileSuffix"), FILE_NAME("fileName");
        private String val;

        InfoPropertyName(String val) {
            this.val = val;
        }

        private String getVal() {
            return val;
        }
    }
}
