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

package org.onap.sdc.backend.ci.tests.datatypes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.be.model.HeatParameterDefinition;

import java.util.List;

@Getter
@Setter
public class ArtifactReqDetails {

	public ArtifactReqDetails() {

	}

	public ArtifactReqDetails(String artifactName, String artifactType, String artifactDescription, String payloadData,
			String artifactLabel) {
		super();
		this.artifactName = artifactName;
		this.artifactType = artifactType;
		this.description = artifactDescription;
		this.payloadData = payloadData;
		this.artifactLabel = artifactLabel;
	}

	public ArtifactReqDetails(String artifactLabel, ArtifactReqDetails a) {
		super();
		this.artifactName = a.getArtifactName();
		this.artifactType = a.getArtifactType();
		this.description = a.getArtifactType();
		this.payloadData = a.getPayload();
		this.artifactLabel = artifactLabel;
	}

	private String uniqueId;
	private String artifactName;
	private String artifactType;
	private String description;
	@Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE) private String payloadData;
	private String artifactLabel;
	@Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE) private String apiUrl;
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

	public String getPayload() {
		return payloadData;
	}

	public void setPayload(String payload) {
		this.payloadData = payload;
	}

	public void setPayloadData(String payloadData) {
		this.payloadData = payloadData;
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

	// public String getPayloadData() {
	// return payloadData;
	// }
	//
	// public void setPayloadData(String payloadData) {
	// this.payloadData = payloadData;
	// }

	// public String getUserIdCreator() {
	// return userIdCreator;
	// }
	//
	// public void setUserIdCreator(String userIdCreator) {
	// this.userIdCreator = userIdCreator;
	// }
	//

}
