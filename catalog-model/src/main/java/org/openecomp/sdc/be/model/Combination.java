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

/*
 *
 * Copyright (C) 2018 Huawei Intellectual Property. All rights reserved.
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
 *
 *
 */

package org.openecomp.sdc.be.model;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;
import org.openecomp.sdc.be.ui.model.UiCombination;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Combination {

    private String uniqueId;
    private String name;
    private String description;
    private List<ComponentInstance> componentInstances;
    private List<RequirementCapabilityRelDef> componentInstancesRelations;
    private Map<String, List<ComponentInstanceInput>> componentInstancesInputs;
    private Map<String, List<ComponentInstanceProperty>> componentInstancesProperties;
    private Map<String, List<ComponentInstanceProperty>> componentInstancesAttributes;

    public Combination() {
    }
    // To form the combination object with the data received from UI
    public Combination(UiCombination UICombination) {
        name = UICombination.getName();
        description = UICombination.getDescription();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return description;
    }

    public void setDesc(String description) {
        this.description = description;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public List<ComponentInstance> getComponentInstances() {
        return componentInstances;
    }

    public void setComponentInstances(List<ComponentInstance> componentInstances) {
        this.componentInstances = componentInstances;
    }

    public List<RequirementCapabilityRelDef> getComponentInstancesRelations() {
        return componentInstancesRelations;
    }

    public void setComponentInstancesRelations(List<RequirementCapabilityRelDef> componentInstancesRelations) {
        this.componentInstancesRelations = componentInstancesRelations;
    }

    public Map<String, List<ComponentInstanceInput>> getComponentInstancesInputs() {
        return componentInstancesInputs;
    }

    public void setComponentInstancesInputs(Map<String, List<ComponentInstanceInput>> componentInstancesInputs) {
        this.componentInstancesInputs = componentInstancesInputs;
    }

    public Map<String, List<ComponentInstanceProperty>> getComponentInstancesProperties() {
        return componentInstancesProperties;
    }

    public void setComponentInstancesProperties(Map<String, List<ComponentInstanceProperty>> componentInstancesProperties) {
        this.componentInstancesProperties = componentInstancesProperties;
    }

    public Map<String, List<ComponentInstanceProperty>> getComponentInstancesAttributes() {
        return componentInstancesAttributes;
    }

    public void setComponentInstancesAttributes(Map<String, List<ComponentInstanceProperty>> componentInstancesAttributes) {
        this.componentInstancesAttributes = componentInstancesAttributes;
    }
}
