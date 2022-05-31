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
package org.openecomp.sdc.be.model;

import java.util.List;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyRule;

public class ComponentInstanceProperty extends PropertyDefinition implements IComponentInstanceConnectedElement, IPropertyInputCommon {

    /**
     * The unique id of the property value on graph
     */
    private String valueUniqueUid;
    private List<String> path;
    private List<PropertyRule> rules;
    private String componentInstanceName;
    private String componentInstanceId;

    public ComponentInstanceProperty() {
        super();
    }

    public ComponentInstanceProperty(PropertyDataDefinition pd) {
        super(pd);
        getConstraints();
    }

    public ComponentInstanceProperty(PropertyDefinition pd) {
        super(pd);
    }

    public ComponentInstanceProperty(PropertyDefinition pd, String value, String valueUniqueUid) {
        super(pd);
        this.setValue(value);
        this.valueUniqueUid = valueUniqueUid;
    }

    public ComponentInstanceProperty(Boolean hidden, PropertyDefinition pd, String valueUniqueUid) {
        super(pd);
        this.setHidden(hidden);
        this.valueUniqueUid = valueUniqueUid;
        setParentUniqueId(pd.getParentUniqueId());
    }

    public String getComponentInstanceName() {
        return componentInstanceName;
    }

    public void setComponentInstanceName(String componentInstanceName) {
        this.componentInstanceName = componentInstanceName;
    }

    public String getComponentInstanceId() {
        return componentInstanceId;
    }

    public void setComponentInstanceId(String componentInstanceId) {
        this.componentInstanceId = componentInstanceId;
    }

    public String getValueUniqueUid() {
        return valueUniqueUid;
    }

    public void setValueUniqueUid(String valueUniqueUid) {
        this.valueUniqueUid = valueUniqueUid;
    }

    public List<String> getPath() {
        return path;
    }

    public void setPath(List<String> path) {
        this.path = path;
    }

    public List<PropertyRule> getRules() {
        return rules;
    }

    public void setRules(List<PropertyRule> rules) {
        this.rules = rules;
    }

    @Override
    public String toString() {
        return "ComponentInstanceProperty [ " + super.toString() + " , value=" + getValue() + ", valueUniqueUid = " + valueUniqueUid + " , rules="
            + rules + " , path=" + path + " ]";
    }

    public void updateCapabilityProperty(ComponentInstanceProperty property) {
        if (property != null && property.getValue() != null) {
            setValue(property.getValue());
        }
    }
}
