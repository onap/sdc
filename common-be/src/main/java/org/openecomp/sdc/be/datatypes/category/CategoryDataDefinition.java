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
package org.openecomp.sdc.be.datatypes.category;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@NoArgsConstructor
public class CategoryDataDefinition extends ToscaDataDefinition {

    private String name;
    private String displayName;
    private String normalizedName;
    private List<String> models;
    private String uniqueId;
    private List<String> icons;
    private boolean useServiceSubstitutionForNestedServices = false;
    private List<MetadataKeyDataDefinition> metadataKeys;

    public CategoryDataDefinition(CategoryDataDefinition c) {
        this.name = c.name;
        this.displayName = c.displayName;
        this.normalizedName = c.normalizedName;
        this.models = c.models;
        this.uniqueId = c.uniqueId;
        this.icons = c.icons;
        this.useServiceSubstitutionForNestedServices = c.useServiceSubstitutionForNestedServices;
        this.metadataKeys = c.metadataKeys;
    }
}
