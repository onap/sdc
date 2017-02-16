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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;

public class ArtifactInfoImpl implements IArtifactInfo {

	private String artifactName;
	private ArtifactTypeEnum artifactType;
	private String artifactURL;
	private String artifactChecksum;
	private String artifactDescription;
	private Integer artifactTimeout;
	private String artifactUUID;
	private String artifactVersion;
	private String generatedFromUUID;
	private List<String> relatedArtifacts;

	public ArtifactInfoImpl() {
	}

	private ArtifactInfoImpl(ArtifactDefinition artifactDef, String generatedFromUUID, List<String> relatedArtifacts) {
		artifactName = artifactDef.getArtifactName();
		artifactType = ArtifactTypeEnum.findType(artifactDef.getArtifactType());
		artifactChecksum = artifactDef.getArtifactChecksum();
		artifactDescription = artifactDef.getDescription();
		artifactTimeout = artifactDef.getTimeout();
		artifactUUID = artifactDef.getArtifactUUID();
		artifactVersion = artifactDef.getArtifactVersion();
		this.relatedArtifacts = relatedArtifacts;
		this.generatedFromUUID = generatedFromUUID;
	}

	public static List<ArtifactInfoImpl> convertToArtifactInfoImpl(Service service, ComponentInstance resourceInstance, Collection<ArtifactDefinition> list) {
		List<ArtifactInfoImpl> ret = new ArrayList<ArtifactInfoImpl>();
		Map<String, List<ArtifactDefinition>> artifactIdToDef = list.stream().collect(Collectors.groupingBy(ArtifactDefinition::getUniqueId));
		if (list != null) {
			for (ArtifactDefinition artifactDef : list) {
				String generatedFromUUID = null;
				if (artifactDef.getGeneratedFromId() != null && !artifactDef.getGeneratedFromId().isEmpty()) {
					ArtifactDefinition artifactFrom = artifactIdToDef.get(artifactDef.getGeneratedFromId()).get(0);
					generatedFromUUID = artifactFrom.getArtifactUUID();
				}
				ArtifactInfoImpl artifactInfoImpl = new ArtifactInfoImpl(artifactDef, generatedFromUUID, getUpdatedRequiredArtifactsFromNamesToUuids(artifactDef, resourceInstance.getDeploymentArtifacts()));
				String artifactURL = ServiceDistributionArtifactsBuilder.buildResourceInstanceArtifactUrl(service, resourceInstance, artifactDef.getArtifactName());
				artifactInfoImpl.setArtifactURL(artifactURL);
				ret.add(artifactInfoImpl);
			}
		}
		return ret;

	}

	public static List<ArtifactInfoImpl> convertServiceArtifactToArtifactInfoImpl(Service service, Collection<ArtifactDefinition> list) {
		List<ArtifactInfoImpl> ret = new ArrayList<ArtifactInfoImpl>();
		Map<String, List<ArtifactDefinition>> artifactIdToDef = list.stream().collect(Collectors.groupingBy(ArtifactDefinition::getUniqueId));
		if (list != null) {
			for (ArtifactDefinition artifactDef : list) {
				String generatedFromUUID = null;
				if (artifactDef.getGeneratedFromId() != null && !artifactDef.getGeneratedFromId().isEmpty()) {
					ArtifactDefinition artifactFrom = artifactIdToDef.get(artifactDef.getGeneratedFromId()).get(0);
					generatedFromUUID = artifactFrom.getArtifactUUID();
				}
				ArtifactInfoImpl artifactInfoImpl = new ArtifactInfoImpl(artifactDef, generatedFromUUID, getUpdatedRequiredArtifactsFromNamesToUuids(artifactDef, service.getDeploymentArtifacts()));
				String artifactURL = ServiceDistributionArtifactsBuilder.buildServiceArtifactUrl(service, artifactDef.getArtifactName());
				artifactInfoImpl.setArtifactURL(artifactURL);
				ret.add(artifactInfoImpl);
			}
		}
		return ret;

	}

	private static List<String> getUpdatedRequiredArtifactsFromNamesToUuids(ArtifactDefinition artifactDefinition, Map<String, ArtifactDefinition> artifacts) {
		List<String> requiredArtifacts = null;
		if (artifactDefinition != null && artifactDefinition.getRequiredArtifacts() != null && !artifactDefinition.getRequiredArtifacts().isEmpty() && artifacts != null && !artifacts.isEmpty()) {
			requiredArtifacts = artifacts.values().stream().filter(art -> artifactDefinition.getRequiredArtifacts().contains(art.getArtifactName())).map(art -> art.getArtifactUUID()).collect(Collectors.toList());
		}
		return requiredArtifacts;
	}

	public String getArtifactName() {
		return artifactName;
	}

	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}

	public ArtifactTypeEnum getArtifactType() {
		return artifactType;
	}

	public void setArtifactType(ArtifactTypeEnum artifactType) {
		this.artifactType = artifactType;
	}

	public String getArtifactURL() {
		return artifactURL;
	}

	public void setArtifactURL(String artifactURL) {
		this.artifactURL = artifactURL;
	}

	public String getArtifactChecksum() {
		return artifactChecksum;
	}

	public void setArtifactChecksum(String artifactChecksum) {
		this.artifactChecksum = artifactChecksum;
	}

	public String getArtifactDescription() {
		return artifactDescription;
	}

	public void setArtifactDescription(String artifactDescription) {
		this.artifactDescription = artifactDescription;
	}

	public Integer getArtifactTimeout() {
		return artifactTimeout;
	}

	public void setArtifactTimeout(Integer artifactTimeout) {
		this.artifactTimeout = artifactTimeout;
	}

	public List<String> getRelatedArtifacts() {
		return relatedArtifacts;
	}

	public void setRelatedArtifacts(List<String> relatedArtifacts) {
		this.relatedArtifacts = relatedArtifacts;
	}

	@Override
	public String toString() {
		return "ArtifactInfoImpl [artifactName=" + artifactName + ", artifactType=" + artifactType + ", artifactURL=" + artifactURL + ", artifactChecksum=" + artifactChecksum + ", artifactDescription=" + artifactDescription + ", artifactTimeout="
				+ artifactTimeout + ", artifactUUID=" + artifactUUID + ", artifactVersion=" + artifactVersion + ", generatedFromUUID=" + generatedFromUUID + ", relatedArtifacts=" + relatedArtifacts + "]";
	}

	public String getArtifactUUID() {
		return artifactUUID;
	}

	public void setArtifactUUID(String artifactUUID) {
		this.artifactUUID = artifactUUID;
	}

	public String getArtifactVersion() {
		return artifactVersion;
	}

	public void setArtifactVersion(String artifactVersion) {
		this.artifactVersion = artifactVersion;
	}

	public String getGeneratedFromUUID() {
		return generatedFromUUID;
	}

	public void setGeneratedFromUUID(String generatedFromUUID) {
		this.generatedFromUUID = generatedFromUUID;
	}

}
