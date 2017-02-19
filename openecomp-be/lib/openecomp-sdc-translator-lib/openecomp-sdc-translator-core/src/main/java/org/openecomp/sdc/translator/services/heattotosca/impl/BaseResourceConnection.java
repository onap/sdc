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

import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.utilities.yaml.YamlUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.datatypes.model.RequirementDefinition;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.errors.TranslatorErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

abstract class BaseResourceConnection<T> {
  protected static Logger logger =
      LoggerFactory.getLogger(ResourceTranslationCinderVolumeAttachmentImpl.class);
  protected TranslateTo translateTo;
  FileData nestedFileData;
  NodeTemplate substitutionNodeTemplate;
  NodeType nodeType;
  ResourceTranslationBase resourceTranslationBase;

  BaseResourceConnection(ResourceTranslationBase resourceTranslationBase, TranslateTo translateTo,
                         FileData nestedFileData, NodeTemplate substitutionNodeTemplate,
                         NodeType nodeType) {
    this.translateTo = translateTo;
    this.nestedFileData = nestedFileData;
    this.substitutionNodeTemplate = substitutionNodeTemplate;
    this.nodeType = nodeType;
    this.resourceTranslationBase = resourceTranslationBase;
  }

  abstract boolean isDesiredNodeTemplateType(NodeTemplate nodeTemplate);

  abstract List<Predicate<T>> getPredicatesListForConnectionPoints();

  abstract Optional<List<String>> getConnectorParamName(String heatResourceId,
                                                        Resource heatResource,
                                                        HeatOrchestrationTemplate
                                                            nestedHeatOrchestrationTemplate);

  abstract String getDesiredResourceType();

  abstract String getTranslatedResourceIdFromSubstitutionMapping(
      ServiceTemplate nestedServiceTemplate, Map.Entry<String, T> entry);

  abstract void addRequirementToConnectResources(Map.Entry<String, T> entry,
                                                 List<String> paramNames);

  abstract List<Map<String, T>> getAllConnectionPoints();

  void connect() {
    ServiceTemplate nestedServiceTemplate = translateTo.getContext().getTranslatedServiceTemplates()
        .get(translateTo.getResource().getType());
    List<String> paramNames = null;
    HeatOrchestrationTemplate nestedHeatOrchestrationTemplate = new YamlUtil()
        .yamlToObject(translateTo.getContext().getFileContent(nestedFileData.getFile()),
            HeatOrchestrationTemplate.class);
    List<Map<String, T>> exposedConnectionPoints = getAllConnectionPoints();
    for (Map<String, T> connectionPointsMap : exposedConnectionPoints) {
      for (Map.Entry<String, T> entry : connectionPointsMap.entrySet()) {
        String translatedResourceId =
            getTranslatedResourceIdFromSubstitutionMapping(nestedServiceTemplate, entry);
        NodeTemplate nodeTemplate = nestedServiceTemplate.getTopology_template().getNode_templates()
            .get(translatedResourceId);
        if (!isDesiredNodeTemplateType(nodeTemplate)) {
          continue;
        }
        paramNames = createResourcesConnection(translatedResourceId, paramNames,
            nestedHeatOrchestrationTemplate, entry);
      }
    }
  }

  private List<String> createResourcesConnection(String translatedResourceId,
                                                 List<String> paramNames,
                                                 HeatOrchestrationTemplate
                                                     nestedHeatOrchestrationTemplate,
                                                 Map.Entry<String, T> entry) {
    List<String> params = paramNames;
    Optional<List<Map.Entry<String, Resource>>> heatResources =
        getResourceByTranslatedResourceId(translatedResourceId, nestedHeatOrchestrationTemplate);
    if (heatResources.isPresent()) {
      params =
          addRequirementAndGetConnectorParamsFromResourceProperties(nestedHeatOrchestrationTemplate,
              entry, params, heatResources.get());
    }
    return params;
  }

  private List<String> addRequirementAndGetConnectorParamsFromResourceProperties(
      HeatOrchestrationTemplate nestedHeatOrchestrationTemplate, Map.Entry<String, T> entry,
      List<String> params, List<Map.Entry<String, Resource>> heatResources) {
    Resource heatResource;
    for (Map.Entry<String, Resource> resourceEntry : heatResources) {
      heatResource = resourceEntry.getValue();
      if (!MapUtils.isEmpty(heatResource.getProperties())) {
        Optional<List<String>> connectorParamName =
            getConnectorParamName(resourceEntry.getKey(), heatResource,
                nestedHeatOrchestrationTemplate);
        if (!connectorParamName.isPresent()) {
          break;
        } else {
          params = connectorParamName.get();
        }
      }
      Objects.requireNonNull(params);
      addRequirementToConnectResources(entry, params);
    }
    return params;
  }

  protected Optional<List<Map.Entry<String, Resource>>> getResourceByTranslatedResourceId(
      String translatedResourceId, HeatOrchestrationTemplate nestedHeatOrchestrationTemplate) {
    Optional<List<Map.Entry<String, Resource>>> resourceByTranslatedResourceId =
        resourceTranslationBase.getResourceByTranslatedResourceId(nestedFileData.getFile(),
            nestedHeatOrchestrationTemplate, translatedResourceId, translateTo,
            getDesiredResourceType());
    if (!resourceByTranslatedResourceId.isPresent()) {
      throw new CoreException((new ErrorCode.ErrorCodeBuilder()).withMessage(
          "Failed to get original resource from heat for translate resource id '"
             + translatedResourceId + "'")
          .withId(TranslatorErrorCodes.HEAT_TO_TOSCA_MAPPING_COLLISION)
          .withCategory(ErrorCategory.APPLICATION).build());
    }
    return resourceByTranslatedResourceId;
  }

  void createRequirementAssignment(Map.Entry<String, RequirementDefinition> entry, String node,
                                   NodeTemplate nodeTemplate) {
    if (Objects.nonNull(node)) {
      RequirementAssignment requirementAssignment;
      requirementAssignment = new RequirementAssignment();
      requirementAssignment.setRelationship(entry.getValue().getRelationship());
      requirementAssignment.setCapability(entry.getValue().getCapability());
      requirementAssignment.setNode(node);
      DataModelUtil.addRequirementAssignment(nodeTemplate, entry.getKey(), requirementAssignment);
    }
  }
}
