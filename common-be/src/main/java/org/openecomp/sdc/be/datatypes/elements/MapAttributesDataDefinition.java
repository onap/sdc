/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020, Nordix Foundation. All rights reserved.
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


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class MapAttributesDataDefinition extends MapDataDefinition<AttributeDataDefinition> {

    private String parentName;

    public MapAttributesDataDefinition(MapDataDefinition cdt, String parentName) {
        super(cdt);
        this.parentName = parentName;
    }

    @JsonCreator
    public MapAttributesDataDefinition(Map<String, AttributeDataDefinition> mapToscaDataDefinition) {
        super(mapToscaDataDefinition);
    }

    /**
     * Copy Constructor
     */
    public MapAttributesDataDefinition(MapAttributesDataDefinition toBeDeepCopiedMapPropertiesDataDefinition) {
        this.parentName = toBeDeepCopiedMapPropertiesDataDefinition.parentName;
        this.toscaPresentation = toBeDeepCopiedMapPropertiesDataDefinition.toscaPresentation == null ? null : new HashMap(toBeDeepCopiedMapPropertiesDataDefinition.toscaPresentation);
        this.mapToscaDataDefinition = toBeDeepCopiedMapPropertiesDataDefinition.mapToscaDataDefinition == null ? null : new HashMap(toBeDeepCopiedMapPropertiesDataDefinition.mapToscaDataDefinition);
    }

    public MapAttributesDataDefinition() {
        super();

    }

    @JsonValue
    @Override
    public Map<String, AttributeDataDefinition> getMapToscaDataDefinition() {
        return mapToscaDataDefinition;
    }


    public void setMapToscaDataDefinition(Map<String, AttributeDataDefinition> mapToscaDataDefinition) {
        this.mapToscaDataDefinition = mapToscaDataDefinition;
    }
}
