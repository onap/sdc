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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.model.ArtifactDefinition;

public class MergedArtifactInfo {

	private List<ArtifactDefinition> createdArtifact;
	private ArtifactTemplateInfo jsonArtifactTemplate;
	private Set<String> parsetArtifactsNames;

	public List<ArtifactDefinition> getCreatedArtifact() {
		return createdArtifact;
	}

	public void setCreatedArtifact(List<ArtifactDefinition> createdArtifact) {
		this.createdArtifact = createdArtifact;
		parsetArtifactsNames = new HashSet<String>();
		parsetArtifactsNames.add(jsonArtifactTemplate.getFileName());
		List<ArtifactTemplateInfo> relatedGroupTemplateList = jsonArtifactTemplate.getRelatedArtifactsInfo();
		if (relatedGroupTemplateList != null && !relatedGroupTemplateList.isEmpty()) {
			this.createArtifactsGroupSet(relatedGroupTemplateList, parsetArtifactsNames);
		}

	}

	public ArtifactTemplateInfo getJsonArtifactTemplate() {
		return jsonArtifactTemplate;
	}

	public void setJsonArtifactTemplate(ArtifactTemplateInfo jsonArtifactTemplate) {
		this.jsonArtifactTemplate = jsonArtifactTemplate;
	}

	public List<ArtifactTemplateInfo> getListToAssociateArtifactToGroup() {
		List<ArtifactTemplateInfo> resList = new ArrayList<ArtifactTemplateInfo>();
		List<ArtifactTemplateInfo> relatedArtifacts = jsonArtifactTemplate.getRelatedArtifactsInfo();
		if (relatedArtifacts != null && !relatedArtifacts.isEmpty()) {
			getNewArtifactsInGroup(resList, relatedArtifacts);
		}
		return resList;
	}

	public List<ArtifactDefinition> getListToDissotiateArtifactFromGroup(List<ArtifactDefinition> deletedArtifacts) {
		List<ArtifactDefinition> resList = new ArrayList<ArtifactDefinition>();
		for (ArtifactDefinition artifactDefinition : createdArtifact) {
			boolean isDissotiate = true;
			if(parsetArtifactsNames.contains(artifactDefinition.getArtifactName())){
				isDissotiate = false;
			}else{
				if (artifactDefinition.getGeneratedFromId() != null && !artifactDefinition.getGeneratedFromId().isEmpty()){
					Optional<ArtifactDefinition> op = createdArtifact.stream().filter(p -> p.getUniqueId().equals(artifactDefinition.getGeneratedFromId())).findAny();
					if(op.isPresent()){
						ArtifactDefinition generatedFromArt = op.get();
						if(parsetArtifactsNames.contains(generatedFromArt.getArtifactName())){
							isDissotiate = false;
						}
					}
					else{
						isDissotiate = true;
					}
						
				}
			}
			if (isDissotiate) {
				boolean isDeleted = false;
				for (ArtifactDefinition deletedArtifact : deletedArtifacts) {
					if (artifactDefinition.getUniqueId().equalsIgnoreCase(deletedArtifact.getUniqueId())) {
						isDeleted = true;
						break;
					}

				}
				if (!isDeleted)
					resList.add(artifactDefinition);
			}

		}

		return resList;
	}

	public List<ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>> getListToUpdateArtifactInGroup() {
		List<ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>> resList = new ArrayList<ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>>();
		for (ArtifactDefinition artifactDefinition : createdArtifact) {
			if (artifactDefinition.getArtifactName().equalsIgnoreCase(jsonArtifactTemplate.getFileName())) {
				resList.add(new ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>(artifactDefinition, jsonArtifactTemplate));
			}
		}
		List<ArtifactTemplateInfo> relatedArtifacts = jsonArtifactTemplate.getRelatedArtifactsInfo();
		if (relatedArtifacts != null && !relatedArtifacts.isEmpty()) {
			getUpdateArtifactsInGroup(resList, relatedArtifacts);
		}
		return resList;
	}

	private void getUpdateArtifactsInGroup(List<ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>> resList, List<ArtifactTemplateInfo> jsonArtifacts) {

		for (ArtifactTemplateInfo artifactTemplateInfo : jsonArtifacts) {

			for (ArtifactDefinition artifactDefinition : createdArtifact) {
				if (artifactDefinition.getArtifactName().equalsIgnoreCase(artifactTemplateInfo.getFileName())) {
					resList.add(new ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>(artifactDefinition, artifactTemplateInfo));
				}
			}

			List<ArtifactTemplateInfo> relatedArtifacts = artifactTemplateInfo.getRelatedArtifactsInfo();
			if (relatedArtifacts != null && !relatedArtifacts.isEmpty()) {
				getUpdateArtifactsInGroup(resList, relatedArtifacts);
			}
		}
	}

	private void getNewArtifactsInGroup(List<ArtifactTemplateInfo> resList, List<ArtifactTemplateInfo> jsonArtifacts) {

		for (ArtifactTemplateInfo artifactTemplateInfo : jsonArtifacts) {
			boolean isNewArtifact = true;
			for (ArtifactDefinition artifactDefinition : createdArtifact) {
				if (artifactDefinition.getArtifactName().equalsIgnoreCase(artifactTemplateInfo.getFileName())) {
					isNewArtifact = false;
				}
			}
			if (isNewArtifact)
				resList.add(artifactTemplateInfo);
			List<ArtifactTemplateInfo> relatedArtifacts = artifactTemplateInfo.getRelatedArtifactsInfo();
			if (relatedArtifacts != null && !relatedArtifacts.isEmpty()) {
				getNewArtifactsInGroup(resList, relatedArtifacts);
			}
		}
	}

	private void createArtifactsGroupSet(List<ArtifactTemplateInfo> parsedGroupTemplateList, Set<String> parsedArtifactsName) {

		for (ArtifactTemplateInfo parsedGroupTemplate : parsedGroupTemplateList) {
			parsedArtifactsName.add(parsedGroupTemplate.getFileName());
			List<ArtifactTemplateInfo> relatedArtifacts = parsedGroupTemplate.getRelatedArtifactsInfo();
			if (relatedArtifacts != null && !relatedArtifacts.isEmpty()) {
				createArtifactsGroupSet(relatedArtifacts, parsedArtifactsName);
			}
		}
	}

}
