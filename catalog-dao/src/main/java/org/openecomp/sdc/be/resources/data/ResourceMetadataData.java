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

import java.util.Map;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionary;
import org.openecomp.sdc.be.dao.neo4j.GraphPropertiesDictionaryExtractor;
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

public class ResourceMetadataData extends ComponentMetadataData {

    public ResourceMetadataData() {
        super(NodeTypeEnum.Resource, new ResourceMetadataDataDefinition());
    }

    public ResourceMetadataData(ComponentMetadataDataDefinition metadataDataDefinition) {
        super(NodeTypeEnum.Resource, metadataDataDefinition);
    }

    public ResourceMetadataData(final GraphPropertiesDictionaryExtractor extractor) {
        super(NodeTypeEnum.Resource, new ResourceMetadataDataDefinition(), extractor);
        final var resourceMetadataDataDefinition = (ResourceMetadataDataDefinition) metadataDataDefinition;
        resourceMetadataDataDefinition.setVendorName(extractor.getVendorName());
        resourceMetadataDataDefinition.setVendorRelease(extractor.getVendorRelease());
        resourceMetadataDataDefinition.setResourceType(extractor.getResourceType());
        resourceMetadataDataDefinition.setAbstract(extractor.isAbstract());
        resourceMetadataDataDefinition.setCost(extractor.getCost());
        resourceMetadataDataDefinition.setLicenseType(extractor.getLicenseType());
        resourceMetadataDataDefinition.setToscaResourceName(extractor.getToscaResourceName());
        resourceMetadataDataDefinition.setCsarVersionId(extractor.getCsarVersionId());
        resourceMetadataDataDefinition.setTenant(extractor.getTenant());
    }

    @Override
    public Map<String, Object> toGraphMap() {
        final Map<String, Object> graphMap = super.toGraphMap();
        final ResourceMetadataDataDefinition resourceMetadataDataDefinition = (ResourceMetadataDataDefinition) metadataDataDefinition;
        addIfExists(graphMap, GraphPropertiesDictionary.VENDOR_NAME, resourceMetadataDataDefinition.getVendorName());
        addIfExists(graphMap, GraphPropertiesDictionary.VENDOR_RELEASE, resourceMetadataDataDefinition.getVendorRelease());
        addIfExists(graphMap, GraphPropertiesDictionary.RESOURCE_TYPE, resourceMetadataDataDefinition.getResourceType().name());
        addIfExists(graphMap, GraphPropertiesDictionary.IS_ABSTRACT, resourceMetadataDataDefinition.isAbstract());
        addIfExists(graphMap, GraphPropertiesDictionary.COST, resourceMetadataDataDefinition.getCost());
        addIfExists(graphMap, GraphPropertiesDictionary.LICENSE_TYPE, resourceMetadataDataDefinition.getLicenseType());
        addIfExists(graphMap, GraphPropertiesDictionary.TOSCA_RESOURCE_NAME, resourceMetadataDataDefinition.getToscaResourceName());
        addIfExists(graphMap, GraphPropertiesDictionary.CSAR_VERSION_ID, resourceMetadataDataDefinition.getCsarVersionId());
        addIfExists(graphMap, GraphPropertiesDictionary.TENANT, resourceMetadataDataDefinition.getTenant());
        return graphMap;
    }
}
