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

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;

public class ComponentParametersView {

	boolean ignoreUsers = false;
	boolean ignoreGroups = false;
	boolean ignoreComponentInstances = false;
	boolean ignoreComponentInstancesProperties = false;
	boolean ignoreProperties = false;
	boolean ignoreCapabilities = false;
	boolean ignoreRequirements = false;
	boolean ignoreCategories = false;
	boolean ignoreAllVersions = false;
	boolean ignoreAdditionalInformation = false;
	boolean ignoreArtifacts = false;
	boolean ignoreInterfaces = false;
	boolean ignoreDerivedFrom = false;
	boolean ignoreAttributesFrom = false;
	boolean ignoreComponentInstancesAttributesFrom = false;
	boolean ignoreInputs = false;
	boolean ignoreComponentInstancesInputs = false;

	///////////////////////////////////////////////////////////////
	// When adding new member, please update the filter method.
	///////////////////////////////////////////////////////////////

	public Component filter(Component component, ComponentTypeEnum componentType) {

		if (ignoreUsers) {
			component.setCreatorUserId(null);
			component.setCreatorFullName(null);
			component.setLastUpdaterUserId(null);
			component.setLastUpdaterFullName(null);
		}

		if (ignoreGroups) {
			component.setGroups(null);
		}

		if (ignoreComponentInstances) {
			component.setComponentInstances(null);
			component.setComponentInstancesRelations(null);
		}

		if (ignoreComponentInstancesProperties) {
			component.setComponentInstancesProperties(null);
		}

		if (ignoreProperties) {
			switch (componentType) {
			case RESOURCE:
				((Resource) component).setProperties(null);
				break;
			default:
				break;
			}
		}

		if (ignoreCapabilities) {
			component.setCapabilities(null);
		}

		if (ignoreRequirements) {
			component.setRequirements(null);
		}

		if (ignoreCategories) {
			component.setCategories(null);
		}

		if (ignoreAllVersions) {
			component.setAllVersions(null);
		}

		if (ignoreAdditionalInformation) {
			switch (componentType) {
			case RESOURCE:
				((Resource) component).setAdditionalInformation(null);
				break;
			default:
				break;
			}
		}

		if (ignoreArtifacts) {
			component.setArtifacts(null);
			component.setSpecificComponetTypeArtifacts(null);
			component.setDeploymentArtifacts(null);
			component.setToscaArtifacts(null);
		}

		if (ignoreInterfaces) {
			switch (componentType) {
			case RESOURCE:
				((Resource) component).setInterfaces(null);
				break;
			default:
				break;
			}
		}

		if (ignoreDerivedFrom) {
			switch (componentType) {
			case RESOURCE:
				((Resource) component).setDerivedFrom(null);
				break;
			default:
				break;
			}
		}

		if (ignoreAttributesFrom) {
			switch (componentType) {
			case RESOURCE:
				((Resource) component).setAttributes(null);
				break;
			default:
				break;
			}
		}

		if (ignoreComponentInstancesAttributesFrom) {
			component.setComponentInstancesAttributes(null);
		}

		if (ignoreInputs) {
			component.setInputs(null);
		}

		if (ignoreComponentInstancesInputs) {
			component.setComponentInstancesInputs(null);
		}

		return component;

	}

	public void disableAll() {
		ignoreUsers = true;
		ignoreGroups = true;
		ignoreComponentInstances = true;
		ignoreComponentInstancesProperties = true;
		ignoreProperties = true;
		ignoreCapabilities = true;
		ignoreRequirements = true;
		ignoreCategories = true;
		ignoreAllVersions = true;
		ignoreAdditionalInformation = true;
		ignoreArtifacts = true;
		ignoreInterfaces = true;
		ignoreDerivedFrom = true;
		ignoreAttributesFrom = true;
		ignoreInputs = true;
		ignoreComponentInstancesAttributesFrom = true;
		ignoreComponentInstancesInputs = true;
	}

	public boolean isIgnoreGroups() {
		return ignoreGroups;
	}

	public void setIgnoreGroups(boolean ignoreGroups) {
		this.ignoreGroups = ignoreGroups;
	}

	public boolean isIgnoreComponentInstances() {
		return ignoreComponentInstances;
	}

	public void setIgnoreComponentInstances(boolean ignoreComponentInstances) {
		this.ignoreComponentInstances = ignoreComponentInstances;
	}

	public boolean isIgnoreProperties() {
		return ignoreProperties;
	}

	public void setIgnoreProperties(boolean ignoreProperties) {
		this.ignoreProperties = ignoreProperties;
	}

	public boolean isIgnoreCapabilities() {
		return ignoreCapabilities;
	}

	public void setIgnoreCapabilities(boolean ignoreCapabilities) {
		this.ignoreCapabilities = ignoreCapabilities;
	}

	public boolean isIgnoreRequirements() {
		return ignoreRequirements;
	}

	public void setIgnoreRequirements(boolean ignoreRequirements) {
		this.ignoreRequirements = ignoreRequirements;
	}

	public boolean isIgnoreCategories() {
		return ignoreCategories;
	}

	public void setIgnoreCategories(boolean ignoreCategories) {
		this.ignoreCategories = ignoreCategories;
	}

	public boolean isIgnoreAllVersions() {
		return ignoreAllVersions;
	}

	public void setIgnoreAllVersions(boolean ignoreAllVersions) {
		this.ignoreAllVersions = ignoreAllVersions;
	}

	public boolean isIgnoreAdditionalInformation() {
		return ignoreAdditionalInformation;
	}

	public void setIgnoreAdditionalInformation(boolean ignoreAdditionalInformation) {
		this.ignoreAdditionalInformation = ignoreAdditionalInformation;
	}

	public boolean isIgnoreArtifacts() {
		return ignoreArtifacts;
	}

	public void setIgnoreArtifacts(boolean ignoreArtifacts) {
		this.ignoreArtifacts = ignoreArtifacts;
	}

	public boolean isIgnoreComponentInstancesProperties() {
		return ignoreComponentInstancesProperties;
	}

	public void setIgnoreComponentInstancesProperties(boolean ignoreComponentInstancesProperties) {
		this.ignoreComponentInstancesProperties = ignoreComponentInstancesProperties;
	}

	public boolean isIgnoreComponentInstancesInputs() {
		return ignoreComponentInstancesInputs;
	}

	public void setIgnoreComponentInstancesInputs(boolean ignoreComponentInstancesInputs) {
		this.ignoreComponentInstancesInputs = ignoreComponentInstancesInputs;
	}

	public boolean isIgnoreInterfaces() {
		return ignoreInterfaces;
	}

	public void setIgnoreInterfaces(boolean ignoreInterfaces) {
		this.ignoreInterfaces = ignoreInterfaces;
	}

	public boolean isIgnoreAttributesFrom() {
		return ignoreAttributesFrom;
	}

	public void setIgnoreAttributesFrom(boolean ignoreAttributesFrom) {
		this.ignoreAttributesFrom = ignoreDerivedFrom;
	}

	public boolean isIgnoreComponentInstancesAttributesFrom() {
		return ignoreComponentInstancesAttributesFrom;
	}

	public void setIgnoreComponentInstancesAttributesFrom(boolean ignoreComponentInstancesAttributesFrom) {
		this.ignoreComponentInstancesAttributesFrom = ignoreComponentInstancesAttributesFrom;
	}

	public boolean isIgnoreDerivedFrom() {
		return ignoreDerivedFrom;
	}

	public void setIgnoreDerivedFrom(boolean ignoreDerivedFrom) {
		this.ignoreDerivedFrom = ignoreDerivedFrom;
	}

	public boolean isIgnoreUsers() {
		return ignoreUsers;
	}

	public void setIgnoreUsers(boolean ignoreUsers) {
		this.ignoreUsers = ignoreUsers;
	}

	public boolean isIgnoreInputs() {
		return ignoreInputs;
	}

	public void setIgnoreInputs(boolean ignoreInputs) {
		this.ignoreInputs = ignoreInputs;
	}

}
