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

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import org.openecomp.core.nosqldb.impl.cassandra.CassandraSessionFactory;
import org.openecomp.core.tools.store.zusammen.datatypes.HealingEntity;

import java.util.List;

public class HealingHandler {

    private static final CqlSession session = CassandraSessionFactory.getSession();

    private static final String SELECT_FLAG = "SELECT healing_needed FROM healing WHERE space = ? AND item_id = ? AND version_id = ?";
    private static final String INSERT = "INSERT INTO healing (space, item_id, version_id, healing_needed, old_version) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FLAG = "UPDATE healing SET healing_needed = ? WHERE space = ? AND item_id = ? AND version_id = ?";

    public void populateHealingTable(List<HealingEntity> healingEntities) {
        PreparedStatement selectPs = session.prepare(SELECT_FLAG);
        PreparedStatement insertPs = session.prepare(INSERT);
        PreparedStatement updatePs = session.prepare(UPDATE_FLAG);

        for (HealingEntity healingEntity : healingEntities) {
            if (isHealingRecordExist(healingEntity, selectPs)) {
                BoundStatement updateBound = updatePs.bind(
                        healingEntity.isHealingFlag(),
                        healingEntity.getSpace(),
                        healingEntity.getItemId(),
                        healingEntity.getVersionId()
                );
                session.execute(updateBound);
            } else {
                BoundStatement insertBound = insertPs.bind(
                        healingEntity.getSpace(),
                        healingEntity.getItemId(),
                        healingEntity.getVersionId(),
                        healingEntity.isHealingFlag(),
                        healingEntity.getOldVersion()
                );
                session.execute(insertBound);
            }
        }
    }

    private boolean isHealingRecordExist(HealingEntity healingEntity, PreparedStatement selectPs) {
        BoundStatement bound = selectPs.bind(
                healingEntity.getSpace(),
                healingEntity.getItemId(),
                healingEntity.getVersionId()
        );
        ResultSet rs = session.execute(bound);
        Row row = rs.one();
        return row != null && row.getBoolean("healing_needed"); // if flag exists
    }
}
