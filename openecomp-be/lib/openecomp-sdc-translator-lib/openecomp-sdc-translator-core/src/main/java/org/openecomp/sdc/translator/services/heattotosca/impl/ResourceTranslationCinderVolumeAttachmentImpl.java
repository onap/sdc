/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 */

package org.openecomp.sdc.translator.services.heattotosca.impl;

import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil.getResource;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.RelationshipTemplate;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.ResourceFileDataAndIDs;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.errors.MissingMandatoryPropertyErrorBuilder;
import org.openecomp.sdc.translator.services.heattotosca.helper.VolumeTranslationHelper;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class ResourceTranslationCinderVolumeAttachmentImpl extends ResourceTranslationBase {
  protected static Logger logger =
      LoggerFactory.getLogger(ResourceTranslationCinderVolumeAttachmentImpl.class);

  @Override
  protected void translate(TranslateTo translateTo) {
    String volumeIdPropertyName = "volume_id";
    RelationshipTemplate relationTemplate = new RelationshipTemplate();
    relationTemplate.setType(ToscaRelationshipType.CINDER_VOLUME_ATTACHES_TO.getDisplayName());
    String relationshipTemplateId = translateTo.getTranslatedId();
    String heatFileName = translateTo.getHeatFileName();
    relationTemplate.setProperties(TranslatorHeatToToscaPropertyConverter
        .getToscaPropertiesSimpleConversion(translateTo.getResource().getProperties(),
            relationTemplate.getProperties(), heatFileName,
            translateTo.getHeatOrchestrationTemplate(), translateTo.getResource().getType(),
            relationTemplate, translateTo.getContext()));

    AttachedResourceId attachedVolumeId = getAttachedResourceId(translateTo, volumeIdPropertyName);
    String instanceUuid = "instance_uuid";
    AttachedResourceId attachedNovaServerId = getAttachedResourceId(translateTo, instanceUuid);

    if (attachedNovaServerId.isGetResource()) {
      handleNovaGetResource(translateTo, relationTemplate, relationshipTemplateId, heatFileName,
          attachedVolumeId, (String) attachedNovaServerId.getEntityId());
    } else {
      logger.warn("Heat resource: '" + translateTo.getResourceId() + "' with type: '"
          + translateTo.getResource().getType()
          + "' include 'instance_uuid' property without 'get_resource' function, therefore this "
          + "resource will be ignored in TOSCA translation.");
    }
  }

  private AttachedResourceId getAttachedResourceId(TranslateTo translateTo, String propertyName) {
    Optional<AttachedResourceId> attachedResourceId =
        HeatToToscaUtil.extractAttachedResourceId(translateTo, propertyName);
    if (!attachedResourceId.isPresent()) {
      throw new CoreException(new MissingMandatoryPropertyErrorBuilder(propertyName).build());
    }

    return attachedResourceId.get();
  }

  private void handleNovaGetResource(TranslateTo translateTo, RelationshipTemplate relationTemplate,
                                     String relationshipTemplateId, String heatFileName,
                                     AttachedResourceId volResourceId, String novaResourceId) {
    String toscaCapabilityAttachment = "tosca.capabilities.Attachment";
    RequirementAssignment requirement = new RequirementAssignment();
    requirement.setCapability(toscaCapabilityAttachment);
    if (volResourceId.isGetResource()) {
      Resource volServerResource = getResource(translateTo.getHeatOrchestrationTemplate(),
          (String) volResourceId.getTranslatedId(), heatFileName);
      if (!StringUtils.equals(HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource(),
          volServerResource.getType())) {
        logger.warn("Volume attachment with id '" + translateTo.getResourceId()
            + "' is pointing to unsupported resource type(" + volServerResource.getType()
            + ") through the property 'volume_id'."
            + " The connection to the volume is ignored. "
            + "Supported types are: "
            + HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource());
        return;
      }
      requirement.setNode((String) volResourceId.getTranslatedId());
      requirement.setRelationship(relationshipTemplateId);
      DataModelUtil
          .addRelationshipTemplate(translateTo.getServiceTemplate(), relationshipTemplateId,
              relationTemplate);
    } else if (volResourceId.isGetParam()) {
      String volumeResourceIdParamName = (String) volResourceId.getEntityId();
      if (translateTo.getContext().getHeatSharedResourcesByParam()
          .containsKey(volumeResourceIdParamName) && !isHeatFileNested(translateTo, heatFileName)) {
        Resource volServerResource =
            translateTo.getContext().getHeatSharedResourcesByParam().get(volumeResourceIdParamName)
                .getHeatResource();
        if (!StringUtils.equals(HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource(),
            volServerResource.getType())) {
          logger.warn("Volume attachment with id '" + translateTo.getResourceId()
              + "' is pointing to unsupported resource type(" + volServerResource.getType()
              + ") through the property 'volume_id'."
              + " The connection to the volume is ignored. "
              + "Supported types are: "
              + HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource());
          return;
        }
        requirement.setNode(
            translateTo.getContext().getHeatSharedResourcesByParam().get(volumeResourceIdParamName)
                .getTranslatedId());
        requirement.setRelationship(relationshipTemplateId);
        DataModelUtil
            .addRelationshipTemplate(translateTo.getServiceTemplate(), relationshipTemplateId,
                relationTemplate);
      } else {
        handleUnsharedVolume(translateTo, relationTemplate, relationshipTemplateId, heatFileName,
            requirement, volumeResourceIdParamName);
      }
    }
    Resource novaServerResource =
        getResource(translateTo.getHeatOrchestrationTemplate(), novaResourceId, heatFileName);
    if (!StringUtils.equals(HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource(),
        novaServerResource.getType())) {
      logger.warn("Volume attachment with id '" + translateTo.getResourceId()
          + "' is pointing to unsupported resource type(" + novaServerResource.getType()
          + ") through the property 'instance_uuid'."
          + " The connection to the nova server is ignored. "
          + "Supported types are: " + HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE
          .getHeatResource());
      return;
    }
    Optional<String> translatedNovaServerId =
        ResourceTranslationFactory.getInstance(novaServerResource)
            .translateResource(heatFileName, translateTo.getServiceTemplate(),
                translateTo.getHeatOrchestrationTemplate(), novaServerResource, novaResourceId,
                translateTo.getContext());

    if (translatedNovaServerId.isPresent() && StringUtils.isNotEmpty(requirement.getNode())) {
      NodeTemplate novaServerNodeTemplate = DataModelUtil
          .getNodeTemplate(translateTo.getServiceTemplate(), translatedNovaServerId.get());
      DataModelUtil.addRequirementAssignment(novaServerNodeTemplate, "local_storage", requirement);
    }
  }

  private void handleUnsharedVolume(TranslateTo translateTo, RelationshipTemplate relationTemplate,
                                    String relationshipTemplateId, String heatFileName,
                                    RequirementAssignment requirement, String volumeResourceId) {
    List<FileData> allFilesData = translateTo.getContext().getManifest().getContent().getData();
    Optional<FileData> fileData = HeatToToscaUtil.getFileData(heatFileName, allFilesData);
    if (fileData.isPresent()) {
      Optional<ResourceFileDataAndIDs> fileDataContainingResource =
          new VolumeTranslationHelper(logger)
              .getFileDataContainingVolume(fileData.get().getData(), volumeResourceId, translateTo,
                  FileData.Type.HEAT_VOL);
      if (fileDataContainingResource.isPresent()) {
        addRelationshipToServiceTemplate(translateTo, relationTemplate, relationshipTemplateId,
            requirement, fileDataContainingResource.get());
      }
    }
  }

  private boolean isHeatFileNested(TranslateTo translateTo, String heatFileName) {
    return translateTo.getContext().getNestedHeatsFiles().contains(heatFileName);
  }

  private void addRelationshipToServiceTemplate(TranslateTo translateTo,
                                                RelationshipTemplate relationTemplate,
                                                String relationshipTemplateId,
                                                RequirementAssignment requirement,
                                                ResourceFileDataAndIDs resourceFileDataAndIDs) {
    String translatedId = resourceFileDataAndIDs.getTranslatedResourceId();
    relationTemplate.getProperties().put("volume_id", translatedId);
    requirement.setNode(translatedId);
    requirement.setRelationship(relationshipTemplateId);
    DataModelUtil.addRelationshipTemplate(translateTo.getServiceTemplate(), relationshipTemplateId,
        relationTemplate);
  }
}
