/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import java.util.List;
import java.util.Map;

public class WorkflowDefinition {

    private String description;
    private Map<String, String> metadata;
    private Map<String, PropertyDefinition> inputs;
    private List<PreconditionDefinition> preconditions;
    private Map<String, StepDefinition> steps;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public Map<String, PropertyDefinition> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, PropertyDefinition> inputs) {
        this.inputs = inputs;
    }

    public List<PreconditionDefinition> getPreconditions() {
        return preconditions;
    }

    public void setPreconditions(List<PreconditionDefinition> preconditions) {
        this.preconditions = preconditions;
    }

    public Map<String, StepDefinition> getSteps() {
        return steps;
    }

    public void setSteps(Map<String, StepDefinition> steps) {
        this.steps = steps;
    }
}
