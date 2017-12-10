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

import org.openecomp.sdc.be.datatypes.elements.GroupInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

public class GroupInstance extends GroupInstanceDataDefinition implements Serializable {

	private static final long serialVersionUID = -2066335818115254401L;
	
	public GroupInstance() {
		super();
	}
	
	public GroupInstance(GroupInstanceDataDefinition r) {
		super(r);
	}
	
	public List<GroupInstanceProperty>  convertToGroupInstancesProperties() {
		List<GroupInstanceProperty> groupInstancesProperties = null;
		List<PropertyDataDefinition> propertiesList = super.getProperties();
		if(propertiesList != null && !propertiesList .isEmpty()){
			groupInstancesProperties = propertiesList.stream().map(p -> new GroupInstanceProperty(p)).collect(Collectors.toList());
		}
		return groupInstancesProperties;
	}
	
	public void convertFromGroupInstancesProperties(List<GroupInstanceProperty> groupInstancesProperties) {
		if(groupInstancesProperties != null && !groupInstancesProperties .isEmpty()){
			List<PropertyDataDefinition> propList = groupInstancesProperties.stream().map(p -> new PropertyDataDefinition(p)).collect(Collectors.toList());
			super.setProperties(propList);
		}
	}

}
