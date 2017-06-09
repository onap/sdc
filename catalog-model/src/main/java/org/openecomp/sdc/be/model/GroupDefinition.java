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

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

public class GroupDefinition extends GroupDataDefinition implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -852613634651112247L;

	// properties (properties should be defined in the group type, the
	// properties here are actually the value for the properties)
	



	// The unique id of the type of this group


	public GroupDefinition() {
		super();
	}

	public GroupDefinition(GroupDataDefinition other) {
		super(other);
	}

	public GroupDefinition(GroupDefinition other) {
		this.setName(other.getName());
		this.setUniqueId(other.getUniqueId());
		this.setType(other.getType());
		this.setVersion(other.getVersion());
		this.setInvariantUUID(other.getInvariantUUID());
		this.setGroupUUID(other.getGroupUUID());
		this.setDescription(other.getDescription());
		this.setTypeUid(other.getTypeUid());
		this.setProperties(other.getProperties());
		
	}

	public List<GroupProperty> convertToGroupProperties() {
		List<GroupProperty> properties = null;
		List<PropertyDataDefinition> propList = super.getProperties();
		if(propList != null && !propList .isEmpty()){
			 properties = propList.stream().map(pr -> new GroupProperty(pr)).collect(Collectors.toList());
		}
		return properties;
	}

	public void convertFromGroupProperties(List<GroupProperty> properties) {
		if(properties != null && !properties .isEmpty()){
			List<PropertyDataDefinition> propList = properties.stream().map(pr -> new PropertyDataDefinition(pr)).collect(Collectors.toList());
			super.setProperties(propList);
		}
		
	}

}
