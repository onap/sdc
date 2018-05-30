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
import java.util.Optional;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;

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

            subInterfaceTemplateConsolidationData.ifPresent(
                    consolidationData -> consolidationData.addNodesConnectedOut(nodeTemplateId,
                            requirementId, requirementAssignment));

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

        subInterfaceTemplateConsolidationData.ifPresent(
                consolidationData -> consolidationData.addNodesConnectedIn(sourceNodeTemplateId,
                        requirementId, requirementAssignment));

    }

    @Override
    public void removeParamNameFromAttrFuncList(ServiceTemplate serviceTemplate,
                                                       HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                       String paramName,
                                                       String contrailSharedResourceId,
                                                       String sharedTranslatedResourceId) {


        throw new UnsupportedOperationException("API removeParamNameFromAttrFuncList "
                  + "not supported for SubInterfaceConsolidationDataHandler");


    }

    private Optional<SubInterfaceTemplateConsolidationData> getSubInterfaceTemplateConsolidationData(
            TranslateTo subInterfaceTo, String subInterfaceNodeTemplateId) {
        Optional<String> parentPortNodeTemplateId =
                HeatToToscaUtil.getSubInterfaceParentPortNodeTemplateId(subInterfaceTo);
        return parentPortNodeTemplateId.map(s -> getSubInterfaceTemplateConsolidationData(subInterfaceTo,
                s, subInterfaceNodeTemplateId));
    }

    private SubInterfaceTemplateConsolidationData getSubInterfaceTemplateConsolidationData(
            TranslateTo subInterfaceTo, String parentPortNodeTemplateId,String subInterfaceNodeTemplateId) {
        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(subInterfaceTo.getServiceTemplate());
        Resource resource = subInterfaceTo.getResource();
        return portConsolidationData.addSubInterfaceTemplateConsolidationData(
                    serviceTemplateFileName, resource, subInterfaceNodeTemplateId, parentPortNodeTemplateId);
    }

}
