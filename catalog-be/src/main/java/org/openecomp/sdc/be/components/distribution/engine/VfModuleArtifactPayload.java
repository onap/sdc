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

package org.openecomp.sdc.be.components.distribution.engine;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.GroupInstanceProperty;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.common.api.Constants;

public class VfModuleArtifactPayload {
	
	private String vfModuleModelName, vfModuleModelInvariantUUID, vfModuleModelVersion, vfModuleModelUUID, vfModuleModelCustomizationUUID, vfModuleModelDescription;
	private Boolean isBase;
	private List<String> artifacts;
	private Map< String, Object> properties;
	
	public VfModuleArtifactPayload(GroupDefinition group) {
		vfModuleModelName = group.getName();
		vfModuleModelInvariantUUID = group.getInvariantUUID();
		vfModuleModelVersion = group.getVersion();
		vfModuleModelUUID = group.getGroupUUID();
		vfModuleModelDescription = group.getDescription();

		artifacts = group.getArtifactsUuid();
		// Base Value is set from properties
		setBaseValue(group);

	}
	
	public VfModuleArtifactPayload(GroupInstance group) {
		vfModuleModelName = group.getGroupName();
		vfModuleModelInvariantUUID = group.getInvariantUUID();
		vfModuleModelVersion = group.getVersion();
		vfModuleModelUUID = group.getGroupUUID();
		vfModuleModelCustomizationUUID = group.getCustomizationUUID();
		vfModuleModelDescription = group.getDescription();

		artifacts = group.getArtifactsUuid();
		artifacts.addAll(group.getGroupInstanceArtifactsUuid());
		
		// Base Value is set from properties
		setBaseValue(group);
		
		if(group.convertToGroupInstancesProperties() != null)
			setProperties(group.convertToGroupInstancesProperties());
			//converts List of GroupInstanceProperties to Map propertyName : GroupInstanceProperty ()
			//setProperties(group.getGroupInstancesProperties().stream().collect(Collectors.toMap(p->p.getName(), p->p)));

	}

	private void setBaseValue(GroupInstance group) {
		if (group.convertToGroupInstancesProperties() != null) {
			Optional<GroupInstanceProperty> findBaseProperty = group.convertToGroupInstancesProperties().stream().filter(p -> p.getName().equals(Constants.IS_BASE)).findAny();
			if (findBaseProperty.isPresent()) {
				isBase = Boolean.valueOf(findBaseProperty.get().getValue());
			}

		}
	}
	private void setBaseValue(GroupDefinition group) {
		if (group.getProperties() != null) {
			Optional<GroupProperty> findBaseProperty = group.convertToGroupProperties().stream().filter(p -> p.getName().equals(Constants.IS_BASE)).findAny();
			if (findBaseProperty.isPresent()) {
				isBase = Boolean.valueOf(findBaseProperty.get().getValue());
			}

		}
	}



	public List<String> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List<String> artifacts) {
		this.artifacts = artifacts;
	}
	
	

	public Map<String, Object> getProperties() {
		return properties;
	}

	/*public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}*/
	
	public void setProperties(List<GroupInstanceProperty> properties) {
		this.properties  = properties.stream().filter(p -> !p.getName().equals(Constants.IS_BASE)).collect(
	                Collectors.toMap(x -> x.getName(), x -> x.getValue() == null? "":x.getValue() ));
	}

	public static int compareByGroupName(VfModuleArtifactPayload art1, VfModuleArtifactPayload art2) {
		Float thisCounter = Float.parseFloat(art1.vfModuleModelName.split(Constants.MODULE_NAME_DELIMITER)[1].replace(' ', '.'));
		Float otherCounter = Float.parseFloat(art2.vfModuleModelName.split(Constants.MODULE_NAME_DELIMITER)[1].replace(' ', '.'));
		return thisCounter.compareTo(otherCounter);
	}
}
