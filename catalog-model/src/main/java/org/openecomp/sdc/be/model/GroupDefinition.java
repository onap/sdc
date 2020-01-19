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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertiesOwner;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

public class GroupDefinition extends GroupDataDefinition implements PropertiesOwner{

	@JsonInclude
	private Map<String, List<CapabilityDefinition>> capabilities;
	
    public GroupDefinition() {
        super();
    }

    public GroupDefinition(GroupDataDefinition other) {
        super(other);
    }

    public GroupDefinition(GroupDefinition other) {
    	super(other);
		if(MapUtils.isNotEmpty(other.getCapabilities())) {
			this.setCapabilities(other.getCapabilities().entrySet()
					.stream()
					.collect(toMap(Map.Entry::getKey, e -> getCapabilitiesCopyList(e.getValue()))));
		}
	}

	public Map<String, List<CapabilityDefinition>> getCapabilities() {
		if(MapUtils.isEmpty(capabilities)) {
			capabilities = Maps.newHashMap();
		}
		return capabilities;
	}

	public void setCapabilities(Map<String, List<CapabilityDefinition>> capabilities) {
		this.capabilities = capabilities;
    }

    public List<GroupProperty> convertToGroupProperties() {
        List<GroupProperty> properties = null;
        List<PropertyDataDefinition> propList = super.getProperties();
        if(propList != null && !propList .isEmpty()){
			 properties = propList.stream().map(GroupProperty::new).collect(toList());
        }
        return properties;
    }

    public <T extends PropertyDataDefinition> void convertFromGroupProperties(List<T> properties) {
        if(properties != null && !properties .isEmpty()){
			List<PropertyDataDefinition> propList = properties.stream().map(PropertyDataDefinition::new).collect(toList());
            super.setProperties(propList);
        }
	}
	
    //returns true iff groupName has the same prefix has the resource
    public boolean isSamePrefix(String resourceName){
        return getName() != null  && getName().toLowerCase().trim().startsWith(resourceName.toLowerCase());
    }

    public void convertCapabilityDefinitions(Map<String, CapabilityDefinition> capabilities) {
        if(MapUtils.isNotEmpty(capabilities)){
            this.capabilities = capabilities.values().stream()
                                                      .collect(groupingBy(CapabilityDefinition::getType));
        }
    }

	@Override
	public String getNormalizedName() {
		return getName();
	}

	@JsonIgnore
	private List<CapabilityDefinition> getCapabilitiesCopyList(List<CapabilityDefinition> capabilities) {
		return Lists.newArrayList(capabilities.stream().map(CapabilityDefinition::new).collect(toList()));
	}

	public void updateCapabilitiesProperties(Map<String, Map<String, CapabilityDefinition>> capabilitiesInfo) {
		if(MapUtils.isNotEmpty(capabilities) && MapUtils.isNotEmpty(capabilitiesInfo)){
			capabilities.entrySet().forEach(e->updateCapabilitiesProperies(e.getValue(), capabilitiesInfo.get(e.getKey())));
		}
	}

	private void updateCapabilitiesProperies(List<CapabilityDefinition> capabilities, Map<String, CapabilityDefinition> capabilitiesInfo) {
		if(CollectionUtils.isNotEmpty(capabilities) && MapUtils.isNotEmpty(capabilitiesInfo)){
			capabilities.forEach(c->c.updateCapabilityProperties(capabilitiesInfo.get(c.getName())));
		}
	}

	public void updateEmptyCapabilitiesOwnerFields(){
    	if(MapUtils.isNotEmpty(this.capabilities)){
    		this.capabilities.values().stream()
					.flatMap(Collection::stream)
					.forEach(c -> c.updateEmptyCapabilityOwnerFields(getUniqueId(), getName(), CapabilityDataDefinition.OwnerType.GROUP));
		}
	}

}
