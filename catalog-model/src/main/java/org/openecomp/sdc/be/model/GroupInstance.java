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
import org.openecomp.sdc.common.log.wrappers.Logger;

public class GroupInstance extends GroupInstanceDataDefinition {

    private static final Logger log = Logger.getLogger(GroupInstance.class);

    public GroupInstance() {
        super();
    }

    public GroupInstance(final GroupInstanceDataDefinition r) {
        super(r);
    }

    /**
     * Converts contained list of PropertyDataDefinitions to list of GroupInstanceProperties
     *
     * @return
     */
    public List<GroupInstanceProperty> convertToGroupInstancesProperties() {
        final List<PropertyDataDefinition> propertiesList = super.getProperties();
        if (CollectionUtils.isNotEmpty(propertiesList)) {
            return propertiesList.stream().map(GroupInstanceProperty::new).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * Converts received list of GroupInstanceProperties to the list of PropertyDataDefinitions and sets It into the GroupInstanceDataDefinition as
     * properties
     *
     * @param groupInstancesProperties
     */
    public void convertFromGroupInstancesProperties(final List<GroupInstanceProperty> groupInstancesProperties) {
        if (groupInstancesProperties != null && !groupInstancesProperties.isEmpty()) {
            super.setProperties(groupInstancesProperties.stream().map(PropertyDataDefinition::new).collect(Collectors.toList()));
        }
    }

    private void removeArtifactsDuplicates() {
        final List<String> artifacts = getArtifacts();
        final Set<String> artifactsSet = new HashSet<>();
        if (artifacts != null && !artifacts.isEmpty()) {
            artifactsSet.addAll(artifacts);
            artifacts.clear();
            artifacts.addAll(artifactsSet);
        }
        final List<String> giArtifacts = getGroupInstanceArtifacts();
        final Set<String> giArtifactsSet = new HashSet<>();
        if (giArtifacts != null && !giArtifacts.isEmpty()) {
            giArtifactsSet.addAll(giArtifacts);
            giArtifacts.clear();
            giArtifacts.addAll(giArtifactsSet);
        }
    }

    private void clearArtifactsUuid() {
        final List<String> artifactsUuid = getArtifactsUuid();
        if (CollectionUtils.isNotEmpty(artifactsUuid)) {
            artifactsUuid.clear();
        } else if (artifactsUuid == null) {
            setArtifactsUuid(new ArrayList<>());
        }
        final List<String> groupInstanceArtifactsUuid = this.getGroupInstanceArtifactsUuid();
        if (CollectionUtils.isNotEmpty(groupInstanceArtifactsUuid)) {
            groupInstanceArtifactsUuid.clear();
        } else if (groupInstanceArtifactsUuid == null) {
            setGroupInstanceArtifactsUuid(new ArrayList<>());
        }
    }

    /**
     * Aligns the list of artifacts UUIDs of group instance according to received deployment artifacts
     *
     * @param deploymentArtifacts
     */
    public void alignArtifactsUuid(final Map<String, ArtifactDefinition> deploymentArtifacts) {
        final List<String> artifactIds = getArtifacts();
        log.debug("the artifacts Id's are: {}, and the deployment artifacts Id's are: {}", artifactIds, deploymentArtifacts);
        if (CollectionUtils.isNotEmpty(artifactIds) && deploymentArtifacts != null) {
            removeArtifactsDuplicates();
            clearArtifactsUuid();
            final List<String> artifactUuids = getArtifactsUuid();
            final List<String> giArtifactUuids = getGroupInstanceArtifactsUuid();
            for (final String artifactId : artifactIds) {
                final var label = artifactId.substring(artifactId.lastIndexOf('.') + 1);
                final ArtifactDefinition artifact = deploymentArtifacts.get(label);
                log.debug("current artifact id: {}, current artifact definition: {}", artifactId, artifact);
                final var artifactType = ArtifactTypeEnum.parse(artifact.getArtifactType());
                if (artifactType != ArtifactTypeEnum.HEAT_ENV) {
                    addArtifactsIdToCollection(artifactUuids, artifact);
                } else {
                    addArtifactsIdToCollection(giArtifactUuids, artifact);
                }
            }
        }
    }

    private void addArtifactsIdToCollection(final List<String> artifactUuids, final ArtifactDefinition artifact) {
        if (StringUtils.isNotEmpty(artifact.getArtifactUUID()) && !artifactUuids.contains(artifact.getArtifactUUID())) {
            artifactUuids.add(artifact.getArtifactUUID());
        }
    }
}
