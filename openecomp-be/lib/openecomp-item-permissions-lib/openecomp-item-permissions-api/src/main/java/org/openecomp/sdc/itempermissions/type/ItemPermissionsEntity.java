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
package org.openecomp.sdc.itempermissions.type;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/**
 * Created by ayalaben on 6/18/2017.
 */
@Getter
@Setter
@NoArgsConstructor
@CqlName("item_permissions")
@Entity
public class ItemPermissionsEntity {

    @PartitionKey
    @CqlName("item_id")
    private String itemId;

    @ClusteringColumn
    @CqlName("user_id")
    private String userId;

    @CqlName("permission")
    private String permission;

    public ItemPermissionsEntity(String itemId, String userId, String permission) {
        this.itemId = itemId;
        this.userId = userId;
        this.permission = permission;
    }
}
