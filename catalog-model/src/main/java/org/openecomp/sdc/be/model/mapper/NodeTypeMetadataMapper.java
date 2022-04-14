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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.be.model.NodeTypeMetadata;
import org.openecomp.sdc.be.model.UploadResourceInfo;

/**
 * Responsible to map the {@code NodeTypeMetadata} to and from other classes.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NodeTypeMetadataMapper {

    public static UploadResourceInfo mapTo(final NodeTypeMetadata nodeTypeMetadata) {
        var uploadResourceInfo = new UploadResourceInfo();
        uploadResourceInfo.setName(nodeTypeMetadata.getName());
        uploadResourceInfo.setDescription(nodeTypeMetadata.getDescription());
        uploadResourceInfo.setContactId(nodeTypeMetadata.getContactId());
        uploadResourceInfo.setIcon(nodeTypeMetadata.getIcon());
        uploadResourceInfo.setResourceIconPath(nodeTypeMetadata.getResourceIconPath());
        uploadResourceInfo.setVendorName(nodeTypeMetadata.getVendorName());
        uploadResourceInfo.setModel(nodeTypeMetadata.getModel());
        uploadResourceInfo.setVendorRelease(nodeTypeMetadata.getVendorRelease());
        uploadResourceInfo.setResourceVendorModelNumber(nodeTypeMetadata.getResourceVendorModelNumber());
        uploadResourceInfo.setResourceType(nodeTypeMetadata.getResourceType());
        uploadResourceInfo.setTags(nodeTypeMetadata.getTags());
        uploadResourceInfo.setCategories(nodeTypeMetadata.getCategories());
        uploadResourceInfo.setNormative(nodeTypeMetadata.isNormative());
        return uploadResourceInfo;
    }

}
