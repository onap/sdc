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
package org.openecomp.sdc.vendorsoftwareproduct.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.dao.UniqueValueDaoFactory;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.json.JsonSchemaDataGenerator;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.vendorsoftwareproduct.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComputeManager;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.NotSupportedHeatOnboardMethodErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;
import org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator.SchemaGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ListComputeResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentComputeAssociation;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComputeData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.ComputeCompositionSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateInput;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;

public class ComputeManagerImpl implements ComputeManager {

    private final ComputeDao computeDao;
    private final CompositionEntityDataManager compositionEntityDataManager;
    private final VendorSoftwareProductInfoDao vspInfoDao;
    private final DeploymentFlavorDao deploymentFlavorDao;

    public ComputeManagerImpl(VendorSoftwareProductInfoDao vspInfoDao, ComputeDao computeDao,
                              CompositionEntityDataManager compositionEntityDataManager, DeploymentFlavorDao deploymentFlavorDao) {
        this.computeDao = computeDao;
        this.compositionEntityDataManager = compositionEntityDataManager;
        this.vspInfoDao = vspInfoDao;
        this.deploymentFlavorDao = deploymentFlavorDao;
    }

    @Override
    public ComputeEntity createCompute(ComputeEntity compute) {
        if (!vspInfoDao.isManual(compute.getVspId(), compute.getVersion())) {
            ErrorCode onboardingMethodUpdateErrorCode = NotSupportedHeatOnboardMethodErrorBuilder
                .getAddComputeNotSupportedHeatOnboardMethodErrorBuilder();
            throw new CoreException(onboardingMethodUpdateErrorCode);
        } else {
            validateUniqueName(compute.getVspId(), compute.getVersion(), compute.getComponentId(), compute.getComputeCompositionData().getName());
            compute.setQuestionnaireData(new JsonSchemaDataGenerator(getComputeQuestionnaireSchema(null)).generateData());
            computeDao.create(compute);
            createUniqueName(compute.getVspId(), compute.getVersion(), compute.getComponentId(), compute.getComputeCompositionData().getName());
        }
        return compute;
    }

    @Override
    public Collection<ListComputeResponse> listComputes(String vspId, Version version, String componentId) {
        Collection<ComputeEntity> computes = computeDao.list(new ComputeEntity(vspId, version, componentId, null));
        return getListComputeResponse(vspId, version, computes);
    }

    private Collection<ListComputeResponse> getListComputeResponse(String vspId, Version version, Collection<ComputeEntity> computes) {
        Set<String> vspComputes = getComputeAssociatedWithDepFlavors(vspId, version);
        Collection<ListComputeResponse> computeResponse = new ArrayList<>();
        for (ComputeEntity computeEntity : computes) {
            ListComputeResponse response = new ListComputeResponse();
            response.setComputeEntity(computeEntity);
            if (vspComputes.contains(computeEntity.getId())) {
                response.setAssociatedWithDeploymentFlavor(true);
            } else {
                response.setAssociatedWithDeploymentFlavor(false);
            }
            computeResponse.add(response);
        }
        return computeResponse;
    }

    private Set<String> getComputeAssociatedWithDepFlavors(String vspId, Version version) {
        final Collection<DeploymentFlavorEntity> deploymentFlavorEntities = deploymentFlavorDao
            .list(new DeploymentFlavorEntity(vspId, version, null));
        Set<String> vspComputes = new HashSet<>();
        for (DeploymentFlavorEntity entity : deploymentFlavorEntities) {
            final List<ComponentComputeAssociation> componentComputeAssociations = entity.getDeploymentFlavorCompositionData()
                .getComponentComputeAssociations();
            if (componentComputeAssociations != null && !componentComputeAssociations.isEmpty()) {
                for (ComponentComputeAssociation association : componentComputeAssociations) {
                    vspComputes.add(association.getComputeFlavorId());
                }
            }
        }
        return vspComputes;
    }

    @Override
    public CompositionEntityResponse<ComputeData> getCompute(String vspId, Version version, String componentId, String computeFlavorId) {
        ComputeEntity computeEntity = getValidatedCompute(vspId, version, componentId, computeFlavorId);
        ComputeData compute = computeEntity.getComputeCompositionData();
        ComputeCompositionSchemaInput schemaInput = new ComputeCompositionSchemaInput();
        schemaInput.setManual(vspInfoDao.isManual(vspId, version));
        schemaInput.setCompute(compute);
        CompositionEntityResponse<ComputeData> response = new CompositionEntityResponse<>();
        response.setId(computeFlavorId);
        response.setData(compute);
        response.setSchema(getComputeCompositionSchema(schemaInput));
        return response;
    }

    private ComputeEntity getValidatedCompute(String vspId, Version version, String componentId, String computeFlavorId) {
        ComputeEntity retrieved = computeDao.get(new ComputeEntity(vspId, version, componentId, computeFlavorId));
        VersioningUtil.validateEntityExistence(retrieved, new ComputeEntity(vspId, version, componentId, computeFlavorId), VspDetails.ENTITY_TYPE);
        return retrieved;
    }

    @Override
    public QuestionnaireResponse getComputeQuestionnaire(String vspId, Version version, String componentId, String computeId) {
        QuestionnaireResponse questionnaireResponse = new QuestionnaireResponse();
        ComputeEntity computeQuestionnaire = computeDao.getQuestionnaireData(vspId, version, componentId, computeId);
        VersioningUtil
            .validateEntityExistence(computeQuestionnaire, new ComputeEntity(vspId, version, componentId, computeId), VspDetails.ENTITY_TYPE);
        questionnaireResponse.setData(computeQuestionnaire.getQuestionnaireData());
        questionnaireResponse.setSchema(getComputeQuestionnaireSchema(null));
        return questionnaireResponse;
    }

    @Override
    public void updateComputeQuestionnaire(String vspId, Version version, String componentId, String computeId, String questionnaireData) {
        ComputeEntity retrieved = computeDao.get(new ComputeEntity(vspId, version, componentId, computeId));
        VersioningUtil.validateEntityExistence(retrieved, new ComputeEntity(vspId, version, componentId, computeId), VspDetails.ENTITY_TYPE);
        computeDao.updateQuestionnaireData(vspId, version, componentId, computeId, questionnaireData);
    }

    @Override
    public CompositionEntityValidationData updateCompute(ComputeEntity compute) {
        ComputeEntity retrieved = getComputeEntity(compute.getVspId(), compute.getVersion(), compute.getComponentId(), compute.getId());
        boolean manual = vspInfoDao.isManual(compute.getVspId(), compute.getVersion());
        ComputeCompositionSchemaInput schemaInput = new ComputeCompositionSchemaInput();
        schemaInput.setManual(manual);
        schemaInput.setCompute(retrieved.getComputeCompositionData());
        CompositionEntityValidationData validationData = compositionEntityDataManager
            .validateEntity(compute, SchemaTemplateContext.composition, schemaInput);
        if (CollectionUtils.isEmpty(validationData.getErrors())) {
            updateUniqueName(compute.getVspId(), compute.getVersion(), compute.getComponentId(), retrieved.getComputeCompositionData().getName(),
                compute.getComputeCompositionData().getName());
            computeDao.update(compute);
        }
        return validationData;
    }

    private ComputeEntity getComputeEntity(String vspId, Version version, String componentId, String computeId) {
        ComputeEntity computeEntity = computeDao.get(new ComputeEntity(vspId, version, componentId, computeId));
        VersioningUtil.validateEntityExistence(computeEntity, new ComputeEntity(vspId, version, componentId, computeId), VspDetails.ENTITY_TYPE);
        return computeEntity;
    }

    @Override
    public void deleteCompute(String vspId, Version version, String componentId, String computeFlavorId) {
        final String vspCompositionEditNotAllowedMsg =
            "Composition entities may not be created / deleted for Vendor Software Product " + "whose entities were uploaded";
        if (!vspInfoDao.isManual(vspId, version)) {
            throw new CoreException(new ErrorCode.ErrorCodeBuilder().withId(VendorSoftwareProductErrorCodes.VSP_COMPOSITION_EDIT_NOT_ALLOWED)
                .withMessage(vspCompositionEditNotAllowedMsg).build());
        }
        ComputeEntity retrieved = getValidatedCompute(vspId, version, componentId, computeFlavorId);
        if (retrieved != null) {
            deleteComputeFromDeploymentFlavors(vspId, version, computeFlavorId);
            computeDao.delete(new ComputeEntity(vspId, version, componentId, computeFlavorId));
            deleteUniqueValue(retrieved.getVspId(), retrieved.getVersion(), retrieved.getComponentId(),
                retrieved.getComputeCompositionData().getName());
        }
    }

    private void deleteComputeFromDeploymentFlavors(String vspId, Version version, String computeFlavorId) {
        Collection<DeploymentFlavorEntity> listDF = deploymentFlavorDao.list(new DeploymentFlavorEntity(vspId, version, null));
        for (DeploymentFlavorEntity df : listDF) {
            DeploymentFlavorEntity deploymentFlavorEntity = removeComputeFromDF(df, computeFlavorId);
            if (deploymentFlavorEntity != null) {
                deploymentFlavorDao.update(deploymentFlavorEntity);
            }
        }
    }

    private DeploymentFlavorEntity removeComputeFromDF(DeploymentFlavorEntity df, String computeFlavorId) {
        DeploymentFlavor flavor = df.getDeploymentFlavorCompositionData();
        List<ComponentComputeAssociation> associations = flavor.getComponentComputeAssociations();
        if (associations != null) {
            List<ComponentComputeAssociation> updatedAssociations = new ArrayList<>();
            for (ComponentComputeAssociation ca : associations) {
                if (ca.getComputeFlavorId() != null && ca.getComputeFlavorId().equals(computeFlavorId)) {
                    ComponentComputeAssociation updateCaremoveCompute = new ComponentComputeAssociation();
                    updateCaremoveCompute.setComponentId(ca.getComponentId());
                    updatedAssociations.add(updateCaremoveCompute);
                } else {
                    updatedAssociations.add(ca);
                }
            }
            flavor.setComponentComputeAssociations(updatedAssociations);
            df.setDeploymentFlavorCompositionData(flavor);
            return df;
        }
        return null;
    }

    protected String getComputeCompositionSchema(SchemaTemplateInput schemaInput) {
        return SchemaGenerator.generate(SchemaTemplateContext.composition, CompositionEntityType.compute, schemaInput);
    }

    protected String getComputeQuestionnaireSchema(SchemaTemplateInput schemaInput) {
        return SchemaGenerator.generate(SchemaTemplateContext.questionnaire, CompositionEntityType.compute, schemaInput);
    }

    protected void validateUniqueName(String vspId, Version version, String componentId, String name) {
        UniqueValueUtil uniqueValueUtil = new UniqueValueUtil(UniqueValueDaoFactory.getInstance().createInterface());
        uniqueValueUtil.validateUniqueValue(VendorSoftwareProductConstants.UniqueValues.COMPUTE_NAME, vspId, version.getId(), componentId, name);
    }

    protected void createUniqueName(String vspId, Version version, String componentId, String name) {
        UniqueValueUtil uniqueValueUtil = new UniqueValueUtil(UniqueValueDaoFactory.getInstance().createInterface());
        uniqueValueUtil.createUniqueValue(VendorSoftwareProductConstants.UniqueValues.COMPUTE_NAME, vspId, version.getId(), componentId, name);
    }

    protected void updateUniqueName(String vspId, Version version, String componentId, String oldName, String newName) {
        UniqueValueUtil uniqueValueUtil = new UniqueValueUtil(UniqueValueDaoFactory.getInstance().createInterface());
        uniqueValueUtil
            .updateUniqueValue(VendorSoftwareProductConstants.UniqueValues.COMPUTE_NAME, oldName, newName, vspId, version.getId(), componentId);
    }

    protected void deleteUniqueValue(String vspId, Version version, String componentId, String name) {
        UniqueValueUtil uniqueValueUtil = new UniqueValueUtil(UniqueValueDaoFactory.getInstance().createInterface());
        if (componentId == null) {
            uniqueValueUtil.deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.COMPUTE_NAME, vspId, version.getId(), name);
        }
        uniqueValueUtil.deleteUniqueValue(VendorSoftwareProductConstants.UniqueValues.COMPUTE_NAME, vspId, version.getId(), componentId, name);
    }
}
