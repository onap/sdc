/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
package org.openecomp.sdc.be.datatypes.elements;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MapInterfaceDataDefinition extends MapDataDefinition<InterfaceDataDefinition> {

    @JsonCreator
    public MapInterfaceDataDefinition(Map<String, InterfaceDataDefinition> mapToscaDataDefinition) {
        super(mapToscaDataDefinition);
    }

    public void setMapToscaDataDefinition(
            Map<String, InterfaceDataDefinition> mapToscaDataDefinition
    ) {
        this.mapToscaDataDefinition = mapToscaDataDefinition;
    }

    @JsonValue
    @Override
    public Map<String, InterfaceDataDefinition> getMapToscaDataDefinition() {
        return mapToscaDataDefinition;
    }
}
