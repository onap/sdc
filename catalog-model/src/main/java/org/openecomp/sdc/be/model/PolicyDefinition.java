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
package org.openecomp.sdc.be.model;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.openecomp.sdc.be.datatypes.elements.PolicyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertiesOwner;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

/**
 * public class representing the component policy
 */
public class PolicyDefinition extends PolicyDataDefinition implements PropertiesOwner {

    /**
     * public constructor by default
     */
    public PolicyDefinition() {
        super();
    }

    public PolicyDefinition(PropertyDataDefinition propertyDataDefinition) {
        super(propertyDataDefinition);
    }

    /**
     * public constructor from superclass
     *
     * @param policy
     */
    public PolicyDefinition(Map<String, Object> policy) {
        super(policy);
    }

    /**
     * public copy constructor
     *
     * @param other
     */
    public PolicyDefinition(PolicyDataDefinition other) {
        super(other);
    }

    /**
     * public converter constructor builds PolicyDefinition object based on received PolicyTypeDefinition object
     *
     * @param policyType
     */
    public PolicyDefinition(PolicyTypeDefinition policyType) {
        this.setPolicyTypeName(policyType.getType());
        this.setPolicyTypeUid(policyType.getUniqueId());
        this.setDerivedFrom(policyType.getDerivedFrom());
        this.setDescription(policyType.getDescription());
        this.setVersion(policyType.getVersion());
        if (policyType.getProperties() != null) {
            this.setProperties(policyType.getProperties().stream().map(PropertyDataDefinition::new).collect(Collectors.toList()));
        }
        this.setTargets(new HashMap<>());
    }

    @Override
    public String getNormalizedName() {
        return getName();
    }
}
