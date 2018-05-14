package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;

public interface ConsolidationDataHandler {

    /**
     * Add nodeConnectedOut.
     *
     * @param translateTo           translate To
     * @param nodeTemplateId        the node template id which is connected from me
     * @param requirementId         the requirement id
     * @param requirementAssignment the requirement assignment
     */
    void addNodesConnectedOut(TranslateTo translateTo, String nodeTemplateId,
                                     String requirementId, RequirementAssignment requirementAssignment);

    /**
     * Add nodeConnectedIn.
     * @param translateTo           translate To
     * @param sourceNodeTemplateId  the node template id which has connection to me
     * @param dependentNodeTemplateId dependent Node Template id
     * @param targetResourceId      target resource id
     * @param requirementId         the requirement id
     * @param requirementAssignment the requirement assignment
     */
    void addNodesConnectedIn(TranslateTo translateTo,  String sourceNodeTemplateId,
                                    String dependentNodeTemplateId,
                                    String targetResourceId, String requirementId,
                                    RequirementAssignment requirementAssignment);

    void removeParamNameFromAttrFuncList(ServiceTemplate serviceTemplate,
                                                HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                String paramName, String contrailSharedResourceId,
                                                String sharedTranslatedResourceId);

}
