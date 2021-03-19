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

import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.be.datatypes.elements.CapabilityTypeDataDefinition;
import org.openecomp.sdc.be.resources.data.CapabilityTypeData;

/**
 * Specifies the capabilities that the Node Type exposes.
 */
@SuppressWarnings("serial")
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class CapabilityTypeDefinition extends CapabilityTypeDataDefinition {

    private String derivedFrom;
    private Map<String, PropertyDefinition> properties;

    public CapabilityTypeDefinition(CapabilityTypeDataDefinition p) {
        super(p);
    }

    public CapabilityTypeDefinition(CapabilityTypeData ctd) {
        this.setUniqueId(ctd.getUniqueId());
        this.setType(ctd.getCapabilityTypeDataDefinition().getType());
        this.setDescription(ctd.getCapabilityTypeDataDefinition().getDescription());
        this.setValidSourceTypes(ctd.getCapabilityTypeDataDefinition().getValidSourceTypes());
    }
}
