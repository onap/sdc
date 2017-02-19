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

package org.openecomp.sdc.be.datatypes.elements;

import java.util.ArrayList;
import java.util.List;

import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;

import java.io.Serializable;

public class ArtifactDataDefinition implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1691343090754083941L;

	/**
	 * The unique id of the artifact
	 */
	private String uniqueId;

	/**
	 * Tosca logical name
	 */
	// private String logicalName;

	/** This attribute specifies the type of this artifact. */
	private String artifactType;

	/** Specifies the reference of the artifact. uri to the SWIFT */
	private String artifactRef;

	/** Specifies the display name of the artifact. */
	private String artifactName;

	/**
	 * Non TOSCA compliant property
	 */
	private String artifactRepository;

	/**
	 * Checksum value of the uploaded artifact file retrieved from "Content-MD5"?
	 * header of the HTTP POST/PUT request. Should be updated each time when the
	 * artifact file is updated.
	 */
	private String artifactChecksum;

	/**
	 * artifact creator
	 */
	private String userIdCreator;

	/**
	 * USER ID of the last resource (artifact) updater
	 */
	private String userIdLastUpdater;

	/**
	 * Full name of artifact creator
	 */
	private String creatorFullName;

	/**
	 * Full name of the last resource (artifact) updater
	 */
	private String updaterFullName;

	/**
	 * Timestamp of the resource (artifact) creation
	 */
	private Long creationDate;

	/**
	 * Timestamp of the last resource (artifact) creation
	 */
	private Long lastUpdateDate;

	/**
	 * Id of artifact data in ES
	 */
	private String esId;

	/**
	 * Logical artifact name. Used by TOSCA
	 */
	private String artifactLabel;

	private String artifactCreator;

	private String description;

	private Boolean mandatory = Boolean.FALSE;

	private String artifactDisplayName;

	private String apiUrl;

	private Boolean serviceApi = Boolean.FALSE;
	
	/**
	 * Flag that set to TRUE if generated from AI&I Artifact generator to
	 * distinguish between manually uploaded and generated artifacts
	 */
	private Boolean generated = Boolean.FALSE;
	
	private ArtifactGroupTypeEnum artifactGroupType;
	private Integer timeout;
	private String artifactVersion;
	private String artifactUUID;
	private Long payloadUpdateDate;
	private Long heatParamsUpdateDate;

	private List<String> requiredArtifacts;

	public ArtifactDataDefinition() {
		artifactVersion = "0";
	}

	public ArtifactDataDefinition(ArtifactDataDefinition a) {
		this.uniqueId = a.uniqueId;
		this.artifactType = a.artifactType;
		this.artifactRef = a.artifactRef;
		this.artifactName = a.artifactName;
		this.artifactRepository = a.artifactRepository;
		this.artifactChecksum = a.artifactChecksum;
		this.userIdCreator = a.userIdCreator;
		this.userIdLastUpdater = a.userIdLastUpdater;
		this.creatorFullName = a.creatorFullName;
		this.updaterFullName = a.updaterFullName;
		this.creationDate = a.creationDate;
		this.lastUpdateDate = a.lastUpdateDate;
		this.description = a.description;
		this.esId = a.esId;
		this.artifactLabel = a.artifactLabel;
		this.artifactCreator = a.artifactCreator;
		this.mandatory = a.mandatory;
		this.artifactDisplayName = a.artifactDisplayName;
		this.apiUrl = a.apiUrl;
		this.serviceApi = a.serviceApi;
		this.artifactGroupType = a.artifactGroupType;
		this.timeout = a.timeout;
		this.artifactVersion = a.artifactVersion;
		this.artifactUUID = a.artifactUUID;
		this.payloadUpdateDate = a.payloadUpdateDate;
		this.heatParamsUpdateDate = a.heatParamsUpdateDate;
		this.setGenerated(a.getGenerated());
		if (a.requiredArtifacts != null)
			this.requiredArtifacts = new ArrayList<>(a.getRequiredArtifacts());
	}

	public String getArtifactName() {
		return artifactName != null ? artifactName : artifactRef;
	}

	public String getArtifactType() {
		return artifactType;
	}

	public void setArtifactType(String artifactType) {
		this.artifactType = artifactType;
	}

	public String getArtifactRef() {
		return artifactRef;
	}

	public void setArtifactRef(String artifactRef) {
		this.artifactRef = artifactRef;
	}

	public String getArtifactRepository() {
		return artifactRepository;
	}

	public void setArtifactRepository(String artifactRepository) {
		this.artifactRepository = artifactRepository;
	}

	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}

	public String getArtifactChecksum() {
		return artifactChecksum;
	}

	public void setArtifactChecksum(String artifactChecksum) {
		this.artifactChecksum = artifactChecksum;
	}

	public String getUserIdCreator() {
		return userIdCreator;
	}

	public void setUserIdCreator(String userIdCreator) {
		this.userIdCreator = userIdCreator;
	}

	public String getUserIdLastUpdater() {
		return userIdLastUpdater;
	}

	public void setUserIdLastUpdater(String userIdLastUpdater) {
		this.userIdLastUpdater = userIdLastUpdater;
	}

	public String getCreatorFullName() {
		return creatorFullName;
	}

	public void setCreatorFullName(String creatorFullName) {
		this.creatorFullName = creatorFullName;
	}

	public String getUpdaterFullName() {
		return updaterFullName;
	}

	public void setUpdaterFullName(String updaterFullName) {
		this.updaterFullName = updaterFullName;
	}

	public Long getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Long creationDate) {
		this.creationDate = creationDate;
	}

	public Long getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Long lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getArtifactLabel() {
		return artifactLabel;
	}

	public void setArtifactLabel(String artifactLabel) {
		this.artifactLabel = artifactLabel;
	}

	public String getEsId() {
		return esId;
	}

	public void setEsId(String esId) {
		this.esId = esId;
	}

	public String getArtifactCreator() {
		return artifactCreator;
	}

	public void setArtifactCreator(String artifactCreator) {
		this.artifactCreator = artifactCreator;
	}

	public Boolean getMandatory() {
		return mandatory;
	}

	public void setMandatory(Boolean mandatory) {
		this.mandatory = mandatory;
	}

	public String getArtifactDisplayName() {
		return artifactDisplayName;
	}

	public void setArtifactDisplayName(String artifactDisplayName) {
		this.artifactDisplayName = artifactDisplayName;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public Boolean getServiceApi() {
		return serviceApi;
	}

	public void setServiceApi(Boolean serviceApi) {
		this.serviceApi = serviceApi;
	}

	public ArtifactGroupTypeEnum getArtifactGroupType() {
		return artifactGroupType;
	}

	public void setArtifactGroupType(ArtifactGroupTypeEnum artifactGroupType) {
		this.artifactGroupType = artifactGroupType;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public String getArtifactVersion() {
		return artifactVersion;
	}

	public void setArtifactVersion(String artifactVersion) {
		this.artifactVersion = artifactVersion;
	}

	public String getArtifactUUID() {
		return artifactUUID;
	}

	public void setArtifactUUID(String artifactUUID) {
		this.artifactUUID = artifactUUID;
	}

	public Long getPayloadUpdateDate() {
		return payloadUpdateDate;
	}

	public void setPayloadUpdateDate(Long payloadUpdateDate) {
		this.payloadUpdateDate = payloadUpdateDate;
	}

	public Long getHeatParamsUpdateDate() {
		return heatParamsUpdateDate;
	}

	public void setHeatParamsUpdateDate(Long heatParamsUpdateDate) {
		this.heatParamsUpdateDate = heatParamsUpdateDate;
	}

	public List<String> getRequiredArtifacts() {
		return requiredArtifacts;
	}

	public void setRequiredArtifacts(List<String> requiredArtifacts) {
		this.requiredArtifacts = requiredArtifacts;
	}
	
	public Boolean getGenerated() {
		return generated;
	}

	public void setGenerated(Boolean generated) {
		this.generated = generated;
	}

	@Override
	public String toString() {
		return "ArtifactDataDefinition [uniqueId=" + uniqueId + ", artifactType=" + artifactType + ", artifactRef="
				+ artifactRef + ", artifactName=" + artifactName + ", artifactRepository=" + artifactRepository
				+ ", artifactChecksum=" + artifactChecksum + ", userIdCreator=" + userIdCreator + ", userIdLastUpdater="
				+ userIdLastUpdater + ", creatorFullName=" + creatorFullName + ", updaterFullName=" + updaterFullName
				+ ", creationDate=" + creationDate + ", lastUpdateDate=" + lastUpdateDate + ", esId=" + esId
				+ ", artifactLabel=" + artifactLabel + ", artifactCreator=" + artifactCreator + ", description="
				+ description + ", mandatory=" + mandatory + ", artifactDisplayName=" + artifactDisplayName
				+ ", apiUrl=" + apiUrl + ", serviceApi=" + serviceApi + ", artifactGroupType=" + artifactGroupType
				+ ", timeout=" + timeout + ", artifactVersion=" + artifactVersion + ", artifactUUID=" + artifactUUID
				+ ", payloadUpdateDate=" + payloadUpdateDate + ", heatParamsUpdateDate=" + heatParamsUpdateDate
				+ ", requiredArtifacts=" + requiredArtifacts + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((apiUrl == null) ? 0 : apiUrl.hashCode());
		result = prime * result + ((artifactChecksum == null) ? 0 : artifactChecksum.hashCode());
		result = prime * result + ((artifactCreator == null) ? 0 : artifactCreator.hashCode());
		result = prime * result + ((artifactDisplayName == null) ? 0 : artifactDisplayName.hashCode());
		result = prime * result + ((artifactGroupType == null) ? 0 : artifactGroupType.hashCode());
		result = prime * result + ((artifactLabel == null) ? 0 : artifactLabel.hashCode());
		result = prime * result + ((artifactName == null) ? 0 : artifactName.hashCode());
		result = prime * result + ((artifactRef == null) ? 0 : artifactRef.hashCode());
		result = prime * result + ((artifactRepository == null) ? 0 : artifactRepository.hashCode());
		result = prime * result + ((artifactType == null) ? 0 : artifactType.hashCode());
		result = prime * result + ((artifactUUID == null) ? 0 : artifactUUID.hashCode());
		result = prime * result + ((artifactVersion == null) ? 0 : artifactVersion.hashCode());
		result = prime * result + ((userIdCreator == null) ? 0 : userIdCreator.hashCode());
		result = prime * result + ((userIdLastUpdater == null) ? 0 : userIdLastUpdater.hashCode());
		result = prime * result + ((creationDate == null) ? 0 : creationDate.hashCode());
		result = prime * result + ((creatorFullName == null) ? 0 : creatorFullName.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((esId == null) ? 0 : esId.hashCode());
		result = prime * result + ((heatParamsUpdateDate == null) ? 0 : heatParamsUpdateDate.hashCode());
		result = prime * result + ((lastUpdateDate == null) ? 0 : lastUpdateDate.hashCode());
		result = prime * result + ((mandatory == null) ? 0 : mandatory.hashCode());
		result = prime * result + ((payloadUpdateDate == null) ? 0 : payloadUpdateDate.hashCode());
		result = prime * result + ((requiredArtifacts == null) ? 0 : requiredArtifacts.hashCode());
		result = prime * result + ((serviceApi == null) ? 0 : serviceApi.hashCode());
		result = prime * result + ((timeout == null) ? 0 : timeout.hashCode());
		result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
		result = prime * result + ((updaterFullName == null) ? 0 : updaterFullName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArtifactDataDefinition other = (ArtifactDataDefinition) obj;
		if (apiUrl == null) {
			if (other.apiUrl != null)
				return false;
		} else if (!apiUrl.equals(other.apiUrl))
			return false;
		if (artifactChecksum == null) {
			if (other.artifactChecksum != null)
				return false;
		} else if (!artifactChecksum.equals(other.artifactChecksum))
			return false;
		if (artifactCreator == null) {
			if (other.artifactCreator != null)
				return false;
		} else if (!artifactCreator.equals(other.artifactCreator))
			return false;
		if (artifactDisplayName == null) {
			if (other.artifactDisplayName != null)
				return false;
		} else if (!artifactDisplayName.equals(other.artifactDisplayName))
			return false;
		if (artifactGroupType != other.artifactGroupType)
			return false;
		if (artifactLabel == null) {
			if (other.artifactLabel != null)
				return false;
		} else if (!artifactLabel.equals(other.artifactLabel))
			return false;
		if (artifactName == null) {
			if (other.artifactName != null)
				return false;
		} else if (!artifactName.equals(other.artifactName))
			return false;
		if (artifactRef == null) {
			if (other.artifactRef != null)
				return false;
		} else if (!artifactRef.equals(other.artifactRef))
			return false;
		if (artifactRepository == null) {
			if (other.artifactRepository != null)
				return false;
		} else if (!artifactRepository.equals(other.artifactRepository))
			return false;
		if (artifactType == null) {
			if (other.artifactType != null)
				return false;
		} else if (!artifactType.equals(other.artifactType))
			return false;
		if (artifactUUID == null) {
			if (other.artifactUUID != null)
				return false;
		} else if (!artifactUUID.equals(other.artifactUUID))
			return false;
		if (artifactVersion == null) {
			if (other.artifactVersion != null)
				return false;
		} else if (!artifactVersion.equals(other.artifactVersion))
			return false;
		if (userIdCreator == null) {
			if (other.userIdCreator != null)
				return false;
		} else if (!userIdCreator.equals(other.userIdCreator))
			return false;
		if (userIdLastUpdater == null) {
			if (other.userIdLastUpdater != null)
				return false;
		} else if (!userIdLastUpdater.equals(other.userIdLastUpdater))
			return false;
		if (creationDate == null) {
			if (other.creationDate != null)
				return false;
		} else if (!creationDate.equals(other.creationDate))
			return false;
		if (creatorFullName == null) {
			if (other.creatorFullName != null)
				return false;
		} else if (!creatorFullName.equals(other.creatorFullName))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (esId == null) {
			if (other.esId != null)
				return false;
		} else if (!esId.equals(other.esId))
			return false;
		if (heatParamsUpdateDate == null) {
			if (other.heatParamsUpdateDate != null)
				return false;
		} else if (!heatParamsUpdateDate.equals(other.heatParamsUpdateDate))
			return false;
		if (lastUpdateDate == null) {
			if (other.lastUpdateDate != null)
				return false;
		} else if (!lastUpdateDate.equals(other.lastUpdateDate))
			return false;
		if (mandatory == null) {
			if (other.mandatory != null)
				return false;
		} else if (!mandatory.equals(other.mandatory))
			return false;
		if (payloadUpdateDate == null) {
			if (other.payloadUpdateDate != null)
				return false;
		} else if (!payloadUpdateDate.equals(other.payloadUpdateDate))
			return false;
		if (requiredArtifacts == null) {
			if (other.requiredArtifacts != null)
				return false;
		} else if (!requiredArtifacts.equals(other.requiredArtifacts))
			return false;
		if (serviceApi == null) {
			if (other.serviceApi != null)
				return false;
		} else if (!serviceApi.equals(other.serviceApi))
			return false;
		if (timeout == null) {
			if (other.timeout != null)
				return false;
		} else if (!timeout.equals(other.timeout))
			return false;
		if (uniqueId == null) {
			if (other.uniqueId != null)
				return false;
		} else if (!uniqueId.equals(other.uniqueId))
			return false;
		if (updaterFullName == null) {
			if (other.updaterFullName != null)
				return false;
		} else if (!updaterFullName.equals(other.updaterFullName))
			return false;
		return true;
	}
}
