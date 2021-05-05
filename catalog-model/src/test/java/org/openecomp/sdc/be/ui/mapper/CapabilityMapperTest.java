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

package org.openecomp.sdc.be.ui.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.ui.model.ComponentInstanceCapabilityUpdateModel;

class CapabilityMapperTest {

    private CapabilityMapper capabilityMapper;

    @BeforeEach
    void beforeEach() {
        capabilityMapper = new CapabilityMapper();
    }

    @Test
    void mapToCapabilityDefinitionTest() {
        final ComponentInstanceCapabilityUpdateModel capabilityUpdateModel = new ComponentInstanceCapabilityUpdateModel();
        capabilityUpdateModel.setUniqueId("uniqueId");
        capabilityUpdateModel.setName("name");
        capabilityUpdateModel.setExternal(true);
        capabilityUpdateModel.setOwnerId("ownerId");
        capabilityUpdateModel.setType("type");
        capabilityUpdateModel.setOwnerName("ownerName");
        final CapabilityDefinition capabilityDefinition = capabilityMapper.mapToCapabilityDefinition(capabilityUpdateModel);
        assertCapabilityDefinition(capabilityDefinition, capabilityUpdateModel);
    }

    private void assertCapabilityDefinition(final CapabilityDefinition actualCapabilityDefinition,
                                            final ComponentInstanceCapabilityUpdateModel expectedCapabilityDefinition) {
        assertEquals(expectedCapabilityDefinition.getUniqueId(), actualCapabilityDefinition.getUniqueId());
        assertEquals(expectedCapabilityDefinition.getName(), actualCapabilityDefinition.getName());
        assertEquals(expectedCapabilityDefinition.getOwnerId(), actualCapabilityDefinition.getOwnerId());
        assertEquals(expectedCapabilityDefinition.getOwnerName(), actualCapabilityDefinition.getOwnerName());
        assertEquals(expectedCapabilityDefinition.getType(), actualCapabilityDefinition.getType());
        assertEquals(expectedCapabilityDefinition.getExternal(), actualCapabilityDefinition.isExternal());
    }
}