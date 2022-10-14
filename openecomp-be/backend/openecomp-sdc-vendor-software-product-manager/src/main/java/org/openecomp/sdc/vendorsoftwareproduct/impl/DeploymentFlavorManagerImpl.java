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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.vendorsoftwareproduct.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.DeploymentFlavorManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.DeploymentFlavorErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.NotSupportedHeatOnboardMethodErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator.SchemaGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentComputeAssociation;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.DeploymentFlavorCompositionSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;

public class DeploymentFlavorManagerImpl implements DeploymentFlavorManager {

    private final VendorSoftwareProductInfoDao vspInfoDao;
    private final DeploymentFlavorDao deploymentFlavorDao;
    private final CompositionEntityDataManager compositionEntityDataManager;
    private final ComputeDao computeDao;
    private final ComponentDao componentDao;

    public DeploymentFlavorManagerImpl(VendorSoftwareProductInfoDao vspInfoDao, DeploymentFlavorDao deploymentFlavorDao,
                                       CompositionEntityDataManager compositionEntityDataManager, ComputeDao computeDao, ComponentDao componentDao) {
        this.vspInfoDao = vspInfoDao;
        this.deploymentFlavorDao = deploymentFlavorDao;
        this.compositionEntityDataManager = compositionEntityDataManager;
        this.computeDao = computeDao;
        this.componentDao = componentDao;
    }

    @Override
    public Collection<DeploymentFlavorEntity> listDeploymentFlavors(String vspId, Version version) {
        return deploymentFlavorDao.list(new DeploymentFlavorEntity(vspId, version, null));
    }

    @Override
    public DeploymentFlavorEntity createDeploymentFlavor(DeploymentFlavorEntity deploymentFlavorEntity) {
        DeploymentFlavorEntity createDeploymentFlavor;
        if (!vspInfoDao.isManual(deploymentFlavorEntity.getVspId(), deploymentFlavorEntity.getVersion())) {
            ErrorCode deploymentFlavorErrorBuilder = DeploymentFlavorErrorBuilder.getAddDeploymentNotSupportedHeatOnboardErrorBuilder();
            throw new CoreException(deploymentFlavorErrorBuilder);
        } else {
            validateDeploymentFlavor(deploymentFlavorEntity, deploymentFlavorEntity.getVersion());
            createDeploymentFlavor = compositionEntityDataManager.createDeploymentFlavor(deploymentFlavorEntity);
        }
        return createDeploymentFlavor;
    }

    private void validateDeploymentFlavor(DeploymentFlavorEntity deploymentFlavorEntity, Version version) {
        //Validation for unique model.
        Collection<DeploymentFlavorEntity> listDeploymentFlavors = listDeploymentFlavors(deploymentFlavorEntity.getVspId(), version);
        isDeploymentFlavorModelDuplicate(deploymentFlavorEntity, listDeploymentFlavors);
        List<String> featureGroups = getFeatureGroupListForVsp(deploymentFlavorEntity.getVspId(), version);
        String featureGroup = deploymentFlavorEntity.getDeploymentFlavorCompositionData().getFeatureGroupId();
        if (featureGroup != null && featureGroup.trim().length() > 0 && (isEmpty(featureGroups) || (!(validFeatureGroup(featureGroups,
            featureGroup))))) {
            ErrorCode deploymentFlavorErrorBuilder = DeploymentFlavorErrorBuilder.getFeatureGroupNotexistErrorBuilder();
            throw new CoreException(deploymentFlavorErrorBuilder);
        }
        validateComponentComputeAssociation(deploymentFlavorEntity, version);
    }

    private void isDeploymentFlavorModelDuplicate(DeploymentFlavorEntity deploymentFlavorEntity,
                                                  Collection<DeploymentFlavorEntity> listDeploymentFlavors) {
        listDeploymentFlavors.forEach(deploymentFlavor -> {
            if (deploymentFlavorEntity.getDeploymentFlavorCompositionData().getModel()
                .equalsIgnoreCase(deploymentFlavor.getDeploymentFlavorCompositionData().getModel())) {
                ErrorCode deploymentFlavorModelErrorBuilder = DeploymentFlavorErrorBuilder
                    .getDuplicateDeploymentFlavorModelErrorBuilder(deploymentFlavorEntity.getDeploymentFlavorCompositionData().getModel(),
                        deploymentFlavorEntity.getVspId());
                throw new CoreException(deploymentFlavorModelErrorBuilder);
            }
        });
    }

    private List<String> getFeatureGroupListForVsp(String vspId, Version version) {
        final VspDetails vspDetails = vspInfoDao.get(new VspDetails(vspId, version));
        return vspDetails.getFeatureGroups();
    }

    private boolean isEmpty(Collection coll) {
        return coll == null || coll.isEmpty();
    }

    private boolean validFeatureGroup(List<String> featureGroups, String featureGroupId) {
        Iterator<String> iterator = featureGroups.iterator();
        boolean valid = false;
        while (iterator.hasNext()) {
            String fgId = iterator.next().trim();
            if (fgId.equals(featureGroupId)) {
                valid = true;
                break;
            } else {
                valid = false;
            }
        }
        return valid;
    }

    private void validateComponentComputeAssociation(DeploymentFlavorEntity deploymentFlavorEntity, Version version) {
        List<ComponentComputeAssociation> componentComputeAssociationList = deploymentFlavorEntity.getDeploymentFlavorCompositionData()
            .getComponentComputeAssociations();
        List<String> vfcList = new ArrayList<>();
        if (!isEmpty(componentComputeAssociationList)) {
            componentComputeAssociationList.forEach(
                componentComputeAssociation -> validateComponentComputeAssocoationList(deploymentFlavorEntity, version, vfcList,
                    componentComputeAssociation));
            Map<String, Integer> frequencyMapping = CollectionUtils.getCardinalityMap(vfcList);
            for (Integer vfcCount : frequencyMapping.values()) {
                if (vfcCount != 1) {
                    ErrorCode duplicateVfcAssociationErrorBuilder = DeploymentFlavorErrorBuilder.getDuplicateVfcAssociationErrorBuilder();
                    throw new CoreException(duplicateVfcAssociationErrorBuilder);
                }
            }
        }
    }

    private void validateComponentComputeAssocoationList(DeploymentFlavorEntity deploymentFlavorEntity, Version version, List<String> vfcList,
                                                         ComponentComputeAssociation componentComputeAssociation) {
        if ((componentComputeAssociation.getComponentId() == null || componentComputeAssociation.getComponentId().trim().length() == 0) && (
            componentComputeAssociation.getComputeFlavorId() != null && componentComputeAssociation.getComputeFlavorId().trim().length() > 0)) {
            ErrorCode invalidAssociationErrorBuilder = DeploymentFlavorErrorBuilder.getInvalidAssociationErrorBuilder();
            throw new CoreException(invalidAssociationErrorBuilder);
        } else if (componentComputeAssociation.getComponentId() != null && componentComputeAssociation.getComponentId().trim().length() > 0) {
            validateComponentAssociation(deploymentFlavorEntity, version, componentComputeAssociation);
            validateComponentComputeAssociationFlavour(deploymentFlavorEntity, version, componentComputeAssociation);
            vfcList.add(componentComputeAssociation.getComponentId());
        }
    }

    private void validateComponentAssociation(DeploymentFlavorEntity deploymentFlavorEntity, Version version,
                                              ComponentComputeAssociation componentComputeAssociation) {
        if (StringUtils.isNotBlank(componentComputeAssociation.getComponentId())) {
            ComponentEntity componentEntity = componentDao
                .get(new ComponentEntity(deploymentFlavorEntity.getVspId(), version, componentComputeAssociation.getComponentId()));
            if (componentEntity == null) {
                ErrorCode invalidComputeIdErrorBuilder = DeploymentFlavorErrorBuilder.getInvalidComponentIdErrorBuilder();
                throw new CoreException(invalidComputeIdErrorBuilder);
            }
        }
    }

    private void validateComponentComputeAssociationFlavour(DeploymentFlavorEntity deploymentFlavorEntity, Version version,
                                                            ComponentComputeAssociation componentComputeAssociation) {
        if (componentComputeAssociation.getComputeFlavorId() != null && componentComputeAssociation.getComputeFlavorId().trim().length() > 0) {
            ComputeEntity computeFlavor = computeDao.get(
                new ComputeEntity(deploymentFlavorEntity.getVspId(), version, componentComputeAssociation.getComponentId(),
                    componentComputeAssociation.getComputeFlavorId()));
            if (computeFlavor == null) {
                ErrorCode invalidComputeIdErrorBuilder = DeploymentFlavorErrorBuilder.getInvalidComputeIdErrorBuilder();
                throw new CoreException(invalidComputeIdErrorBuilder);
            }
        }
    }

    @Override
    public CompositionEntityResponse<DeploymentFlavor> getDeploymentFlavor(String vspId, Version version, String deploymentFlavorId) {
        DeploymentFlavorEntity deploymentFlavorEntity = getValidatedDeploymentFlavor(vspId, version, deploymentFlavorId);
        DeploymentFlavor deploymentFlavor = deploymentFlavorEntity.getDeploymentFlavorCompositionData();
        DeploymentFlavorCompositionSchemaInput schemaInput = new DeploymentFlavorCompositionSchemaInput();
        schemaInput.setManual(vspInfoDao.isManual(vspId, version));
        schemaInput.setDeploymentFlavor(deploymentFlavor);
        List<String> featureGroups = getFeatureGroupListForVsp(vspId, version);
        schemaInput.setFeatureGroupIds(featureGroups);
        CompositionEntityResponse<DeploymentFlavor> response = new CompositionEntityResponse<>();
        response.setId(deploymentFlavorId);
        response.setSchema(SchemaGenerator.generate(SchemaTemplateContext.composition, CompositionEntityType.deployment, schemaInput));
        response.setData(deploymentFlavor);
        return response;
    }

    private DeploymentFlavorEntity getValidatedDeploymentFlavor(String vspId, Version version, String deploymentFlavorId) {
        DeploymentFlavorEntity retrieved = deploymentFlavorDao.get(new DeploymentFlavorEntity(vspId, version, deploymentFlavorId));
        VersioningUtil.validateEntityExistence(retrieved, new DeploymentFlavorEntity(vspId, version, deploymentFlavorId), VspDetails.ENTITY_TYPE);
        return retrieved;
    }

    @Override
    public CompositionEntityResponse<DeploymentFlavor> getDeploymentFlavorSchema(String vspId, Version version) {
        DeploymentFlavorCompositionSchemaInput schemaInput = new DeploymentFlavorCompositionSchemaInput();
        schemaInput.setManual(vspInfoDao.isManual(vspId, version));
        List<String> featureGroups = getFeatureGroupListForVsp(vspId, version);
        schemaInput.setFeatureGroupIds(featureGroups);
        CompositionEntityResponse<DeploymentFlavor> response = new CompositionEntityResponse<>();
        response.setSchema(SchemaGenerator.generate(SchemaTemplateContext.composition, CompositionEntityType.deployment, schemaInput));
        return response;
    }

    @Override
    public void deleteDeploymentFlavor(String vspId, Version version, String deploymentFlavorId) {
        DeploymentFlavorEntity deploymentFlavorEntity = getValidatedDeploymentFlavor(vspId, version, deploymentFlavorId);
        if (!vspInfoDao.isManual(vspId, version)) {
            final ErrorCode deleteDeploymentFlavorErrorBuilder = NotSupportedHeatOnboardMethodErrorBuilder
                .getDelDeploymentFlavorNotSupportedHeatOnboardMethodErrorBuilder();
            throw new CoreException(deleteDeploymentFlavorErrorBuilder);
        }
        if (deploymentFlavorEntity != null) {
            deploymentFlavorDao.delete(new DeploymentFlavorEntity(vspId, version, deploymentFlavorId));
        }
    }

    @Override
    public CompositionEntityValidationData updateDeploymentFlavor(DeploymentFlavorEntity deploymentFlavorEntity) {
        if (!vspInfoDao.isManual(deploymentFlavorEntity.getVspId(), deploymentFlavorEntity.getVersion())) {
            final ErrorCode updateDeploymentFlavorErrorBuilder = NotSupportedHeatOnboardMethodErrorBuilder
                .getUpdateDfNotSupportedHeatOnboardMethodErrorBuilder();
            throw new CoreException(updateDeploymentFlavorErrorBuilder);
        }
        DeploymentFlavorEntity retrieved = getValidatedDeploymentFlavor(deploymentFlavorEntity.getVspId(), deploymentFlavorEntity.getVersion(),
            deploymentFlavorEntity.getId());
        Collection<DeploymentFlavorEntity> listDeploymentFlavors = listDeploymentFlavors(deploymentFlavorEntity.getVspId(),
            deploymentFlavorEntity.getVersion());
        listDeploymentFlavors.remove(retrieved);
        isDeploymentFlavorModelDuplicate(deploymentFlavorEntity, listDeploymentFlavors);
        validateComponentComputeAssociation(deploymentFlavorEntity, deploymentFlavorEntity.getVersion());
        DeploymentFlavorCompositionSchemaInput schemaInput = new DeploymentFlavorCompositionSchemaInput();
        schemaInput.setManual(vspInfoDao.isManual(deploymentFlavorEntity.getVspId(), deploymentFlavorEntity.getVersion()));
        schemaInput.setDeploymentFlavor(retrieved.getDeploymentFlavorCompositionData());
        List<String> featureGroups = getFeatureGroupListForVsp(deploymentFlavorEntity.getVspId(), deploymentFlavorEntity.getVersion());
        schemaInput.setFeatureGroupIds(featureGroups);
        CompositionEntityValidationData validationData = compositionEntityDataManager
            .validateEntity(deploymentFlavorEntity, SchemaTemplateContext.composition, schemaInput);
        if (CollectionUtils.isEmpty(validationData.getErrors())) {
            deploymentFlavorDao.update(deploymentFlavorEntity);
        }
        return validationData;
    }
}
