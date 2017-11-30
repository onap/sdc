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

package org.openecomp.sdc.ci.tests.datatypes;

import java.util.List;

import org.openecomp.sdc.be.model.HeatParameterDefinition;

public class ArtifactReqDetails {

	public ArtifactReqDetails() {

	}

	public ArtifactReqDetails(String artifactName, String artifactType, String artifactDescription, String payloadData,
			String artifactLable) {
		super();
		this.artifactName = artifactName;
		this.artifactType = artifactType;
		this.description = artifactDescription;
		this.payloadData = payloadData;
		this.artifactLabel = artifactLable;
	}

	public ArtifactReqDetails(String artifactLable, ArtifactReqDetails a) {
		super();
		this.artifactName = a.getArtifactName();
		this.artifactType = a.getArtifactType();
		this.description = a.getArtifactType();
		this.payloadData = a.getPayload();
		this.artifactLabel = artifactLable;
	}

	private String uniqueId;
	private String artifactName;
	private String artifactType;
	private String description;
	private String payloadData;
	private String artifactLabel;
	private String apiUrl;
	private String artifactGroupType;
	private Integer timeout;
	private String userIdLastUpdater;
	private String creatorFullName;
	private String updaterFullName;
	private String artifactChecksum;
	private String artifactDisplayName;
	private List<HeatParameterDefinition> heatParameters;

	private boolean mandatory;
	private boolean serviceApi;

	public boolean isServiceApi() {
		return serviceApi;
	}

	public void setServiceApi(boolean serviceApi) {
		this.serviceApi = serviceApi;
	}

	public String getArtifactLabel() {
		return artifactLabel;
	}

	public void setArtifactLabel(String artifactLabel) {
		this.artifactLabel = artifactLabel;
	}

	public String getArtifactName() {
		return artifactName;
	}

	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}

	public String getArtifactType() {
		return artifactType;
	}

	public void setArtifactType(String artifactType) {
		this.artifactType = artifactType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPayload() {
		return payloadData;
	}

	public void setPayload(String payload) {
		this.payloadData = payload;
	}

	public void setPayloadData(String payloadData) {
		this.payloadData = payloadData;
	}

	public String getArtifactGroupType() {
		return artifactGroupType;
	}

	public void setArtifactGroupType(String artifactGroupType) {
		this.artifactGroupType = artifactGroupType;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public String getUrl() {
		return apiUrl;
	}

	public void setUrl(String url) {
		this.apiUrl = url;
	}

	@Override
	public String toString() {
		if (!apiUrl.isEmpty()) {
			return "ArtifactReqDetails [artifactName=" + artifactName + ", artifactType=" + artifactType
					+ ", description=" + description + ", payloadData=" + payloadData + ", artifactLabel="
					+ artifactLabel + ", mandatory=" + mandatory + ", url=" + apiUrl + "]";
		}

		return "ArtifactReqDetails [artifactName=" + artifactName + ", artifactType=" + artifactType + ", description="
				+ description + ", payloadData=" + payloadData + ", artifactLabel=" + artifactLabel
				+ ", artifactUniqueId=" + uniqueId + ", mandatory=" + mandatory + ", serviceApi=" + serviceApi + "]";

	}

	public String getArtifactDisplayName() {

		return artifactDisplayName;
	}

	public void setArtifactDisplayName(String artifactDisplayName) {
		this.artifactDisplayName = artifactDisplayName;
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

	public String getArtifactChecksum() {
		return artifactChecksum;
	}

	public void setArtifactChecksum(String artifactChecksum) {
		this.artifactChecksum = artifactChecksum;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String artifactUniqueId) {
		this.uniqueId = artifactUniqueId;
	}

	public List<HeatParameterDefinition> getHeatParameters() {
		return heatParameters;
	}

	public void setHeatParameters(List<HeatParameterDefinition> heatParameters) {
		this.heatParameters = heatParameters;
	}

}
