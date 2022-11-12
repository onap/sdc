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
package org.openecomp.sdc.be.resources.data;

import java.util.HashMap;
import java.util.Map;
import org.openecomp.sdc.be.dao.graph.datatype.GraphNode;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionaryExtractor;
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public abstract class ComponentMetadataData extends GraphNode {

    protected ComponentMetadataDataDefinition metadataDataDefinition;
    protected Integer componentInstanceCounter;

    public ComponentMetadataData(NodeTypeEnum label, ComponentMetadataDataDefinition metadataDataDefinition) {
        super(label);
        this.metadataDataDefinition = metadataDataDefinition;
        this.componentInstanceCounter = 0;
    }

    public ComponentMetadataData(NodeTypeEnum label, ComponentMetadataDataDefinition metadataDataDefinition,
                                 GraphPropertiesDictionaryExtractor extractor) {
        this(label, metadataDataDefinition);
        metadataDataDefinition.setUniqueId(extractor.getUniqueId());
        metadataDataDefinition.setCreationDate(extractor.getCreationDate());
        metadataDataDefinition.setDescription(extractor.getDescription());
        metadataDataDefinition.setConformanceLevel(extractor.getConformanceLevel());
        metadataDataDefinition.setIcon(extractor.getIcon());
        metadataDataDefinition.setHighestVersion(extractor.isHighestVersion());
        metadataDataDefinition.setLastUpdateDate(extractor.getLastUpdateDate());
        metadataDataDefinition.setName(extractor.getName());
        metadataDataDefinition.setState(extractor.getState());
        metadataDataDefinition.setTags(extractor.getTags());
        metadataDataDefinition.setVersion(extractor.getVersion());
        metadataDataDefinition.setContactId(extractor.getContactId());
        metadataDataDefinition.setUUID(extractor.getUUID());
        metadataDataDefinition.setNormalizedName(extractor.getNormalizedName());
        metadataDataDefinition.setSystemName(extractor.getSystemName());
        metadataDataDefinition.setIsDeleted(extractor.isDeleted());
        metadataDataDefinition.setProjectCode(extractor.getProjectCode());
        metadataDataDefinition.setCsarUUID(extractor.getCsarUuid());
        metadataDataDefinition.setCsarVersion(extractor.getCsarVersion());
        metadataDataDefinition.setImportedToscaChecksum(extractor.getImportedToscaChecksum());
        metadataDataDefinition.setInvariantUUID(extractor.getInvariantUuid());
        metadataDataDefinition.setModel(extractor.getModel());
        metadataDataDefinition.setTenant(extractor.getTenant());
        componentInstanceCounter = extractor.getInstanceCounter();
    }

    @Override
    public Map<String, Object> toGraphMap() {
        Map<String, Object> map = new HashMap<>();
        addIfExists(map, GraphPropertiesDictionary.UNIQUE_ID, metadataDataDefinition.getUniqueId());
        addIfExists(map, GraphPropertiesDictionary.VERSION, metadataDataDefinition.getVersion());
        addIfExists(map, GraphPropertiesDictionary.CREATION_DATE, metadataDataDefinition.getCreationDate());
        addIfExists(map, GraphPropertiesDictionary.DESCRIPTION, metadataDataDefinition.getDescription());
        addIfExists(map, GraphPropertiesDictionary.CONFORMANCE_LEVEL, metadataDataDefinition.getConformanceLevel());
        addIfExists(map, GraphPropertiesDictionary.ICON, metadataDataDefinition.getIcon());
        addIfExists(map, GraphPropertiesDictionary.IS_HIGHEST_VERSION, metadataDataDefinition.isHighestVersion());
        addIfExists(map, GraphPropertiesDictionary.LAST_UPDATE_DATE, metadataDataDefinition.getLastUpdateDate());
        addIfExists(map, GraphPropertiesDictionary.STATE, metadataDataDefinition.getState());
        addIfExists(map, GraphPropertiesDictionary.TAGS, metadataDataDefinition.getTags());
        addIfExists(map, GraphPropertiesDictionary.CONTACT_ID, metadataDataDefinition.getContactId());
        addIfExists(map, GraphPropertiesDictionary.NAME, metadataDataDefinition.getName());
        addIfExists(map, GraphPropertiesDictionary.UUID, metadataDataDefinition.getUUID());
        addIfExists(map, GraphPropertiesDictionary.NORMALIZED_NAME, metadataDataDefinition.getNormalizedName());
        addIfExists(map, GraphPropertiesDictionary.SYSTEM_NAME, metadataDataDefinition.getSystemName());
        addIfExists(map, GraphPropertiesDictionary.IS_DELETED, metadataDataDefinition.isDeleted());
        addIfExists(map, GraphPropertiesDictionary.INSTANCE_COUNTER, componentInstanceCounter);
        addIfExists(map, GraphPropertiesDictionary.PROJECT_CODE, metadataDataDefinition.getProjectCode());
        addIfExists(map, GraphPropertiesDictionary.CSAR_UUID, metadataDataDefinition.getCsarUUID());
        addIfExists(map, GraphPropertiesDictionary.CSAR_VERSION, metadataDataDefinition.getCsarVersion());
        addIfExists(map, GraphPropertiesDictionary.IMPORTED_TOSCA_CHECKSUM, metadataDataDefinition.getImportedToscaChecksum());
        addIfExists(map, GraphPropertiesDictionary.INVARIANT_UUID, metadataDataDefinition.getInvariantUUID());
        addIfExists(map, GraphPropertiesDictionary.MODEL, metadataDataDefinition.getModel());
        addIfExists(map, GraphPropertiesDictionary.TENANT, metadataDataDefinition.getTenant());
        return map;
    }

    @Override
    public String getUniqueId() {
        return metadataDataDefinition.getUniqueId();
    }

    public ComponentMetadataDataDefinition getMetadataDataDefinition() {
        return metadataDataDefinition;
    }

    public void setMetadataDataDefinition(ComponentMetadataDataDefinition metadataDataDefinition) {
        this.metadataDataDefinition = metadataDataDefinition;
    }

    public Integer getComponentInstanceCounter() {
        return componentInstanceCounter;
    }

    public void setComponentInstanceCounter(Integer componentInstanceCounter) {
        this.componentInstanceCounter = componentInstanceCounter;
    }

    public Integer increaseAndGetComponentInstanceCounter() {
        return ++componentInstanceCounter;
    }

    @Override
    public String toString() {
        return "ComponentMetadataData [metadataDataDefinition=" + metadataDataDefinition + ", componentInstanceCounter=" + componentInstanceCounter
            + "]";
    }
}
