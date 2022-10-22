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
package org.openecomp.sdc.be.datatypes.elements;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class DataTypeDataDefinition extends ToscaDataDefinition {

    private String name;
    private String uniqueId;
    // "boolean", "string", "float", "integer", "version" })
    private String derivedFromName;
    private String description;
    private String model;

    /**
     * Timestamp of data type creation
     */
    private Long creationTime;
    /**
     * Timestamp of the data type last update
     */
    private Long modificationTime;
    private List<PropertyDataDefinition> propertiesData;

    public DataTypeDataDefinition(DataTypeDataDefinition p) {
        this.name = p.name;
        this.uniqueId = p.uniqueId;
        this.derivedFromName = p.derivedFromName;
        this.description = p.description;
        this.creationTime = p.creationTime;
        this.modificationTime = p.modificationTime;
        this.model = p.model;
    }
}
