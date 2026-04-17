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
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.ResultSet;

import com.google.common.collect.Sets;
import org.openecomp.core.nosqldb.impl.cassandra.CassandraSessionFactory;
import org.openecomp.core.tools.store.zusammen.datatypes.ElementEntity;
import org.openecomp.core.tools.store.zusammen.datatypes.VersionEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class VersionCassandraLoader {

    private static final CqlSession session = CassandraSessionFactory.getSession();

    public void insertElementToVersion(ElementEntity elementEntity) {
        String cql = "UPDATE zusammen_dox.version_elements SET element_ids = element_ids + ? WHERE space=? AND item_id=? AND version_id=?";
        BoundStatement stmt = session.prepare(cql)
                .bind(Sets.newHashSet(elementEntity.getElementId()), elementEntity.getSpace(), elementEntity.getItemId(), elementEntity.getVersionId());
        session.execute(stmt);
    }

    public void insertVersion(VersionEntity versionEntity) {
        String cql = "INSERT INTO zusammen_dox.version " +
                "(space, item_id, version_id, base_version_id, creation_time, info, modification_time, relations) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        BoundStatement stmt = session.prepare(cql)
                .bind(
                        versionEntity.getSpace(),
                        versionEntity.getItemId(),
                        versionEntity.getVersionId(),
                        versionEntity.getBaseVersionId(),
                        versionEntity.getCreationTime(),
                        versionEntity.getInfo(),
                        versionEntity.getModificationTime(),
                        versionEntity.getRelations()
                );
        session.execute(stmt);
    }

    public List<VersionEntity> list() {
        String cql = "SELECT * FROM zusammen_dox.version";
        ResultSet rs = session.execute(cql);
        List<VersionEntity> list = new ArrayList<>();
        for (Row row : rs) {
            VersionEntity entity = new VersionEntity();
            entity.setSpace(row.getString("space"));
            entity.setItemId(row.getString("item_id"));
            entity.setVersionId(row.getString("version_id"));
            entity.setBaseVersionId(row.getString("base_version_id"));
            entity.setCreationTime(row.getInstant("creation_time") != null ? row.getInstant("creation_time") : null);
            entity.setModificationTime(row.getInstant("modification_time") != null ? row.getInstant("modification_time") : null);
            entity.setInfo(row.getString("info"));
            entity.setRelations(row.getString("relations"));
            list.add(entity);
        }
        return list;
    }

    public List<Row> listItemVersion() {
        String cql = "SELECT space, item_id, version_id FROM zusammen_dox.version";
        ResultSet rs = session.execute(cql);
        return rs.all();
    }
}
