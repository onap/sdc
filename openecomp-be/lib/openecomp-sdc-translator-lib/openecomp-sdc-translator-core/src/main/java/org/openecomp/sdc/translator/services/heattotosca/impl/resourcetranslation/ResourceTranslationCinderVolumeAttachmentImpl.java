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
package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_INVALID_INSTANCE_UUID;
import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_UNSUPPORTED_VOLUME_ATTACHMENT_MSG;
import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil.getResource;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.RelationshipTemplate;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.datatypes.ToscaTopologyTemplateElements;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.ResourceFileDataAndIDs;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationDataUtil;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.errors.MissingMandatoryPropertyErrorBuilder;
import org.openecomp.sdc.translator.services.heattotosca.helper.VolumeTranslationHelper;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;

public class ResourceTranslationCinderVolumeAttachmentImpl extends ResourceTranslationBase {

    protected static Logger logger = LoggerFactory.getLogger(ResourceTranslationCinderVolumeAttachmentImpl.class);

    @Override
    protected void translate(TranslateTo translateTo) {
        RelationshipTemplate relationTemplate = new RelationshipTemplate();
        relationTemplate.setType(ToscaRelationshipType.CINDER_VOLUME_ATTACHES_TO);
        String heatFileName = translateTo.getHeatFileName();
        relationTemplate.setProperties(TranslatorHeatToToscaPropertyConverter
            .getToscaPropertiesSimpleConversion(translateTo.getServiceTemplate(), translateTo.getResourceId(),
                translateTo.getResource().getProperties(), relationTemplate.getProperties(), heatFileName, translateTo.getHeatOrchestrationTemplate(),
                translateTo.getResource().getType(), relationTemplate, translateTo.getContext()));
        String volumeIdPropertyName = HeatConstants.VOL_ID_PROPERTY_NAME;
        AttachedResourceId attachedVolumeId = getAttachedResourceId(translateTo, volumeIdPropertyName);
        String instanceUuid = HeatConstants.INSTANCE_UUID_PROPERTY_NAME;
        AttachedResourceId attachedNovaServerId = getAttachedResourceId(translateTo, instanceUuid);
        if (attachedNovaServerId.isGetResource()) {
            handleNovaGetResource(translateTo, relationTemplate, attachedVolumeId, (String) attachedNovaServerId.getEntityId());
        } else {
            logger.warn(LOG_INVALID_INSTANCE_UUID, translateTo.getResourceId(), translateTo.getResource().getType());
        }
    }

    @Override
    protected Optional<ToscaTopologyTemplateElements> getTranslatedToscaTopologyElement(TranslateTo translateTo) {
        if (isEssentialRequirementsValid(translateTo)) {
            return Optional.of(ToscaTopologyTemplateElements.RELATIONSHIP_TEMPLATE);
        } else {
            return Optional.empty();
        }
    }

    private AttachedResourceId getAttachedResourceId(TranslateTo translateTo, String propertyName) {
        Optional<AttachedResourceId> attachedResourceId = HeatToToscaUtil.extractAttachedResourceId(translateTo, propertyName);
        if (!attachedResourceId.isPresent()) {
            throw new CoreException(new MissingMandatoryPropertyErrorBuilder(propertyName).build());
        }
        return attachedResourceId.get();
    }

    private void handleNovaGetResource(TranslateTo translateTo, RelationshipTemplate relationTemplate, AttachedResourceId volResourceId,
                                       String novaResourceId) {
        RequirementAssignment requirement = new RequirementAssignment();
        String toscaCapabilityAttachment = ToscaCapabilityType.NATIVE_ATTACHMENT;
        requirement.setCapability(toscaCapabilityAttachment);
        if (volResourceId.isGetResource()) {
            createVolumeNovaRelationshipForVolGetResource(translateTo, relationTemplate, volResourceId, requirement);
        } else if (volResourceId.isGetParam() && volResourceId.getEntityId() instanceof String) {
            createVolumeNovaRelationshipForVolGetParam(translateTo, relationTemplate, volResourceId, requirement);
        }
        translateNovaServerResource(translateTo, novaResourceId, requirement);
    }

    private void translateNovaServerResource(TranslateTo translateTo, String novaResourceId, RequirementAssignment requirement) {
        Resource novaServerResource = getResource(translateTo.getHeatOrchestrationTemplate(), novaResourceId, translateTo.getHeatFileName());
        if (!StringUtils.equals(HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(), novaServerResource.getType())) {
            logger.warn(LOG_UNSUPPORTED_VOLUME_ATTACHMENT_MSG, translateTo.getResourceId(), novaServerResource.getType(),
                HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource());
            return;
        }
        Optional<String> translatedNovaServerId = ResourceTranslationFactory.getInstance(novaServerResource)
            .translateResource(translateTo.getHeatFileName(), translateTo.getServiceTemplate(), translateTo.getHeatOrchestrationTemplate(),
                novaServerResource, novaResourceId, translateTo.getContext());
        if (translatedNovaServerId.isPresent() && StringUtils.isNotEmpty(requirement.getNode())) {
            NodeTemplate novaServerNodeTemplate = DataModelUtil.getNodeTemplate(translateTo.getServiceTemplate(), translatedNovaServerId.get());
            DataModelUtil.addRequirementAssignment(novaServerNodeTemplate, ToscaConstants.LOCAL_STORAGE_REQUIREMENT_ID, requirement);
            //Add volume information to consolidation data
            ConsolidationDataUtil.updateComputeConsolidationDataVolumes(translateTo, novaServerNodeTemplate.getType(), translatedNovaServerId.get(),
                ToscaConstants.LOCAL_STORAGE_REQUIREMENT_ID, requirement);
        }
    }

    private void createVolumeNovaRelationshipForVolGetParam(TranslateTo translateTo, RelationshipTemplate relationTemplate,
                                                            AttachedResourceId volResourceId, RequirementAssignment requirement) {
        String volumeResourceIdParamName = (String) volResourceId.getEntityId();
        if (translateTo.getContext().getHeatSharedResourcesByParam().containsKey(volumeResourceIdParamName) && !isHeatFileNested(translateTo,
            translateTo.getHeatFileName())) {
            handleSharedVolume(translateTo, relationTemplate, requirement, volumeResourceIdParamName);
        } else {
            handleUnsharedVolume(translateTo, relationTemplate, requirement, volumeResourceIdParamName);
        }
    }

    private void handleSharedVolume(TranslateTo translateTo, RelationshipTemplate relationTemplate, RequirementAssignment requirement,
                                    String volumeResourceIdParamName) {
        Resource volServerResource = translateTo.getContext().getHeatSharedResourcesByParam().get(volumeResourceIdParamName).getHeatResource();
        if (!StringUtils.equals(HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource(), volServerResource.getType())) {
            logger.warn(LOG_UNSUPPORTED_VOLUME_ATTACHMENT_MSG, translateTo.getResourceId(), volServerResource.getType(),
                HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource());
            return;
        }
        requirement.setNode(translateTo.getContext().getHeatSharedResourcesByParam().get(volumeResourceIdParamName).getTranslatedId());
        requirement.setRelationship(translateTo.getTranslatedId());
        DataModelUtil.addRelationshipTemplate(translateTo.getServiceTemplate(), translateTo.getTranslatedId(), relationTemplate);
    }

    private void createVolumeNovaRelationshipForVolGetResource(TranslateTo translateTo, RelationshipTemplate relationTemplate,
                                                               AttachedResourceId volResourceId, RequirementAssignment requirement) {
        Resource volServerResource = getResource(translateTo.getHeatOrchestrationTemplate(), (String) volResourceId.getTranslatedId(),
            translateTo.getHeatFileName());
        if (!StringUtils.equals(HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource(), volServerResource.getType())) {
            logger.warn(LOG_UNSUPPORTED_VOLUME_ATTACHMENT_MSG, translateTo.getResourceId(), volServerResource.getType(),
                HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource());
            return;
        }
        requirement.setNode((String) volResourceId.getTranslatedId());
        requirement.setRelationship(translateTo.getTranslatedId());
        DataModelUtil.addRelationshipTemplate(translateTo.getServiceTemplate(), translateTo.getTranslatedId(), relationTemplate);
    }

    private void handleUnsharedVolume(TranslateTo translateTo, RelationshipTemplate relationTemplate, RequirementAssignment requirement,
                                      String volumeResourceId) {
        List<FileData> allFilesData = translateTo.getContext().getManifest().getContent().getData();
        Optional<FileData> fileData = HeatToToscaUtil.getFileData(translateTo.getHeatFileName(), allFilesData);
        if (fileData.isPresent()) {
            Optional<ResourceFileDataAndIDs> fileDataContainingResource = new VolumeTranslationHelper(logger)
                .getFileDataContainingVolume(fileData.get().getData(), volumeResourceId, translateTo, FileData.Type.HEAT_VOL);
            fileDataContainingResource.ifPresent(
                resourceFileDataAndIDs -> addRelationshipToServiceTemplate(translateTo, relationTemplate, requirement, resourceFileDataAndIDs));
        }
    }

    private boolean isHeatFileNested(TranslateTo translateTo, String heatFileName) {
        return translateTo.getContext().getNestedHeatsFiles().contains(heatFileName);
    }

    private void addRelationshipToServiceTemplate(TranslateTo translateTo, RelationshipTemplate relationTemplate, RequirementAssignment requirement,
                                                  ResourceFileDataAndIDs resourceFileDataAndIDs) {
        String translatedId = resourceFileDataAndIDs.getTranslatedResourceId();
        String toscaVolIdPropName = HeatToToscaUtil.getToscaPropertyName(translateTo, HeatConstants.VOL_ID_PROPERTY_NAME);
        relationTemplate.getProperties().put(toscaVolIdPropName, translatedId);
        requirement.setNode(translatedId);
        requirement.setRelationship(translateTo.getTranslatedId());
        DataModelUtil.addRelationshipTemplate(translateTo.getServiceTemplate(), translateTo.getTranslatedId(), relationTemplate);
    }
}
