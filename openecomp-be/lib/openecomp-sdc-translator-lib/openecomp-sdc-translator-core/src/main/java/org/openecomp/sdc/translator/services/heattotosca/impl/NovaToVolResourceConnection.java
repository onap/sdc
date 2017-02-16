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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.RequirementDefinition;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.ResourceFileDataAndIDs;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslatedHeatResource;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.services.heattotosca.errors.MissingMandatoryPropertyErrorBuilder;
import org.openecomp.sdc.translator.services.heattotosca.helper.VolumeTranslationHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class NovaToVolResourceConnection extends NovaAndPortResourceConnectionHelper {

  public NovaToVolResourceConnection(ResourceTranslationBase resourceTranslationBase,
                                     TranslateTo translateTo, FileData nestedFileData,
                                     NodeTemplate substitutionNodeTemplate, NodeType nodeType) {
    super(resourceTranslationBase, translateTo, nestedFileData, substitutionNodeTemplate, nodeType);
  }

  @Override
  boolean isDesiredNodeTemplateType(NodeTemplate nodeTemplate) {
    ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
    ToscaServiceModel toscaServiceModel = HeatToToscaUtil.getToscaServiceModel(translateTo);
    return toscaAnalyzerService.isTypeOf(nodeTemplate, ToscaNodeType.NOVA_SERVER.getDisplayName(),
        translateTo.getContext().getTranslatedServiceTemplates()
            .get(translateTo.getResource().getType()), toscaServiceModel);
  }

  @Override
  List<Predicate<RequirementDefinition>> getPredicatesListForConnectionPoints() {
    ArrayList<Predicate<RequirementDefinition>> predicates = new ArrayList<>();
    predicates
        .add(req -> req.getCapability().equals(ToscaCapabilityType.ATTACHMENT.getDisplayName())
            && req.getNode().equals(ToscaNodeType.BLOCK_STORAGE.getDisplayName())
            && req.getRelationship()
                .equals(ToscaRelationshipType.NATIVE_ATTACHES_TO.getDisplayName()));
    return predicates;
  }

  @Override
  Optional<List<String>> getConnectorParamName(
          String heatResourceId, Resource heatResource,
          HeatOrchestrationTemplate nestedHeatOrchestrationTemplate) {
    Optional<AttachedResourceId> volumeId = HeatToToscaUtil
        .extractAttachedResourceId(nestedFileData.getFile(), nestedHeatOrchestrationTemplate,
            translateTo.getContext(), heatResource.getProperties().get("volume_id"));
    if (volumeId.isPresent() && volumeId.get().isGetParam()) {
      return Optional.of(Collections.singletonList((String) volumeId.get().getEntityId()));
    } else {
      return Optional.empty();
    }
  }

  @Override
  String getDesiredResourceType() {
    return HeatResourcesTypes.CINDER_VOLUME_ATTACHMENT_RESOURCE_TYPE.getHeatResource();
  }

  @Override
  protected Optional<List<Map.Entry<String, Resource>>> getResourceByTranslatedResourceId(
      String translatedResourceId, HeatOrchestrationTemplate nestedHeatOrchestrationTemplate) {

    List<Predicate<Map.Entry<String, Resource>>> predicates =
        buildPredicates(nestedFileData.getFile(), nestedHeatOrchestrationTemplate,
            translatedResourceId);
    List<Map.Entry<String, Resource>> list =
        nestedHeatOrchestrationTemplate.getResources().entrySet()
            .stream()
            .filter(entry -> predicates
                .stream()
                    .allMatch(p -> p.test(entry)))
            .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(list)) {
      return Optional.empty();
    } else {
      return Optional.of(list);
    }
  }

  private List<Predicate<Map.Entry<String, Resource>>> buildPredicates(String fileName,
                                                                       HeatOrchestrationTemplate
                                                                       heatOrchestrationTemplate,
                                                                       String
                                                                       novaTranslatedResourceId) {
    List<Predicate<Map.Entry<String, Resource>>> list = new ArrayList<>();
    list.add(entry -> entry.getValue().getType().equals(getDesiredResourceType()));
    list.add(entry -> {
      Object instanceUuidProp = entry.getValue().getProperties().get("instance_uuid");
      TranslationContext context = translateTo.getContext();
      Optional<AttachedResourceId> instanceUuid = HeatToToscaUtil
          .extractAttachedResourceId(fileName, heatOrchestrationTemplate, context,
              instanceUuidProp);
      if (instanceUuid.isPresent()) {
        Optional<String> resourceTranslatedId = ResourceTranslationBase
            .getResourceTranslatedId(fileName, heatOrchestrationTemplate,
                (String) instanceUuid.get().getTranslatedId(), context);
        return resourceTranslatedId.isPresent()
            && resourceTranslatedId.get().equals(novaTranslatedResourceId);

      } else {
        throw new CoreException(new MissingMandatoryPropertyErrorBuilder("instance_uuid").build());
      }
    });
    return list;
  }

  @Override
  String getTranslatedResourceIdFromSubstitutionMapping(ServiceTemplate nestedServiceTemplate,
                                                        Map.Entry<String,
                                                        RequirementDefinition> entry) {
    List<String> substitutionMapping =
        nestedServiceTemplate.getTopology_template().getSubstitution_mappings().getRequirements()
            .get(entry.getKey());
    return substitutionMapping.get(0);
  }

  @Override
  void addRequirementToConnectResources(Map.Entry<String, RequirementDefinition> entry,
                                        List<String> paramNames) {
    String paramName = paramNames.get(0);
    Optional<AttachedResourceId> attachedResourceId =
        HeatToToscaUtil.extractAttachedResourceId(translateTo, paramName);
    String node;
    if (!attachedResourceId.isPresent()) {
      return;
    }
    AttachedResourceId attachedResource = attachedResourceId.get();
    if (attachedResource.isGetResource()) {
      String volTranslatedId = (String) attachedResource.getTranslatedId();
      Resource volServerResource = HeatToToscaUtil
          .getResource(translateTo.getHeatOrchestrationTemplate(), volTranslatedId,
              translateTo.getHeatFileName());
      if (!StringUtils.equals(HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource(),
          volServerResource.getType())) {
        logger.warn("Volume attachment used from nested resource " + translateTo.getResourceId()
            + " is pointing to incorrect resource type(" + volServerResource.getType()
            + ") for relation through the parameter '" + paramName + "."
            + " The connection to the volume is ignored. "
            + "Supported types are: "
            + HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource());
        return;
      }
      node = volTranslatedId;
      createRequirementAssignment(entry, node, substitutionNodeTemplate);
    } else if (attachedResource.isGetParam()) {
      TranslatedHeatResource shareResource =
          translateTo.getContext().getHeatSharedResourcesByParam()
              .get(attachedResource.getEntityId());
      if (Objects.nonNull(shareResource)
          && !HeatToToscaUtil.isHeatFileNested(translateTo, translateTo.getHeatFileName())) {
        if (!StringUtils.equals(HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource(),
            shareResource.getHeatResource().getType())) {
          logger.warn("Volume attachment used from nested resource " + translateTo.getResourceId()
              + " is pointing to incorrect resource type("
              + shareResource.getHeatResource().getType() + ") for relation through the parameter '"
              + paramName + "."
              + " The connection to the volume is ignored. "
              + "Supported types are: "
              + HeatResourcesTypes.CINDER_VOLUME_RESOURCE_TYPE.getHeatResource());
          return;
        }
        node = shareResource.getTranslatedId();
        createRequirementAssignment(entry, node, substitutionNodeTemplate);
      } else if (Objects.isNull(shareResource)) {
        List<FileData> allFilesData = translateTo.getContext().getManifest().getContent().getData();
        Optional<FileData> fileData =
            HeatToToscaUtil.getFileData(translateTo.getHeatFileName(), allFilesData);
        if (fileData.isPresent()) {
          Optional<ResourceFileDataAndIDs> fileDataContainingResource =
              new VolumeTranslationHelper(logger)
                  .getFileDataContainingVolume(fileData.get().getData(),
                      (String) attachedResource.getEntityId(), translateTo, FileData.Type.HEAT_VOL);
          if (fileDataContainingResource.isPresent()) {
            createRequirementAssignment(entry,
                fileDataContainingResource.get().getTranslatedResourceId(),
                substitutionNodeTemplate);
          }
        }
      }
    }
  }
}
