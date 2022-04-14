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

package org.openecomp.sdc.be.model;

import java.util.List;
import lombok.Data;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.category.CategoryDefinition;

/**
 * Holds metadata information about a TOSCA node_type.
 */
@Data
public class NodeTypeMetadata {

    private String name;
    private String toscaName;
    private String description;
    private String contactId;
    private String resourceIconPath;
    private String model;
    private String icon;
    private String vendorName;
    private String vendorRelease;
    private String resourceVendorModelNumber;
    private String resourceType = ResourceTypeEnum.VFC.getValue();
    private boolean isNormative = true;
    private List<String> tags;
    private List<CategoryDefinition> categories;

}
