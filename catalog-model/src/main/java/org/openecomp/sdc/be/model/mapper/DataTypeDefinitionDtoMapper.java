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

package org.openecomp.sdc.be.model.mapper;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.be.datatypes.elements.DataTypeDataDefinition;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.dto.DataTypeDefinitionDto;
import org.openecomp.sdc.be.model.dto.PropertyDefinitionDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataTypeDefinitionDtoMapper {

    public static DataTypeDefinition mapTo(final DataTypeDefinitionDto dataTypeDefinitionDto) {
        final var dataTypeDefinition = new DataTypeDefinition();
        dataTypeDefinition.setUniqueId(dataTypeDefinitionDto.getUniqueId());
        dataTypeDefinition.setModel(dataTypeDefinitionDto.getModel().getName());
        dataTypeDefinition.setDerivedFromName(dataTypeDefinitionDto.getDerivedFromName());
        dataTypeDefinition.setName(dataTypeDefinitionDto.getName());
        //dataTypeDefinition.setCreationTime(dataTypeDefinitionDto.getCreationTime());
        //dataTypeDefinitionDto.setModificationTime(dataTypeDefinitionDto.getModificationTime());

        if (CollectionUtils.isNotEmpty(dataTypeDefinitionDto.getProperties())) {
            final List<PropertyDefinition> properties = new ArrayList<>();
            dataTypeDefinitionDto.getProperties().stream().forEach(
                property -> properties.add(PropertyDefinitionDtoMapper.mapTo(property))
            );
            dataTypeDefinition.setProperties(properties);

        }
        dataTypeDefinition.setDescription(dataTypeDefinitionDto.getDescription());
        return dataTypeDefinition;
    }

    public static DataTypeDefinitionDto mapFrom(final DataTypeDataDefinition dataTypeDataDefinition) {
        final var dataTypeDefinition = new DataTypeDefinition(dataTypeDataDefinition);
        final var dataTypeDefinitionDto = new DataTypeDefinitionDto();
        dataTypeDefinitionDto.setUniqueId(dataTypeDefinition.getUniqueId());
        dataTypeDefinitionDto.setName(dataTypeDefinition.getName());
        dataTypeDefinitionDto.setDerivedFromName(dataTypeDefinition.getDerivedFromName());
        dataTypeDefinitionDto.setDescription(dataTypeDefinition.getDescription());
        //dataTypeDefinitionDto.setCreationTime(dataTypeDefinition.getCreationTime());
        //dataTypeDefinitionDto.setModificationTime(dataTypeDefinition.getModificationTime());
        //dataTypeDefinitionDto.setModel(new ModelData(dataTypeDefinition.getModel()));
        if (CollectionUtils.isNotEmpty(dataTypeDefinition.getProperties())) {
            final List<PropertyDefinitionDto> properties = new ArrayList<>();
            dataTypeDefinition.getProperties().stream().forEach(
                property -> properties.add(PropertyDefinitionDtoMapper.mapFrom(property))
            );
            dataTypeDefinitionDto.setProperties(properties);
        }
        return dataTypeDefinitionDto;
    }
}
