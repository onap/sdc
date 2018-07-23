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

import java.util.Objects;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation.FunctionTranslator;

public class PortConsolidationDataHandler implements ConsolidationDataHandler {

    private final PortConsolidationData portConsolidationData;

    public PortConsolidationDataHandler(PortConsolidationData portConsolidationData) {
        this.portConsolidationData = portConsolidationData;
    }

    @Override
    public void addNodesConnectedOut(TranslateTo translateTo, String nodeTemplateId, String requirementId,
                                            RequirementAssignment requirementAssignment) {

        EntityConsolidationData entityConsolidationData =
                getPortTemplateConsolidationData(translateTo, translateTo.getResourceId(),
                        translateTo.getResource().getType(), translateTo.getTranslatedId());

        entityConsolidationData.addNodesConnectedOut(nodeTemplateId, requirementId, requirementAssignment);

    }

    @Override
    public void addNodesConnectedIn(TranslateTo translateTo, String sourceNodeTemplateId,
               String dependentNodeTemplateId, String targetResourceId, String requirementId,
               RequirementAssignment requirementAssignment) {

        EntityConsolidationData entityConsolidationData =
                getPortTemplateConsolidationData(translateTo, translateTo.getResourceId(),
                        translateTo.getResource().getType(), dependentNodeTemplateId);

        entityConsolidationData.addNodesConnectedIn(sourceNodeTemplateId, requirementId, requirementAssignment);

    }

    @Override
    public void removeParamNameFromAttrFuncList(ServiceTemplate serviceTemplate,
                HeatOrchestrationTemplate heatOrchestrationTemplate, String paramName,
                String contrailSharedResourceId, String sharedTranslatedResourceId) {

        Resource resource = heatOrchestrationTemplate.getResources().get(contrailSharedResourceId);
        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
        EntityConsolidationData entityConsolidationData = getPortTemplateConsolidationData(serviceTemplateFileName,
                contrailSharedResourceId, resource.getType(), sharedTranslatedResourceId);
        entityConsolidationData.removeParamNameFromAttrFuncList(paramName);

    }

    /**
     * Add port in consolidation data base on given parameters.
     *
     */
    public void addConsolidationData(String serviceTemplateFileName,
                String portResourceId, String portResourceType, String portNodeTemplateId) {
        getPortTemplateConsolidationData(
                serviceTemplateFileName, portResourceId, portResourceType, portNodeTemplateId);
    }

    @Override
    public void addNodesGetAttrOut(FunctionTranslator functionTranslator, String nodeTemplateId,
            String resourceTranslatedId, String propertyName, String attributeName) {

        String resourceId = functionTranslator.getResourceId();
        EntityConsolidationData entityConsolidationData =
                getPortTemplateConsolidationData(functionTranslator, resourceId, resourceTranslatedId);

        if (Objects.nonNull(entityConsolidationData)) {
            GetAttrFuncData getAttrFuncData = createGetAttrFuncData(propertyName, attributeName);
            entityConsolidationData.addNodesGetAttrOut(nodeTemplateId, getAttrFuncData);
        }
    }

    @Override
    public void addNodesGetAttrIn(FunctionTranslator functionTranslator,String nodeTemplateId,
            String targetResourceId, String targetResourceTranslatedId,  String propertyName, String attributeName) {
        EntityConsolidationData entityConsolidationData =
                getPortTemplateConsolidationData(functionTranslator, targetResourceId, targetResourceTranslatedId);

        if (Objects.nonNull(entityConsolidationData)) {
            GetAttrFuncData getAttrFuncData = createGetAttrFuncData(propertyName, attributeName);
            entityConsolidationData.addNodesGetAttrIn(nodeTemplateId, getAttrFuncData);
        }
    }

    @Override
    public void addOutputParamGetAttrIn(FunctionTranslator functionTranslator, String targetResourceId,
            String targetResourceTranslatedId, String propertyName, String attributeName) {

        EntityConsolidationData entityConsolidationData =
                getPortTemplateConsolidationData(functionTranslator, targetResourceId, targetResourceTranslatedId);

        if (Objects.nonNull(entityConsolidationData)) {
            GetAttrFuncData getAttrFuncData = createGetAttrFuncData(propertyName, attributeName);
            entityConsolidationData.addOutputParamGetAttrIn(getAttrFuncData);
        }
    }

    private GetAttrFuncData createGetAttrFuncData(String propertyName, String attributeName) {
        GetAttrFuncData getAttrFuncData = new GetAttrFuncData();
        getAttrFuncData.setFieldName(propertyName);
        getAttrFuncData.setAttributeName(attributeName);
        return getAttrFuncData;
    }

    private EntityConsolidationData getPortTemplateConsolidationData(FunctionTranslator functionTranslator,
            String targetResourceId, String targetResourceTranslatedId) {
        HeatOrchestrationTemplate heatOrchestrationTemplate = functionTranslator.getHeatOrchestrationTemplate();
        Resource resource = heatOrchestrationTemplate.getResources().get(targetResourceId);
        ServiceTemplate serviceTemplate = functionTranslator.getServiceTemplate();
        return getPortTemplateConsolidationData(ToscaUtil.getServiceTemplateFileName(serviceTemplate),
                targetResourceId, resource.getType(), targetResourceTranslatedId);
    }

    private PortTemplateConsolidationData getPortTemplateConsolidationData(TranslateTo translateTo,
            String portResourceId, String portResourceType, String portNodeTemplateId) {
        ServiceTemplate serviceTemplate = translateTo.getServiceTemplate();
        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
        return getPortTemplateConsolidationData(serviceTemplateFileName,
                portResourceId, portResourceType, portNodeTemplateId);
    }

    private PortTemplateConsolidationData getPortTemplateConsolidationData(String serviceTemplateFileName,
            String portResourceId, String portResourceType, String portNodeTemplateId) {

        return portConsolidationData.addPortTemplateConsolidationData(serviceTemplateFileName,
                        portNodeTemplateId, portResourceId, portResourceType);

    }
}
