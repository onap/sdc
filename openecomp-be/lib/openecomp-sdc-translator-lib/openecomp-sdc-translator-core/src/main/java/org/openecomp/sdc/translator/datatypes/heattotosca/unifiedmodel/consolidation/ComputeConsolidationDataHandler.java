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

package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.NameExtractor;
import org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation.FunctionTranslator;

import java.util.Objects;

public class ComputeConsolidationDataHandler implements ConsolidationDataHandler {

    private final ComputeConsolidationData computeConsolidationData;

    ComputeConsolidationDataHandler(ComputeConsolidationData computeConsolidationData) {
        this.computeConsolidationData = computeConsolidationData;
    }

    @Override
    public void addNodesConnectedOut(TranslateTo translateTo, String nodeTemplateId, String requirementId,
                                     RequirementAssignment requirementAssignment) {

        String translatedSourceNodeId = translateTo.getTranslatedId();
        ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
        NodeTemplate computeNodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate, translatedSourceNodeId);
        String nodeType = computeNodeTemplate.getType();

        EntityConsolidationData entityConsolidationData =
                getComputeTemplateConsolidationData(translateTo, nodeType, translatedSourceNodeId);

        if (Objects.nonNull(entityConsolidationData)) {
            entityConsolidationData.addNodesConnectedOut(nodeTemplateId, requirementId, requirementAssignment);
        }
    }

    @Override
    public void addNodesConnectedIn(TranslateTo translateTo, String sourceNodeTemplateId,
                                    String dependentNodeTemplateId, String targetResourceId, String requirementId,
                                    RequirementAssignment requirementAssignment) {

        ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
        NodeTemplate nodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate, dependentNodeTemplateId);
        String nodeType = getNodeType(nodeTemplate, translateTo.getHeatOrchestrationTemplate(),
                targetResourceId, dependentNodeTemplateId, dependentNodeTemplateId);

        EntityConsolidationData entityConsolidationData =
                getComputeTemplateConsolidationData(translateTo, nodeType, dependentNodeTemplateId);

        if (Objects.nonNull(entityConsolidationData)) {
            entityConsolidationData.addNodesConnectedIn(sourceNodeTemplateId, requirementId, requirementAssignment);
        }
    }

    @Override
    public void removeParamNameFromAttrFuncList(ServiceTemplate serviceTemplate,
            HeatOrchestrationTemplate heatOrchestrationTemplate, String paramName, String contrailSharedResourceId,
            String sharedTranslatedResourceId) {

        NodeTemplate nodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate, sharedTranslatedResourceId);
        EntityConsolidationData entityConsolidationData =
                getComputeTemplateConsolidationData(ToscaUtil
                    .getServiceTemplateFileName(serviceTemplate), nodeTemplate.getType(), sharedTranslatedResourceId);

        if (Objects.nonNull(entityConsolidationData)) {
            entityConsolidationData.removeParamNameFromAttrFuncList(paramName);
        }
    }

    @Override
    public void addNodesGetAttrOut(FunctionTranslator functionTranslator, String nodeTemplateId,
            String resourceTranslatedId, String propertyName, String attributeName) {
        EntityConsolidationData entityConsolidationData =
                getComputeTemplateConsolidationData(functionTranslator,
                        functionTranslator.getResourceId(), resourceTranslatedId);

        if (Objects.nonNull(entityConsolidationData)) {
            GetAttrFuncData getAttrFuncData = createGetAttrFuncData(propertyName, attributeName);
            entityConsolidationData.addNodesGetAttrOut(nodeTemplateId, getAttrFuncData);
        }
    }

    @Override
    public void addNodesGetAttrIn(FunctionTranslator functionTranslator,String nodeTemplateId, String targetResourceId,
            String targetResourceTranslatedId,  String propertyName, String attributeName) {

        EntityConsolidationData entityConsolidationData =
                getComputeTemplateConsolidationData(functionTranslator, targetResourceId, targetResourceTranslatedId);

        if (Objects.nonNull(entityConsolidationData)) {
            GetAttrFuncData getAttrFuncData = createGetAttrFuncData(propertyName, attributeName);
            entityConsolidationData.addNodesGetAttrIn(nodeTemplateId, getAttrFuncData);
        }

    }

    @Override
    public void addOutputParamGetAttrIn(FunctionTranslator functionTranslator, String targetResourceId,
            String targetResourceTranslatedId, String propertyName, String attributeName) {

        EntityConsolidationData entityConsolidationData =
                getComputeTemplateConsolidationData(functionTranslator, targetResourceId, targetResourceTranslatedId);

        if (Objects.nonNull(entityConsolidationData)) {
            GetAttrFuncData getAttrFuncData = createGetAttrFuncData(propertyName, attributeName);
            entityConsolidationData.addOutputParamGetAttrIn(getAttrFuncData);
        }
    }

    /**
     * Add compute in consolidation data entity base on given keys.
     *
     */
    public void addConsolidationData(String serviceTemplateFileName,
            String computeNodeType, String computeNodeTemplateId) {
        getComputeTemplateConsolidationData(serviceTemplateFileName, computeNodeType,  computeNodeTemplateId);
    }

    /**
     * Add port to compute consolidation data entity base on given keys.
     *
     */
    public void addPortToConsolidationData(TranslateTo translateTo, String computeNodeType,
                String computeNodeTemplateId, String portType, String portNodeTemplateId) {
        ComputeTemplateConsolidationData consolidationData =
                getComputeTemplateConsolidationData(translateTo, computeNodeType, computeNodeTemplateId);
        consolidationData.addPort(portType, portNodeTemplateId);
    }

    /**
     * Add volume to consolidation data.
     *
     */
    public void addVolumeToConsolidationData(TranslateTo translateTo, String computeNodeType,
            String computeNodeTemplateId, String requirementId, RequirementAssignment requirementAssignment) {
        ComputeTemplateConsolidationData consolidationData =
                getComputeTemplateConsolidationData(translateTo, computeNodeType,
                        computeNodeTemplateId);
        consolidationData.addVolume(requirementId, requirementAssignment);
    }

    /**
     * Add group id information to consolidation data.
     *
     * @param translatedGroupId Group id of which compute node is a part
     */

    public void addGroupIdToConsolidationData(TranslateTo translateTo, String computeNodeType,
            String computeNodeTemplateId, String translatedGroupId) {
        ComputeTemplateConsolidationData consolidationData =
                getComputeTemplateConsolidationData(translateTo, computeNodeType,
                        computeNodeTemplateId);
        consolidationData.addGroupId(translatedGroupId);
    }

    public boolean isNumberOfComputeTypesLegal(String serviceTemplateName) {
        return computeConsolidationData.isNumberOfComputeTypesLegal(serviceTemplateName);
    }

    private EntityConsolidationData getComputeTemplateConsolidationData(FunctionTranslator functionTranslator,
                                                                      String resourceId, String computeNodeTemplateId) {
        HeatOrchestrationTemplate heatOrchestrationTemplate = functionTranslator.getHeatOrchestrationTemplate();
        TranslationContext context = functionTranslator.getContext();
        String heatFileName = functionTranslator.getHeatFileName();
        String translatedId = context.getTranslatedIds().get(heatFileName).get(resourceId);
        ServiceTemplate serviceTemplate = functionTranslator.getServiceTemplate();
        String computeType = getNodeType(heatOrchestrationTemplate, resourceId, resourceId, translatedId);
        return getComputeTemplateConsolidationData(
                ToscaUtil.getServiceTemplateFileName(serviceTemplate), computeType, computeNodeTemplateId);
    }

    private ComputeTemplateConsolidationData getComputeTemplateConsolidationData(
            TranslateTo translateTo, String computeNodeType, String computeNodeTemplateId) {
        ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
        return getComputeTemplateConsolidationData(serviceTemplateFileName, computeNodeType, computeNodeTemplateId);
    }

    private ComputeTemplateConsolidationData getComputeTemplateConsolidationData(
            String serviceTemplateFileName, String computeNodeType, String computeNodeTemplateId) {

        return computeConsolidationData.addComputeTemplateConsolidationData(serviceTemplateFileName, computeNodeType,
                computeNodeTemplateId);

    }

    private String getNodeType(HeatOrchestrationTemplate heatOrchestrationTemplate,
                                      String targetResourceId, String nodeTemplateId, String translatedId) {
        return getNodeType(null, heatOrchestrationTemplate, targetResourceId,
                nodeTemplateId, translatedId);
    }

    private String getNodeType(NodeTemplate computeNodeTemplate, HeatOrchestrationTemplate heatOrchestrationTemplate,
                                      String targetResourceId, String nodeTemplateId, String translatedId) {
        if (Objects.isNull(computeNodeTemplate)) {
            Resource targetResource = heatOrchestrationTemplate.getResources().get(targetResourceId);
            NameExtractor nodeTypeNameExtractor = TranslationContext.getNameExtractorImpl(targetResource.getType());
            return nodeTypeNameExtractor.extractNodeTypeName(heatOrchestrationTemplate.getResources()
                    .get(nodeTemplateId), nodeTemplateId, translatedId);
        }
        return computeNodeTemplate.getType();
    }

    private GetAttrFuncData createGetAttrFuncData(String propertyName, String attributeName) {
        GetAttrFuncData getAttrFuncData = new GetAttrFuncData();
        getAttrFuncData.setFieldName(propertyName);
        getAttrFuncData.setAttributeName(attributeName);
        return getAttrFuncData;
    }
}