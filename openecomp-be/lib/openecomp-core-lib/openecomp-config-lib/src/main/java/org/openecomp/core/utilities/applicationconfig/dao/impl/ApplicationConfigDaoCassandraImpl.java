/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.core.utilities.applicationconfig.dao.impl;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.core.utilities.applicationconfig.dao.ApplicationConfigDao;
import org.openecomp.core.utilities.applicationconfig.dao.type.ApplicationConfigEntity;
import org.openecomp.core.utilities.applicationconfig.type.ConfigurationData;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ApplicationConfigDaoCassandraImpl
        extends CassandraBaseDao<ApplicationConfigEntity>
        implements ApplicationConfigDao {

    private final CqlSession session;

    private final PreparedStatement insertOrUpdateStmt;
    private final PreparedStatement selectStmt;
    private final PreparedStatement listStmt;
    private final PreparedStatement valueAndTimestampStmt;

    public ApplicationConfigDaoCassandraImpl(CqlSession session) {
        super(session);
        this.session = session;

        this.insertOrUpdateStmt = session.prepare(
                "INSERT INTO application_config (namespace, key, value) VALUES (?, ?, ?)");
        this.selectStmt = session.prepare(
                "SELECT namespace, key, value FROM application_config WHERE namespace = ? AND key = ?");
        this.listStmt = session.prepare(
                "SELECT namespace, key, value FROM application_config WHERE namespace = ?");
        this.valueAndTimestampStmt = session.prepare(
                "SELECT value, writetime(value) as ts FROM application_config WHERE namespace = ? AND key = ?");
    }

    @Override
    protected Object[] getKeys(ApplicationConfigEntity entity) {
        return new Object[]{entity.getNamespace(), entity.getKey()};
    }

    @Override
    public void create(ApplicationConfigEntity entity) {
        BoundStatement bound = insertOrUpdateStmt.bind(
                entity.getNamespace(),
                entity.getKey(),
                entity.getValue()
        );
        session.execute(bound);
    }

    @Override
    public void update(ApplicationConfigEntity entity) {
        create(entity); // same as insert in Cassandra
    }

    @Override
    public ApplicationConfigEntity get(ApplicationConfigEntity entity) {
        BoundStatement bound = selectStmt.bind(entity.getNamespace(), entity.getKey());
        Row row = session.execute(bound).one();
        if (row == null) {
            return null;
        }
        return new ApplicationConfigEntity(
                row.getString("namespace"),
                row.getString("key"),
                row.getString("value"));
    }

    @Override
    public Collection<ApplicationConfigEntity> list(ApplicationConfigEntity entity) {
        BoundStatement bound = listStmt.bind(entity.getNamespace());
        ResultSet rs = session.execute(bound);
        return StreamSupport.stream(rs.spliterator(), false)
                .map(row -> new ApplicationConfigEntity(
                        row.getString("namespace"),
                        row.getString("key"),
                        row.getString("value")))
                .collect(Collectors.toList());
    }

    @Override
    public long getValueTimestamp(String namespace, String key) {
        BoundStatement bound = valueAndTimestampStmt.bind(namespace, key);
        Row row = session.execute(bound).one();
        return (row != null) ? row.getLong("ts") : -1;
    }

    @Override
    public ConfigurationData getConfigurationData(String namespace, String key) {
        BoundStatement bound = valueAndTimestampStmt.bind(namespace, key);
        Row row = session.execute(bound).one();
        if (Objects.nonNull(row)) {
            return new ConfigurationData(
                    row.getString("value"),
                    row.getLong("ts"));
        }
        return null;
    }

    // ===== CassandraBaseDao abstract methods =====

    @Override
    protected String getTableName() {
        return "application_config";
    }

    @Override
    protected String[] getColumns(ApplicationConfigEntity entity) {
        return new String[]{"namespace", "key", "value"};
    }

    @Override
    protected Object[] getValues(ApplicationConfigEntity entity) {
        return new Object[]{entity.getNamespace(), entity.getKey(), entity.getValue()};
    }


    public Collection<ApplicationConfigEntity> list(String namespace) {
        BoundStatement bound = listStmt.bind(namespace);
        ResultSet rs = session.execute(bound);
        return StreamSupport.stream(rs.spliterator(), false)
                .map(row -> new ApplicationConfigEntity(
                        row.getString("namespace"),
                        row.getString("key"),
                        row.getString("value")))
                .collect(Collectors.toList());
    }
}
