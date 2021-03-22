/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdcrests.itempermissions.types;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

/**
 * Created by ayalaben on 6/20/2017.
 */
@Schema(description = "ItemPermissionsRequest")
public class ItemPermissionsRequestDto {

    private Set<String> addedUsersIds;
    private Set<String> removedUsersIds;

    public Set<String> getAddedUsersIds() {
        return addedUsersIds;
    }

    public void setAddedUsersIds(Set<String> addedUsersIds) {
        this.addedUsersIds = addedUsersIds;
    }

    public Set<String> getRemovedUsersIds() {
        return removedUsersIds;
    }

    public void setRemovedUsersIds(Set<String> removedUsersIds) {
        this.removedUsersIds = removedUsersIds;
    }
}
