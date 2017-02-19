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

package org.openecomp.sdc.be.info;

import org.openecomp.sdc.be.resources.data.ESArtifactData;

public class ArtifactAccessInfo {

	public ArtifactAccessInfo() {
	}

	public ArtifactAccessInfo(ESArtifactData artifactData) {
		// this.name = artifactData.getArtifactName();
		this.id = artifactData.getId();
		// this.type = artifactData.getArtifactType();
		// this.description = artifactData.getArtifactDescription();
		// this.creator = artifactData.getArtifactCreator();
		// DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL);
		// Date creationTimestamp = artifactData.getArtifactCreationTimestamp();
		// this.creationTime = (creationTimestamp !=
		// null)?dateFormat.format(creationTimestamp): null;
		// Date updateTimestamp = artifactData.getArtifactLastUpdateTimestamp();
		// this.lastUpdateTime = (updateTimestamp !=
		// null)?dateFormat.format(updateTimestamp):null;
		// this.lastUpdater = artifactData.getArtifactLastUpdater();
		// if (artifactData.getArtifactChecksum() != null){
		// this.checksum = new String(artifactData.getArtifactChecksum());
		// } else {
		// this.checksum = null;
		// }
	}

	public ArtifactAccessInfo(ESArtifactData artifactData, String servletContext) {
		// this.name = artifactData.getArtifactName();
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder = urlBuilder.append(servletContext).append("/");
		// if (ArtifactDataEnum.COMPONENT_ARTIFACT.equals(resource)){
		urlBuilder.append("resources/")
				// .append(artifactData.getResourceId()).append("/")

				.append("/artifacts/");
		/*
		 * }else { ServiceArtifactData serviceArtifact = (ServiceArtifactData)artifactData; urlBuilder.append("services/") .append(serviceArtifact.getServiceName()).append("/") .append(serviceArtifact.getServiceVersion()) .append("/artifacts/")
		 * .append(serviceArtifact.getNodeTemplateName()) .append("/"); }
		 */
		// urlBuilder.append(artifactData.getArtifactName());
		this.url = urlBuilder.toString();

	}

	private String name;
	private String url;
	private String id;
	private String type;
	private String description;
	private String creator;
	private String creationTime;
	private String lastUpdater;
	private String lastUpdateTime;
	private String checksum;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(String creationTime) {
		this.creationTime = creationTime;
	}

	public String getLastUpdater() {
		return lastUpdater;
	}

	public void setLastUpdater(String lastUpdater) {
		this.lastUpdater = lastUpdater;
	}

	public String getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(String lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}
}
