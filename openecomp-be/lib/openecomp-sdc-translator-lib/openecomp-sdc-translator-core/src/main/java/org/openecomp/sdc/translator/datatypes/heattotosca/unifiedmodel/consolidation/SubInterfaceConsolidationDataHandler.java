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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.collections4.MapUtils;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation.FunctionTranslator;

public class SubInterfaceConsolidationDataHandler implements ConsolidationDataHandler {

    private final PortConsolidationData portConsolidationData;

    SubInterfaceConsolidationDataHandler(PortConsolidationData portConsolidationData) {
        this.portConsolidationData = portConsolidationData;
    }

    @Override
    public void addNodesConnectedOut(TranslateTo translateTo, String nodeTemplateId, String requirementId,
                                            RequirementAssignment requirementAssignment) {
        ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
        if (Objects.nonNull(
                serviceTemplate.getTopology_template().getNode_templates().get(translateTo.getTranslatedId()))) {
            Optional<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationData =
                    getSubInterfaceTemplateConsolidationData(translateTo, translateTo.getTranslatedId());

            subInterfaceTemplateConsolidationData.ifPresent(consolidationData ->
                    consolidationData.addNodesConnectedOut(nodeTemplateId, requirementId, requirementAssignment));
        }
    }

    @Override
    public void addNodesConnectedIn(TranslateTo translateTo, String sourceNodeTemplateId,
                                           String dependentNodeTemplateId, String targetResourceId,
                                           String requirementId, RequirementAssignment requirementAssignment) {
        ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
        TranslationContext translationContext = translateTo.getContext();
        Resource targetResource = translateTo.getHeatOrchestrationTemplate().getResources().get(targetResourceId);
        TranslateTo subInterfaceTo = new TranslateTo(translateTo.getHeatFileName(), serviceTemplate,
                                                            translateTo.getHeatOrchestrationTemplate(), targetResource,
                                                            targetResourceId, null, translationContext);
        Optional<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationData =
                getSubInterfaceTemplateConsolidationData(subInterfaceTo, targetResourceId);

        subInterfaceTemplateConsolidationData.ifPresent(consolidationData ->
                consolidationData.addNodesConnectedIn(sourceNodeTemplateId, requirementId, requirementAssignment));

    }

    @Override
    public void removeParamNameFromAttrFuncList(ServiceTemplate serviceTemplate,
                                                       HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                       String paramName, String contrailSharedResourceId,
                                                       String sharedTranslatedResourceId) {


        throw new UnsupportedOperationException(
                "API removeParamNameFromAttrFuncList doesn't supported for SubInterfaceConsolidationDataHandler");
    }

    @Override
    public void addNodesGetAttrOut(FunctionTranslator functionTranslator, String nodeTemplateId,
            String resourceTranslatedId, String propertyName, String attributeName) {
        TranslateTo subInterfaceTo = createTranslateTo(functionTranslator, functionTranslator.getResourceId(),
                resourceTranslatedId);

        Optional<SubInterfaceTemplateConsolidationData> subInterfaceConsolidationData =
                getSubInterfaceTemplateConsolidationData(subInterfaceTo, resourceTranslatedId);

        subInterfaceConsolidationData.ifPresent(consolidationData -> {
            GetAttrFuncData getAttrFuncData = createGetAttrFuncData(propertyName, attributeName);
            consolidationData.addNodesGetAttrOut(nodeTemplateId, getAttrFuncData);
        });
    }

    @Override
    public void addNodesGetAttrIn(FunctionTranslator functionTranslator,String nodeTemplateId, String targetResourceId,
            String targetResourceTranslatedId,  String propertyName, String attributeName) {

        TranslateTo subInterfaceTo = createTranslateTo(functionTranslator, targetResourceId,
                targetResourceTranslatedId);

        Optional<SubInterfaceTemplateConsolidationData> subInterfaceConsolidationData =
                getSubInterfaceTemplateConsolidationData(subInterfaceTo, targetResourceTranslatedId);

        subInterfaceConsolidationData.ifPresent(consolidationData -> {
            GetAttrFuncData getAttrFuncData = createGetAttrFuncData(propertyName, attributeName);
            consolidationData.addNodesGetAttrIn(nodeTemplateId, getAttrFuncData);
        });
    }

    @Override
    public void addOutputParamGetAttrIn(FunctionTranslator functionTranslator, String targetResourceId,
            String targetResourceTranslatedId, String propertyName, String attributeName) {
        TranslateTo subInterfaceTo = createTranslateTo(functionTranslator, targetResourceId,
                targetResourceTranslatedId);

        Optional<SubInterfaceTemplateConsolidationData> subInterfaceConsolidationData =
                getSubInterfaceTemplateConsolidationData(subInterfaceTo, targetResourceTranslatedId);

        subInterfaceConsolidationData.ifPresent(consolidationData -> {
            GetAttrFuncData getAttrFuncData = createGetAttrFuncData(propertyName, attributeName);
            consolidationData.addOutputParamGetAttrIn(getAttrFuncData);
        });
    }

    public void setNetworkRole(TranslateTo translateTo, String translatedId, String networkRole) {
        Optional<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationData =
                getSubInterfaceTemplateConsolidationData(translateTo, translatedId);

        subInterfaceTemplateConsolidationData.ifPresent(
                consolidationData -> consolidationData.setNetworkRole(networkRole));
    }

    public void setResourceGroupCount(TranslateTo translateTo, String translatedId,
            Object resourceGroupCount) {
        Optional<SubInterfaceTemplateConsolidationData> subInterfaceTemplateConsolidationData =
                getSubInterfaceTemplateConsolidationData(translateTo, translatedId);

        subInterfaceTemplateConsolidationData.ifPresent(
                consolidationData -> consolidationData.setResourceGroupCount(resourceGroupCount));
    }

    private Optional<SubInterfaceTemplateConsolidationData> getSubInterfaceTemplateConsolidationData(
            TranslateTo subInterfaceTo, String subInterfaceNodeTemplateId) {
        Optional<String> parentPortNodeTemplateId =
                HeatToToscaUtil.getSubInterfaceParentPortNodeTemplateId(subInterfaceTo);
        return parentPortNodeTemplateId.map(s -> getSubInterfaceTemplateConsolidationData(subInterfaceTo,
                s, subInterfaceNodeTemplateId));
    }

    private SubInterfaceTemplateConsolidationData getSubInterfaceTemplateConsolidationData(TranslateTo subInterfaceTo,
            String parentPortNodeTemplateId, String subInterfaceNodeTemplateId) {
        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(subInterfaceTo.getServiceTemplate());
        Resource resource = subInterfaceTo.getResource();
        Optional<String> portResourceId = getPortResourceId(subInterfaceTo, parentPortNodeTemplateId);

        if (portResourceId.isPresent()) {
            String portResourceType = getPortResourceType(subInterfaceTo, portResourceId.get());
            return portConsolidationData
                    .addSubInterfaceTemplateConsolidationData(serviceTemplateFileName, resource,
                            subInterfaceNodeTemplateId, parentPortNodeTemplateId,
                                   portResourceId.get(), portResourceType);
        } else {
            return portConsolidationData
                    .addSubInterfaceTemplateConsolidationData(serviceTemplateFileName, resource,
                            subInterfaceNodeTemplateId, parentPortNodeTemplateId);
        }
    }

    private String getPortResourceType(TranslateTo subInterfaceTo, String portResourceId) {
        return HeatToToscaUtil.getResourceType(portResourceId, subInterfaceTo
        .getHeatOrchestrationTemplate(), subInterfaceTo.getHeatFileName());
    }

    private Optional<String> getPortResourceId(TranslateTo subInterfaceTo, String parentPortNodeTemplateId) {
        Map<String, String> resourceIdTranslatedResourceIdMap =
                subInterfaceTo.getContext().getTranslatedIds().get(subInterfaceTo.getHeatFileName());
        return getSubInterfaceParentPortResourceId(parentPortNodeTemplateId,
                resourceIdTranslatedResourceIdMap);
    }

    private Optional<String> getSubInterfaceParentPortResourceId(String parentPortNodeTemplateId,
            Map<String, String> resourceIdTranslatedResourceIdMap) {
        if (MapUtils.isEmpty(resourceIdTranslatedResourceIdMap)) {
            return Optional.empty();
        }
        return resourceIdTranslatedResourceIdMap.entrySet().stream()
               .filter(entry -> entry.getValue().equals(parentPortNodeTemplateId))
                        .findFirst().map(Map.Entry::getKey);
    }

    private TranslateTo createTranslateTo(FunctionTranslator functionTranslator, String resourceId,
            String resourceTranslatedId) {
        Resource resource = functionTranslator.getHeatOrchestrationTemplate().getResources().get(resourceId);
        return new TranslateTo(ToscaUtil.getServiceTemplateFileName(functionTranslator.getServiceTemplate()),
            functionTranslator.getServiceTemplate(), functionTranslator.getHeatOrchestrationTemplate(),
            resource, resourceId, resourceTranslatedId, functionTranslator.getContext());
    }

    private GetAttrFuncData createGetAttrFuncData(String propertyName, String attributeName) {
        GetAttrFuncData getAttrFuncData = new GetAttrFuncData();
        getAttrFuncData.setFieldName(propertyName);
        getAttrFuncData.setAttributeName(attributeName);
        return getAttrFuncData;
    }
}
