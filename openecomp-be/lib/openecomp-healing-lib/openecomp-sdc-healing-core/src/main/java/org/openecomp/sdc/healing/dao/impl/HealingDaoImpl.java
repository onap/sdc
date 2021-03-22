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
package org.openecomp.sdc.healing.dao.impl;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import java.util.Optional;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.healing.dao.HealingDao;

/**
 * Created by ayalaben on 10/17/2017
 */
public class HealingDaoImpl implements HealingDao {

    private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
    private static HealingAccessor accessor = noSqlDb.getMappingManager().createAccessor(HealingAccessor.class);

    @Override
    public Optional<Boolean> getItemHealingFlag(String space, String itemId, String versionId) {
        ResultSet result = accessor.getItemHealingFlag(space, itemId, versionId);
        return result.getAvailableWithoutFetching() < 1 ? Optional.empty() : Optional.of(result.one().getBool("healing_needed"));
    }

    @Override
    public void setItemHealingFlag(boolean healingNeededFlag, String space, String itemId, String versionId) {
        accessor.setItemHealingFlag(healingNeededFlag, space, itemId, versionId);
    }

    @Accessor
    interface HealingAccessor {

        @Query("SELECT healing_needed FROM healing WHERE space=? AND item_id=? AND version_id=?")
        ResultSet getItemHealingFlag(String space, String itemId, String versionId);

        @Query("UPDATE healing SET healing_needed=? WHERE space=? AND item_id=? AND version_id=?")
        void setItemHealingFlag(boolean flag, String space, String itemId, String versionId);
    }
}
