/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.model.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.model.NodeTypeMetadata;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.category.CategoryDefinition;

class NodeTypeMetadataMapperTest {

    @Test
    void mapToUploadResourceInfoTest() {
        final var nodeTypeMetadata = new NodeTypeMetadata();
        nodeTypeMetadata.setName("name");
        nodeTypeMetadata.setDescription("description");
        nodeTypeMetadata.setContactId("contactId");
        nodeTypeMetadata.setIcon("icon");
        nodeTypeMetadata.setResourceIconPath("resourceIconPath");
        nodeTypeMetadata.setVendorName("vendorName");
        nodeTypeMetadata.setModel("model");
        nodeTypeMetadata.setVendorRelease("vendorRelease");
        nodeTypeMetadata.setResourceVendorModelNumber("resourceVendorModelNumber");
        nodeTypeMetadata.setResourceType("resourceType");
        nodeTypeMetadata.setTags(List.of("tag1", "tag2"));
        nodeTypeMetadata.setCategories(List.of(new CategoryDefinition()));

        final UploadResourceInfo uploadResourceInfo = NodeTypeMetadataMapper.mapTo(nodeTypeMetadata);
        assertEquals(nodeTypeMetadata.getName(), uploadResourceInfo.getName());
        assertEquals(nodeTypeMetadata.getDescription(), uploadResourceInfo.getDescription());
        assertEquals(nodeTypeMetadata.getContactId(), uploadResourceInfo.getContactId());
        assertEquals(nodeTypeMetadata.getResourceIconPath(), uploadResourceInfo.getResourceIconPath());
        assertEquals(nodeTypeMetadata.getVendorName(), uploadResourceInfo.getVendorName());
        assertEquals(nodeTypeMetadata.getModel(), uploadResourceInfo.getModel());
        assertEquals(nodeTypeMetadata.getVendorRelease(), uploadResourceInfo.getVendorRelease());
        assertEquals(nodeTypeMetadata.getResourceVendorModelNumber(), uploadResourceInfo.getResourceVendorModelNumber());
        assertEquals(nodeTypeMetadata.getResourceType(), uploadResourceInfo.getResourceType());
        assertEquals(nodeTypeMetadata.getTags(), uploadResourceInfo.getTags());
        assertEquals(nodeTypeMetadata.getCategories(), uploadResourceInfo.getCategories());

        final var nodeTypeMetadata1 = new NodeTypeMetadata();
        nodeTypeMetadata1.setIcon("icon");
        final UploadResourceInfo uploadResourceInfo1 = NodeTypeMetadataMapper.mapTo(nodeTypeMetadata1);
        assertEquals(nodeTypeMetadata1.getIcon(), uploadResourceInfo1.getResourceIconPath());
    }

}