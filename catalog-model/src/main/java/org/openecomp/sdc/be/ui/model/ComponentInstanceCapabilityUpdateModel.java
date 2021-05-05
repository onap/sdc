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

package org.openecomp.sdc.be.ui.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

/**
 * Model for the component instance capability update request
 */
@Data
public class ComponentInstanceCapabilityUpdateModel {

    @NotNull
    @Size(min=1)
    private String type;
    @NotNull
    @Size(min=1)
    private String name;
    @NotNull
    @Size(min=1)
    private String ownerId;
    @NotNull
    @Size(min=1)
    private String ownerName;
    @NotNull
    @Size(min=1)
    private String uniqueId;
    @NotNull
    private Boolean external;

}
