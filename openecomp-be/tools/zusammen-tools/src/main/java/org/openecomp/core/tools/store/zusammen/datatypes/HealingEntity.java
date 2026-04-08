/*
 * Copyright © 2016-2017 European Support Limited
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
package org.openecomp.core.tools.store.zusammen.datatypes;


import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by ayalaben on 10/15/2017
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@CqlName("healing")
public class HealingEntity {

    @CqlName("space")
    @PartitionKey(0)
    private String space;
    @CqlName("item_id")
    @PartitionKey(1)
    private String itemId;
    @CqlName("version_id")
    @PartitionKey(2)
    private String versionId;
    @CqlName("healing_needed")
    private boolean healingFlag;
    @CqlName("old_version")
    private String oldVersion;
}
