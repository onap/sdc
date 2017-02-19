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
import java.util.Optional;

import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.common.api.Constants;

public class VfModuleArtifactPayload {
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

	private void setBaseValue(GroupDefinition group) {
		if (group.getProperties() != null) {
			Optional<GroupProperty> findBaseProperty = group.getProperties().stream().filter(p -> p.getName().equals(Constants.IS_BASE)).findAny();
			if (findBaseProperty.isPresent()) {
				isBase = Boolean.valueOf(findBaseProperty.get().getValue());
			}

		}
	}

	private String vfModuleModelName, vfModuleModelInvariantUUID, vfModuleModelVersion, vfModuleModelUUID, vfModuleModelDescription;
	private Boolean isBase;
	private List<String> artifacts;

	public List<String> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List<String> artifacts) {
		this.artifacts = artifacts;
	}

	public static int compareByGroupName(VfModuleArtifactPayload art1, VfModuleArtifactPayload art2) {
		Integer thisCounter = Integer.parseInt(art1.vfModuleModelName.split(Constants.MODULE_NAME_DELIMITER)[1]);
		Integer otherCounter = Integer.parseInt(art2.vfModuleModelName.split(Constants.MODULE_NAME_DELIMITER)[1]);
		return thisCounter.compareTo(otherCounter);
	}
}
