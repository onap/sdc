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
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation.FunctionTranslator;

public interface ConsolidationDataHandler {

    /**
     * Add node connected out from current consolidation entity.
     *
     * @param nodeTemplateId        the node template id of target node - node connected out from current node
     * @param requirementId         the requirement id of requirement assignment
     * @param requirementAssignment the requirement assignment data connected to target node
     */
    void addNodesConnectedOut(TranslateTo translateTo, String nodeTemplateId,
                                     String requirementId, RequirementAssignment requirementAssignment);

    /**
     * Add source node connected in to target node consolidation entity.
     *
     * @param sourceNodeTemplateId  the node template id of source node connected to consolidation entity
     * @param targetNodeTemplateId  the node template id of consolidation entity node
     * @param targetResourceId      the resource id of consolidation entity node
     * @param requirementId         the requirement id of source node
     * @param requirementAssignment the requirement assignment data of source node
     */
    void addNodesConnectedIn(TranslateTo translateTo,  String sourceNodeTemplateId,
                                    String targetNodeTemplateId,
                                    String targetResourceId, String requirementId,
                                    RequirementAssignment requirementAssignment);

    void removeParamNameFromAttrFuncList(ServiceTemplate serviceTemplate,
                                                HeatOrchestrationTemplate heatOrchestrationTemplate,
                                                String paramName, String contrailSharedResourceId,
                                                String sharedTranslatedResourceId);

    void addNodesGetAttrOut(FunctionTranslator functionTranslator, String nodeTemplateId,
            String resourceTranslatedId, String propertyName, String attributeName);

    void addNodesGetAttrIn(FunctionTranslator functionTranslator,String nodeTemplateId,
            String targetResourceId, String targetResourceTranslatedId,  String propertyName, String attributeName);

    void addOutputParamGetAttrIn(FunctionTranslator functionTranslator, String targetResourceId,
            String targetResourceTranslatedId, String propertyName, String attributeName);
}
