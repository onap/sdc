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

import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.ui.model.ComponentInstanceCapabilityUpdateModel;
import org.springframework.stereotype.Service;

@Service
public class CapabilityMapper {

    public CapabilityDefinition mapToCapabilityDefinition(final ComponentInstanceCapabilityUpdateModel capabilityUpdateModel) {
        var capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setUniqueId(capabilityUpdateModel.getUniqueId());
        capabilityDefinition.setType(capabilityUpdateModel.getType());
        capabilityDefinition.setOwnerId(capabilityUpdateModel.getOwnerId());
        capabilityDefinition.setOwnerName(capabilityUpdateModel.getOwnerName());
        capabilityDefinition.setName(capabilityUpdateModel.getName());
        capabilityDefinition.setExternal(capabilityUpdateModel.getExternal());
        return capabilityDefinition;
    }

}
