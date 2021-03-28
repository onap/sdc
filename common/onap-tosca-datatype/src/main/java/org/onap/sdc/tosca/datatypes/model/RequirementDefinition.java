/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */
package org.onap.sdc.tosca.datatypes.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequirementDefinition implements Cloneable {

    private String capability;
    private String node;
    private String relationship;
    private Object[] occurrences;

    /**
     * Instantiates a new Requirement definition.
     */
    public RequirementDefinition() {
        occurrences = new Object[2];
        occurrences[0] = 1;
        occurrences[1] = 1;
    }

    @Override
    public RequirementDefinition clone() {
        RequirementDefinition requirementDefinition = new RequirementDefinition();
        requirementDefinition.setNode(this.getNode());
        requirementDefinition.setRelationship(this.getRelationship());
        requirementDefinition.setCapability(this.getCapability());
        requirementDefinition.setOccurrences(new Object[]{this.getOccurrences()[0], this.getOccurrences()[1]});
        return requirementDefinition;
    }
}
