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

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import java.util.Optional;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.sdc.healing.dao.HealingDao;

/**
 * Created by ayalaben on 10/17/2017
 */
public class HealingDaoImpl implements HealingDao {

    private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
    private static final CqlSession session = noSqlDb.getSession();

      private static final PreparedStatement selectStmt = session.prepare(
        "SELECT healing_needed FROM healing WHERE space=? AND item_id=? AND version_id=?"
    );

    private static final PreparedStatement updateStmt = session.prepare(
        "UPDATE healing SET healing_needed=? WHERE space=? AND item_id=? AND version_id=?"
    );

    @Override
    public Optional<Boolean> getItemHealingFlag(String space, String itemId, String versionId) {
        BoundStatement bound = selectStmt.bind(space, itemId, versionId);
        ResultSet result = session.execute(bound);
        Row row = result.one();
        return row != null ? Optional.of(row.getBoolean("healing_needed")) : Optional.empty();
    }


    @Override
    public void setItemHealingFlag(boolean healingNeededFlag, String space, String itemId, String versionId) {
        BoundStatement bound = updateStmt.bind(healingNeededFlag, space, itemId, versionId);
        session.execute(bound);
    }
}
