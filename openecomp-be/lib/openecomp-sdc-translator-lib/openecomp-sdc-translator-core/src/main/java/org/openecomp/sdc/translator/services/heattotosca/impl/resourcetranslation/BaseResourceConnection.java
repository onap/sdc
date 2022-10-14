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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.RequirementDefinition;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslatedHeatResource;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.errors.TranslatorErrorCodes;

abstract class BaseResourceConnection<T> {

    protected static Logger logger = LoggerFactory.getLogger(BaseResourceConnection.class);
    protected TranslateTo translateTo;
    FileData nestedFileData;
    NodeTemplate substitutionNodeTemplate;
    NodeType nodeType;
    ResourceTranslationBase resourceTranslationBase;

    BaseResourceConnection(ResourceTranslationBase resourceTranslationBase, TranslateTo translateTo, FileData nestedFileData,
                           NodeTemplate substitutionNodeTemplate, NodeType nodeType) {
        this.translateTo = translateTo;
        this.nestedFileData = nestedFileData;
        this.substitutionNodeTemplate = substitutionNodeTemplate;
        this.nodeType = nodeType;
        this.resourceTranslationBase = resourceTranslationBase;
    }

    abstract boolean isDesiredNodeTemplateType(NodeTemplate nodeTemplate);

    abstract List<Predicate<T>> getPredicatesListForConnectionPoints();

    abstract Optional<List<String>> getConnectorPropertyParamName(String heatResourceId, Resource heatResource,
                                                                  HeatOrchestrationTemplate nestedHeatOrchestrationTemplate,
                                                                  String nestedHeatFileName);

    abstract String getDesiredResourceType();

    abstract String getMappedNodeTranslatedResourceId(ServiceTemplate nestedServiceTemplate, Map.Entry<String, T> connectionPointEntry);

    abstract Map.Entry<String, T> getMappedConnectionPointEntry(ServiceTemplate nestedServiceTemplate, Map.Entry<String, T> connectionPointEntry);

    abstract void addRequirementToConnectResources(Map.Entry<String, T> connectionPointEntry, List<String> paramNames);

    abstract List<Map<String, T>> getAllConnectionPoints();

    abstract boolean validateResourceTypeSupportedForReqCreation(String nestedResourceId, final String nestedPropertyName, String connectionPointId,
                                                                 Resource connectedResource, List<String> supportedTypes);

    void connect() {
        ServiceTemplate nestedServiceTemplate = translateTo.getContext().getTranslatedServiceTemplates().get(translateTo.getResource().getType());
        List<String> paramNames;
        HeatOrchestrationTemplate nestedHeatOrchestrationTemplate = new YamlUtil()
            .yamlToObject(translateTo.getContext().getFileContentAsStream(nestedFileData.getFile()), HeatOrchestrationTemplate.class);
        List<Map<String, T>> exposedConnectionPoints = getAllConnectionPoints();
        for (Map<String, T> connectionPointsMap : exposedConnectionPoints) {
            for (Map.Entry<String, T> connectionPointEntry : connectionPointsMap.entrySet()) {
                paramNames = getConnectionParameterName(nestedServiceTemplate, nestedHeatOrchestrationTemplate, nestedFileData.getFile(),
                    connectionPointEntry);
                if (CollectionUtils.isNotEmpty(paramNames)) {
                    addRequirementToConnectResources(connectionPointEntry, paramNames);
                }
            }
        }
    }

    private List<String> getConnectionParameterName(ServiceTemplate nestedServiceTemplate, HeatOrchestrationTemplate nestedHeatOrchestrationTemplate,
                                                    String nestedHeatFileName, Map.Entry<String, T> connectionPointEntry) {
        List<String> connectionParameterNameList = new ArrayList<>();
        String mappedTranslatedResourceId = getMappedNodeTranslatedResourceId(nestedServiceTemplate, connectionPointEntry);
        NodeTemplate mappedNodeTemplate = nestedServiceTemplate.getTopology_template().getNode_templates().get(mappedTranslatedResourceId);
        if (isDesiredNodeTemplateType(mappedNodeTemplate)) {
            return getResourcesConnectionParameterName(mappedTranslatedResourceId, nestedHeatOrchestrationTemplate, nestedHeatFileName);
        }
        ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
        if (!toscaAnalyzerService.isSubstitutableNodeTemplate(mappedNodeTemplate)) {
            return Collections.emptyList();
        }
        Optional<String> mappedSubstituteServiceTemplateName = toscaAnalyzerService
            .getSubstituteServiceTemplateName(mappedTranslatedResourceId, mappedNodeTemplate);
        if (!mappedSubstituteServiceTemplateName.isPresent()) {
            return Collections.emptyList();
        }
        String mappedNestedHeatFileName = translateTo.getContext().getNestedHeatFileName().get(mappedSubstituteServiceTemplateName.get());
        if (Objects.isNull(mappedNestedHeatFileName)) {
            return Collections.emptyList();
        }
        HeatOrchestrationTemplate mappedNestedHeatOrchestrationTemplate = new YamlUtil()
            .yamlToObject(translateTo.getContext().getFileContentAsStream(mappedNestedHeatFileName), HeatOrchestrationTemplate.class);
        ServiceTemplate mappedNestedServiceTemplate = translateTo.getContext().getTranslatedServiceTemplates().get(mappedNestedHeatFileName);
        List<String> nestedPropertyNames = getConnectionParameterName(mappedNestedServiceTemplate, mappedNestedHeatOrchestrationTemplate,
            mappedNestedHeatFileName, getMappedConnectionPointEntry(nestedServiceTemplate, connectionPointEntry));
        if (CollectionUtils.isEmpty(nestedPropertyNames)) {
            return connectionParameterNameList;
        }
        for (String propertyName : nestedPropertyNames) {
            Object propertyValue = mappedNodeTemplate.getProperties().get(propertyName);
            if (propertyValue instanceof Map && ((Map) propertyValue).containsKey(ToscaFunctions.GET_INPUT.getFunctionName())) {
                Object paramName = ((Map) propertyValue).get(ToscaFunctions.GET_INPUT.getFunctionName());
                if (paramName instanceof String) {
                    connectionParameterNameList.add((String) paramName);
                }
            }
        }
        return connectionParameterNameList;
    }

    private List<String> getResourcesConnectionParameterName(String translatedResourceId, HeatOrchestrationTemplate nestedHeatOrchestrationTemplate,
                                                             String nestedHeatFileName) {
        List<String> params = new ArrayList<>();
        Optional<List<Map.Entry<String, Resource>>> heatResources = getResourceByTranslatedResourceId(translatedResourceId,
            nestedHeatOrchestrationTemplate);
        if (!heatResources.isPresent()) {
            return params;
        }
        for (Map.Entry<String, Resource> resourceEntry : heatResources.get()) {
            Resource heatResource = resourceEntry.getValue();
            if (!MapUtils.isEmpty(heatResource.getProperties())) {
                Optional<List<String>> connectorParamName = getConnectorPropertyParamName(resourceEntry.getKey(), heatResource,
                    nestedHeatOrchestrationTemplate, nestedHeatFileName);
                connectorParamName.ifPresent(params::addAll);
            }
        }
        return params;
    }

    protected Optional<List<Map.Entry<String, Resource>>> getResourceByTranslatedResourceId(String translatedResourceId,
                                                                                            HeatOrchestrationTemplate nestedHeatOrchestrationTemplate) {
        Optional<List<Map.Entry<String, Resource>>> resourceByTranslatedResourceId = resourceTranslationBase
            .getResourceByTranslatedResourceId(nestedFileData.getFile(), nestedHeatOrchestrationTemplate, translatedResourceId, translateTo,
                getDesiredResourceType());
        if (!resourceByTranslatedResourceId.isPresent()) {
            throw new CoreException((new ErrorCode.ErrorCodeBuilder())
                .withMessage("Failed to get original resource from heat for translate resource id '" + translatedResourceId + "'")
                .withId(TranslatorErrorCodes.HEAT_TO_TOSCA_MAPPING_COLLISION).withCategory(ErrorCategory.APPLICATION).build());
        }
        return resourceByTranslatedResourceId;
    }

    RequirementAssignment createRequirementAssignment(Map.Entry<String, RequirementDefinition> requirementEntry, String node,
                                                      NodeTemplate nodeTemplate) {
        RequirementAssignment requirementAssignment = null;
        if (Objects.nonNull(node)) {
            requirementAssignment = new RequirementAssignment();
            requirementAssignment.setRelationship(requirementEntry.getValue().getRelationship());
            requirementAssignment.setCapability(requirementEntry.getValue().getCapability());
            requirementAssignment.setNode(node);
            DataModelUtil.addRequirementAssignment(nodeTemplate, requirementEntry.getKey(), requirementAssignment);
        }
        return requirementAssignment;
    }

    Optional<String> getConnectionTranslatedNodeUsingGetParamFunc(Map.Entry<String, T> connectionPointEntry, String paramName,
                                                                  List<String> supportedNodeTypes) {
        Optional<AttachedResourceId> attachedResourceId = HeatToToscaUtil.extractAttachedResourceId(translateTo, paramName);
        if (!attachedResourceId.isPresent()) {
            return Optional.empty();
        }
        AttachedResourceId resourceId = attachedResourceId.get();
        if (resourceId.isGetParam() && resourceId.getEntityId() instanceof String) {
            TranslatedHeatResource shareResource = translateTo.getContext().getHeatSharedResourcesByParam().get(resourceId.getEntityId());
            if (isSupportedSharedResource(paramName, connectionPointEntry.getKey(), supportedNodeTypes, shareResource)) {
                return Optional.of(shareResource.getTranslatedId());
            }
        }
        return Optional.empty();
    }

    private boolean isSupportedSharedResource(String paramName, String connectionPointId, List<String> supportedNodeTypes,
                                              TranslatedHeatResource shareResource) {
        return Objects.nonNull(shareResource) && !HeatToToscaUtil.isHeatFileNested(translateTo, translateTo.getHeatFileName())
            && validateResourceTypeSupportedForReqCreation(translateTo.getResourceId(), paramName, connectionPointId, shareResource.getHeatResource(),
            supportedNodeTypes);
    }

    Optional<TranslatedHeatResource> getConnectionTranslatedHeatResourceUsingGetParamFunc(Map.Entry<String, T> connectionPointEntry, String paramName,
                                                                                          List<String> supportedNodeTypes) {
        Optional<AttachedResourceId> attachedResourceId = HeatToToscaUtil.extractAttachedResourceId(translateTo, paramName);
        if (!attachedResourceId.isPresent()) {
            return Optional.empty();
        }
        AttachedResourceId resourceId = attachedResourceId.get();
        if (resourceId.isGetParam() && resourceId.getEntityId() instanceof String) {
            TranslatedHeatResource shareResource = translateTo.getContext().getHeatSharedResourcesByParam().get(resourceId.getEntityId());
            if (isSupportedSharedResource(paramName, connectionPointEntry.getKey(), supportedNodeTypes, shareResource)) {
                return Optional.of(shareResource);
            }
        }
        return Optional.empty();
    }

    Optional<String> getConnectionTranslatedNodeUsingGetResourceFunc(Map.Entry<String, T> connectionPointEntry, String paramName, Object paramValue,
                                                                     List<String> supportedNodeTypes) {
        Optional<String> getResourceAttachedResourceId = HeatToToscaUtil.extractContrailGetResourceAttachedHeatResourceId(paramValue);
        if (getResourceAttachedResourceId.isPresent()) { // get resource
            Resource resource = translateTo.getHeatOrchestrationTemplate().getResources().get(getResourceAttachedResourceId.get());
            if (validateResourceTypeSupportedForReqCreation(translateTo.getResourceId(), paramName, connectionPointEntry.getKey(), resource,
                supportedNodeTypes)) {
                return ResourceTranslationBase.getResourceTranslatedId(translateTo.getHeatFileName(), translateTo.getHeatOrchestrationTemplate(),
                    getResourceAttachedResourceId.get(), translateTo.getContext());
            }
        }
        return Optional.empty();
    }

    Optional<String> getConnectionResourceUsingGetResourceFunc(Map.Entry<String, T> connectionPointEntry, String paramName, Object paramValue,
                                                               List<String> supportedNodeTypes) {
        Optional<String> getResourceAttachedResourceId = HeatToToscaUtil.extractContrailGetResourceAttachedHeatResourceId(paramValue);
        if (getResourceAttachedResourceId.isPresent()) { // get resource
            Resource resource = translateTo.getHeatOrchestrationTemplate().getResources().get(getResourceAttachedResourceId.get());
            if (validateResourceTypeSupportedForReqCreation(translateTo.getResourceId(), paramName, connectionPointEntry.getKey(), resource,
                supportedNodeTypes)) {
                return getResourceAttachedResourceId;
            }
        }
        return Optional.empty();
    }
}
