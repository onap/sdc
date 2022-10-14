/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.sdc.vendorsoftwareproduct.impl;

import static org.openecomp.sdc.tosca.datatypes.ToscaNodeType.COMPUTE_TYPE_PREFIX;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManager;
import org.openecomp.sdc.vendorsoftwareproduct.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.NicManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.CompositionEditNotAllowedErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;
import org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator.SchemaGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.ComponentCompositionSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.ComponentQuestionnaireSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateInput;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;

public class ComponentManagerImpl implements ComponentManager {

    private final ComponentDao componentDao;
    private final CompositionEntityDataManager compositionEntityDataManager;
    private final NicManager nicManager;
    private final VendorSoftwareProductInfoDao vspInfoDao;

    public ComponentManagerImpl(ComponentDao componentDao, CompositionEntityDataManager compositionEntityDataManager, NicManager nicManager,
                                VendorSoftwareProductInfoDao vspInfoDao) {
        this.componentDao = componentDao;
        this.compositionEntityDataManager = compositionEntityDataManager;
        this.nicManager = nicManager;
        this.vspInfoDao = vspInfoDao;
    }

    @Override
    public Collection<ComponentEntity> listComponents(String vspId, Version version) {
        return componentDao.list(new ComponentEntity(vspId, version, null));
    }

    @Override
    public void deleteComponents(String vspId, Version version) {
        if (!vspInfoDao.isManual(vspId, version)) {
            throw new CoreException(new CompositionEditNotAllowedErrorBuilder(vspId, version).build());
        }
    }

    @Override
    public ComponentEntity createComponent(ComponentEntity component) {
        final String vfcAddNotAllowedInHeatOnboardingMsg = "VFCs cannot be added for VSPs onboarded with HEAT.";
        ComponentEntity createdComponent;
        if (!vspInfoDao.isManual(component.getVspId(), component.getVersion())) {
            throw new CoreException(new ErrorCode.ErrorCodeBuilder().withId(VendorSoftwareProductErrorCodes.VFC_ADD_NOT_ALLOWED_IN_HEAT_ONBOARDING)
                .withMessage(vfcAddNotAllowedInHeatOnboardingMsg).build());
        } else {
            validateComponentManual(component);
            updateComponentName(component);
            createdComponent = compositionEntityDataManager.createComponent(component, true);
        }
        return createdComponent;
    }

    private void updateComponentName(ComponentEntity component) {
        ComponentData data = component.getComponentCompositionData();
        data.setName(COMPUTE_TYPE_PREFIX + data.getDisplayName());
        component.setComponentCompositionData(data);
    }

    private void validateComponentManual(ComponentEntity component) {
        final String vspVfcCountExceedMsg = "Creation of only one VFC per " + "VSP allowed.";
        final String vspVfcDuplicateNameMsg = "VFC with specified name " + "already present in given VSP.";
        Collection<ComponentEntity> vspComponentList = listComponents(component.getVspId(), component.getVersion());
        if (!vspComponentList.isEmpty()) {
            //1707 release only supports 1 VFC in VSP (manual creation)
            throw new CoreException(
                new ErrorCode.ErrorCodeBuilder().withId(VendorSoftwareProductErrorCodes.VSP_VFC_COUNT_EXCEED).withMessage(vspVfcCountExceedMsg)
                    .build());
        }
        if (!isVfcNameUnique(vspComponentList, component.getComponentCompositionData().getDisplayName())) {
            throw new CoreException(
                new ErrorCode.ErrorCodeBuilder().withId(VendorSoftwareProductErrorCodes.VSP_VFC_DUPLICATE_NAME).withMessage(vspVfcDuplicateNameMsg)
                    .build());
        }
    }

    private boolean isVfcNameUnique(Collection<ComponentEntity> component, String displayName) {
        for (ComponentEntity comp : component) {
            if (comp.getComponentCompositionData().getDisplayName().equalsIgnoreCase(displayName)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public CompositionEntityValidationData updateComponent(ComponentEntity component) {
        ComponentEntity retrieved = getValidatedComponent(component.getVspId(), component.getVersion(), component.getId());
        boolean isManual = vspInfoDao.isManual(component.getVspId(), component.getVersion());
        if (isManual) {
            validateComponentUpdateManual(retrieved);
        }
        ComponentCompositionSchemaInput schemaInput = new ComponentCompositionSchemaInput();
        schemaInput.setManual(isManual);
        schemaInput.setComponent(retrieved.getComponentCompositionData());
        CompositionEntityValidationData validationData = compositionEntityDataManager
            .validateEntity(component, SchemaTemplateContext.composition, schemaInput);
        if (CollectionUtils.isEmpty(validationData.getErrors())) {
            if (isManual) {
                updateComponentName(component);
            }
            componentDao.update(component);
        }
        return validationData;
    }

    private void validateComponentUpdateManual(ComponentEntity component) {
        Collection<ComponentEntity> vspComponentList = listComponents(component.getVspId(), component.getVersion());
        //VFC name should be unique within VSP

        //Removing VFC with same ID from list to avoid self compare
        for (ComponentEntity ce : vspComponentList) {
            if (ce.getId().equals(component.getId())) {
                vspComponentList.remove(ce);
                break;
            }
        }
        if (!isVfcNameUnique(vspComponentList, component.getComponentCompositionData().getDisplayName())) {
            throw new CoreException(new ErrorCode.ErrorCodeBuilder().withId(VendorSoftwareProductErrorCodes.VSP_VFC_DUPLICATE_NAME)
                .withMessage("VFC with specified name already present in given VSP.").build());
        }
    }

    @Override
    public CompositionEntityResponse<ComponentData> getComponent(String vspId, Version version, String componentId) {
        ComponentEntity componentEntity = getValidatedComponent(vspId, version, componentId);
        ComponentData component = componentEntity.getComponentCompositionData();
        ComponentCompositionSchemaInput schemaInput = new ComponentCompositionSchemaInput();
        schemaInput.setManual(vspInfoDao.isManual(vspId, version));
        schemaInput.setComponent(component);
        CompositionEntityResponse<ComponentData> response = new CompositionEntityResponse<>();
        response.setId(componentId);
        response.setData(component);
        response.setSchema(getComponentCompositionSchema(schemaInput));
        return response;
    }

    @Override
    public void deleteComponent(String vspId, Version version, String componentId) {
        if (!vspInfoDao.isManual(vspId, version)) {
            throw new CoreException(new CompositionEditNotAllowedErrorBuilder(vspId, version).build());
        }
    }

    @Override
    public QuestionnaireResponse getQuestionnaire(String vspId, Version version, String componentId) {
        QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
        ComponentEntity component = componentDao.getQuestionnaireData(vspId, version, componentId);
        VersioningUtil.validateEntityExistence(component, new ComponentEntity(vspId, version, componentId), VspDetails.ENTITY_TYPE);
        questionnaireResponse.setData(component.getQuestionnaireData());
        List<String> nicNames = nicManager.listNics(vspId, version, componentId).stream().map(nic -> nic.getNicCompositionData().getName())
            .collect(Collectors.toList());
        questionnaireResponse.setSchema(getComponentQuestionnaireSchema(new ComponentQuestionnaireSchemaInput(nicNames,
            questionnaireResponse.getData() == null ? null : JsonUtil.json2Object(questionnaireResponse.getData(), Map.class), null, false)));
        return questionnaireResponse;
    }

    @Override
    public void updateQuestionnaire(String vspId, Version version, String componentId, String questionnaireData) {
        validateComponentExistence(vspId, version, componentId);
        componentDao.updateQuestionnaireData(vspId, version, componentId, questionnaireData);
    }

    @Override
    public void validateComponentExistence(String vspId, Version version, String componentId) {
        getValidatedComponent(vspId, version, componentId);
    }

    private ComponentEntity getValidatedComponent(String vspId, Version version, String componentId) {
        ComponentEntity retrieved = componentDao.get(new ComponentEntity(vspId, version, componentId));
        VersioningUtil.validateEntityExistence(retrieved, new ComponentEntity(vspId, version, componentId), VspDetails.ENTITY_TYPE);
        return retrieved;
    }

    protected String getComponentCompositionSchema(ComponentCompositionSchemaInput schemaInput) {
        return SchemaGenerator.generate(SchemaTemplateContext.composition, CompositionEntityType.component, schemaInput);
    }

    protected String getComponentQuestionnaireSchema(SchemaTemplateInput schemaInput) {
        return SchemaGenerator.generate(SchemaTemplateContext.questionnaire, CompositionEntityType.component, schemaInput);
    }
}
