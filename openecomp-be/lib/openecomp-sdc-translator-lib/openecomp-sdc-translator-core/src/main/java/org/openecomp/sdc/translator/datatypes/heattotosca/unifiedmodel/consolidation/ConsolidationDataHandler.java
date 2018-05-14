package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;

public interface ConsolidationDataHandler {

    void addNodesConnectedOut(TranslateTo translateTo, String nodeTemplateId,
                                     String requirementId, RequirementAssignment requirementAssignment);

    void addNodesConnectedIn(TranslateTo translateTo,  String sourceNodeTemplateId,
                                    String dependentNodeTemplateId,
                                    String targetResourceId, String requirementId,
                                    RequirementAssignment requirementAssignment);

    void removeParamNameFromAttrFuncList(ServiceTemplate serviceTemplate,
                                                HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                String paramName, String contrailSharedResourceId,
                                                String sharedTranslatedResourceId);

}
