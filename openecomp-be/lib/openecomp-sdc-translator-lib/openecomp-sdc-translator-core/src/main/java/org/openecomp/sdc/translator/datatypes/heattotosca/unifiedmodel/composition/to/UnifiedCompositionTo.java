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
package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.to;

import java.util.List;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionData;

public class UnifiedCompositionTo {

    private ServiceTemplate serviceTemplate;
    private ServiceTemplate substitutionServiceTemplate;
    private List<UnifiedCompositionData> unifiedCompositionDataList;
    private TranslationContext context;
    private NodeTemplate nodeTemplate;

    public UnifiedCompositionTo(ServiceTemplate serviceTemplate, ServiceTemplate substitutionServiceTemplate,
                                List<UnifiedCompositionData> unifiedCompositionDataList, TranslationContext context, NodeTemplate nodeTemplate) {
        this.serviceTemplate = serviceTemplate;
        this.substitutionServiceTemplate = substitutionServiceTemplate;
        this.unifiedCompositionDataList = unifiedCompositionDataList;
        this.context = context;
        this.nodeTemplate = nodeTemplate;
    }

    public ServiceTemplate getServiceTemplate() {
        return serviceTemplate;
    }

    public void setServiceTemplate(ServiceTemplate serviceTemplate) {
        this.serviceTemplate = serviceTemplate;
    }

    public ServiceTemplate getSubstitutionServiceTemplate() {
        return substitutionServiceTemplate;
    }

    public void setSubstitutionServiceTemplate(ServiceTemplate substitutionServiceTemplate) {
        this.substitutionServiceTemplate = substitutionServiceTemplate;
    }

    public List<UnifiedCompositionData> getUnifiedCompositionDataList() {
        return unifiedCompositionDataList;
    }

    public void setUnifiedCompositionDataList(List<UnifiedCompositionData> unifiedCompositionDataList) {
        this.unifiedCompositionDataList = unifiedCompositionDataList;
    }

    public TranslationContext getContext() {
        return context;
    }

    public void setContext(TranslationContext context) {
        this.context = context;
    }

    public NodeTemplate getNodeTemplate() {
        return nodeTemplate;
    }

    public void setNodeTemplate(NodeTemplate nodeTemplate) {
        this.nodeTemplate = nodeTemplate;
    }
}
