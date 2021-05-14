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

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.onap.sdc.tosca.services.DataModelCloneUtil;

@Getter
@Setter
public class CapabilityDefinition implements Cloneable {

    private String type;
    private String description;
    private Map<String, PropertyDefinition> properties;
    private Map<String, AttributeDefinition> attributes;
    private List<String> valid_source_types;
    private Object[] occurrences;

    public CapabilityDefinition() {
        occurrences = new Object[] {1, "UNBOUNDED"};
    }

    public CapabilityDefinition(final String type) {
        this.type = type;
    }

    @Override
    public CapabilityDefinition clone() {
        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setOccurrences(new Object[]{this.getOccurrences()[0], this.getOccurrences()[1]});
        capabilityDefinition.setProperties(DataModelCloneUtil.clonePropertyDefinitions(this.getProperties()));
        capabilityDefinition.setType(this.getType());
        capabilityDefinition.setAttributes(DataModelCloneUtil.cloneAttributeDefinitions(this.getAttributes()));
        capabilityDefinition.setDescription(this.getDescription());
        capabilityDefinition.setValid_source_types(DataModelCloneUtil.cloneListString(this.getValid_source_types()));
        return capabilityDefinition;
    }
}
