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
package org.openecomp.sdc.translator.datatypes.heattotosca.to;

import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;

public class ContrailServiceInstanceTo {

    private final ServiceTemplate nestedSubstitutionServiceTemplate;
    private final NodeTemplate substitutedNodeTemplate;
    private final String heatStackGroupKey;
    private final boolean orderedInterfaces;
    private final String computeNodeTemplateId;

    public ContrailServiceInstanceTo(ServiceTemplate nestedSubstitutionServiceTemplate, NodeTemplate substitutedNodeTemplate,
                                     String heatStackGroupKey, boolean orderedInterfaces, String computeNodeTemplateId) {
        this.nestedSubstitutionServiceTemplate = nestedSubstitutionServiceTemplate;
        this.substitutedNodeTemplate = substitutedNodeTemplate;
        this.heatStackGroupKey = heatStackGroupKey;
        this.orderedInterfaces = orderedInterfaces;
        this.computeNodeTemplateId = computeNodeTemplateId;
    }

    public ServiceTemplate getNestedSubstitutionServiceTemplate() {
        return nestedSubstitutionServiceTemplate;
    }

    public NodeTemplate getSubstitutedNodeTemplate() {
        return substitutedNodeTemplate;
    }

    public String getHeatStackGroupKey() {
        return heatStackGroupKey;
    }

    public boolean isOrderedInterfaces() {
        return orderedInterfaces;
    }

    public String getComputeNodeTemplateId() {
        return computeNodeTemplateId;
    }
}
