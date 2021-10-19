/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.datatypes.elements;

import java.util.List;

import org.onap.sdc.tosca.datatypes.model.PropertyDefinition;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ArtifactTypeDataDefinition extends ToscaDataDefinition {

    private String name;
    private String uniqueId;
    private String derivedFrom;
    private String description;
    private String model;
    private Long creationDate;
    private Long lastUpdated;
    private List<PropertyDefinition> propertiesData;

    public ArtifactTypeDataDefinition(ArtifactTypeDataDefinition p) {
        this.name = p.name;
        this.uniqueId = p.uniqueId;
        this.derivedFrom = p.derivedFrom;
        this.description = p.description;
        this.creationDate = p.creationDate;
        this.lastUpdated = p.lastUpdated;
        this.model = p.model;
    }
}
