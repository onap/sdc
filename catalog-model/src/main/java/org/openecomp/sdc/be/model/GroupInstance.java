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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.datatypes.elements.GroupInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class GroupInstance extends GroupInstanceDataDefinition {

    public GroupInstance() {
        super();
    }

    public GroupInstance(GroupInstanceDataDefinition r) {
        super(r);
    }

    private static final Logger log = Logger.getLogger(GroupInstance.class);
    /**
     * Converts contained list of PropertyDataDefinitions to list of GroupInstanceProperties
     * @return
     */
    public List<GroupInstanceProperty>  convertToGroupInstancesProperties() {
        List<GroupInstanceProperty> groupInstancesProperties = null;
        List<PropertyDataDefinition> propertiesList = super.getProperties();
        if(propertiesList != null && !propertiesList .isEmpty()){
            groupInstancesProperties = propertiesList.stream().map(GroupInstanceProperty::new).collect(Collectors.toList());
        }
        return groupInstancesProperties;
    }
    /**
     * Converts received list of GroupInstanceProperties to the list of PropertyDataDefinitions and sets It into the GroupInstanceDataDefinition as properties
     * @param groupInstancesProperties
     */
    public void convertFromGroupInstancesProperties(List<GroupInstanceProperty> groupInstancesProperties) {
        if(groupInstancesProperties != null && !groupInstancesProperties .isEmpty()){
            List<PropertyDataDefinition> propList = groupInstancesProperties.stream().map(PropertyDataDefinition::new).collect(Collectors.toList());
            super.setProperties(propList);
        }
    }

    private void removeArtifactsDuplicates() {
        List<String> artifacts = getArtifacts();
        Set<String> artifactsSet = new HashSet<>();
		
		if (artifacts != null && !artifacts.isEmpty()) {
			artifactsSet.addAll(artifacts);
			artifacts.clear();
			artifacts.addAll(artifactsSet);
		}

        List<String> giArtifacts = getGroupInstanceArtifacts();
        Set<String> giArtifactsSet = new HashSet<>();
		
		if (giArtifacts != null && !giArtifacts.isEmpty()) {
			giArtifactsSet.addAll(giArtifacts);
			giArtifacts.clear();
			giArtifacts.addAll(giArtifactsSet);
		}
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
        log.debug("the artifacts Id's are: {}, and the deployment artifacts Id's are: {}", artifactIds, deploymentArtifacts);
        if(CollectionUtils.isNotEmpty(artifactIds) && deploymentArtifacts != null){
            removeArtifactsDuplicates();
            clearArtifactsUuid();
            List<String> artifactUuids = getArtifactsUuid();
            List<String> giArtifactUuids = getGroupInstanceArtifactsUuid();
            for(String artifactId : artifactIds){
                String label = artifactId.substring(artifactId.lastIndexOf('.') + 1);
                ArtifactDefinition artifact = deploymentArtifacts.get(label);
                log.debug("current artifact id: {}, current artifact definition: {}", artifactId, artifact);
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
