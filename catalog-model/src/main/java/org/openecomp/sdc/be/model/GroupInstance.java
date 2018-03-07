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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.datatypes.elements.GroupInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;

public class GroupInstance extends GroupInstanceDataDefinition implements Serializable {

	private static final long serialVersionUID = -2066335818115254401L;
	
	public GroupInstance() {
		super();
	}
	
	public GroupInstance(GroupInstanceDataDefinition r) {
		super(r);
	}
	/**
	 * Converts contained list of PropertyDataDefinitions to list of GroupInstanceProperties
	 * @return
	 */
	public List<GroupInstanceProperty>  convertToGroupInstancesProperties() {
		List<GroupInstanceProperty> groupInstancesProperties = null;
		List<PropertyDataDefinition> propertiesList = super.getProperties();
		if(propertiesList != null && !propertiesList .isEmpty()){
			groupInstancesProperties = propertiesList.stream().map(p -> new GroupInstanceProperty(p)).collect(Collectors.toList());
		}
		return groupInstancesProperties;
	}
	/**
	 * Converts received list of GroupInstanceProperties to the list of PropertyDataDefinitions and sets It into the GroupInstanceDataDefinition as properties
	 * @param groupInstancesProperties
	 */
	public void convertFromGroupInstancesProperties(List<GroupInstanceProperty> groupInstancesProperties) {
		if(groupInstancesProperties != null && !groupInstancesProperties .isEmpty()){
			List<PropertyDataDefinition> propList = groupInstancesProperties.stream().map(p -> new PropertyDataDefinition(p)).collect(Collectors.toList());
			super.setProperties(propList);
		}
	}
	
	private void removeArtifactsDuplicates() {
		List<String> artifacts = getArtifacts();
		Set<String> artifactsSet = new HashSet<>();
		artifactsSet.addAll(artifacts);
		artifacts.clear();
		artifacts.addAll(artifactsSet);
		
		List<String> giArtifacts = getGroupInstanceArtifacts();
		Set<String> giArtifactsSet = new HashSet<>();
		giArtifactsSet.addAll(giArtifacts);
		giArtifacts.clear();
		giArtifacts.addAll(giArtifactsSet);
	}

	private void clearArtifactsUuid() {
		List<String> artifactsUuid = getArtifactsUuid();
		if(CollectionUtils.isNotEmpty(artifactsUuid)){
			artifactsUuid.clear();
		} else if (artifactsUuid == null){
			setArtifactsUuid(new ArrayList<>());
		}
		
		List<String> giartifactsUuid = this.getGroupInstanceArtifactsUuid();
		if(CollectionUtils.isNotEmpty(giartifactsUuid)){
			giartifactsUuid.clear();
		} else if (giartifactsUuid == null){
			setGroupInstanceArtifactsUuid(new ArrayList<>());
		}
	}
	
	/**
	 * Aligns the list of artifacts UUIDs of group instance according to received deployment artifacts
	 * @param deploymentArtifacts
	 */
	public void alignArtifactsUuid(Map<String, ArtifactDefinition> deploymentArtifacts) {
		List<String> artifactIds = getArtifacts();
		if(CollectionUtils.isNotEmpty(artifactIds)){
			removeArtifactsDuplicates();
			clearArtifactsUuid();
			List<String> artifactUuids = getArtifactsUuid();
			List<String> giArtifactUuids = getGroupInstanceArtifactsUuid();
			for(String artifactId : artifactIds){
				String label = artifactId.substring(artifactId.lastIndexOf('.') + 1);
				ArtifactDefinition artifact = deploymentArtifacts.get(label);
				ArtifactTypeEnum artifactType = ArtifactTypeEnum.findType(artifact.getArtifactType());
				if (artifactType != ArtifactTypeEnum.HEAT_ENV){
					addArtifactsIdToCollection(artifactUuids, artifact);
				}else{
					addArtifactsIdToCollection(giArtifactUuids, artifact);
				}
			}
			
		}
	}

	private void addArtifactsIdToCollection(List<String> artifactUuids, ArtifactDefinition artifact) {
		if(!artifactUuids.contains(artifact.getArtifactUUID()) && StringUtils.isNotEmpty(artifact.getArtifactUUID())){
			artifactUuids.add(artifact.getArtifactUUID());
		
		}
	}

}
