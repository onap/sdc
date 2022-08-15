/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
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
package org.openecomp.sdc.be.model;

public class DefaultUploadResourceInfo extends UploadResourceInfo{

    public DefaultUploadResourceInfo(NullNodeTypeMetadata nullNodeTypeMetadata){
        super.setName(nullNodeTypeMetadata.getName());
        super.setDescription(nullNodeTypeMetadata.getDescription());
        super.setContactId(nullNodeTypeMetadata.getContactId());
        super.setIcon(nullNodeTypeMetadata.getIcon());
        super.setResourceIconPath(nullNodeTypeMetadata.getResourceIconPath());
        super.setVendorName(nullNodeTypeMetadata.getVendorName());
        super.setModel(nullNodeTypeMetadata.getModel());
        super.setVendorRelease(nullNodeTypeMetadata.getVendorRelease());
        super.setResourceVendorModelNumber(nullNodeTypeMetadata.getResourceVendorModelNumber());
        super.setResourceType(nullNodeTypeMetadata.getResourceType());
        super.setTags(nullNodeTypeMetadata.getTags());
        super.setCategories(nullNodeTypeMetadata.getCategories());
        super.setNormative(nullNodeTypeMetadata.isNormative());
    }
}
