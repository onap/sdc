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

import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;

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
    public void addPortTemplateConsolidationData(String serviceTemplateFileName,
                String portResourceId, String portResourceType, String portNodeTemplateId) {
        getPortTemplateConsolidationData(
                serviceTemplateFileName, portResourceId, portResourceType, portNodeTemplateId);
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
