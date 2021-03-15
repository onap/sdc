/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.core.tools.store;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.core.tools.store.zusammen.datatypes.HealingEntity;

import java.util.List;

/**
 * Created by ayalaben on 10/15/2017
 */
public class HealingHandler {

  private static NoSqlDb nnoSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static HealingAccessor accessor =
      nnoSqlDb.getMappingManager().createAccessor(HealingAccessor.class);


  public void populateHealingTable(List<HealingEntity> healingEntities) {
    healingEntities.forEach(healingEntity -> {
      if (isHealingRecordExist(healingEntity)) {
        accessor.updateFlag(healingEntity.isHealingFlag(), healingEntity.getSpace(),
            healingEntity.getItemId(), healingEntity.getVersionId());
      } else {
        accessor.create(healingEntity.getSpace(), healingEntity.getItemId(),
            healingEntity.getVersionId(), healingEntity.isHealingFlag(),
            healingEntity.getOldVersion());
      }
    });
  }

  private boolean isHealingRecordExist(HealingEntity healingEntity) {
    return accessor.getFlag(healingEntity.getSpace(), healingEntity.getItemId(),
        healingEntity.getVersionId()).getAvailableWithoutFetching() == 1;
  }

  @Accessor
  interface HealingAccessor {

    @Query("SELECT healing_needed FROM healing WHERE space=? AND item_id=? AND version_id=?")
    ResultSet getFlag(String space, String itemId, String versionId);

    @Query("Insert into healing (space, item_id, version_id, healing_needed, old_version) " +
        "values (?,?,?,?,?)")
    void create(String space, String itemId, String versionId, boolean flag, String oldVersion);

    @Query("UPDATE healing SET healing_needed=? WHERE space=? AND item_id=? AND version_id=?")
    void updateFlag(boolean flag, String space, String itemId, String versionId);
  }

}
