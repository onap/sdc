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
package org.openecomp.sdc.versioning.dao.impl;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.sdc.versioning.dao.VersionInfoDeletedDao;
import org.openecomp.sdc.versioning.dao.types.VersionInfoDeletedEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.dao.types.UserCandidateVersion;

import java.util.*;

public class VersionInfoDeletedDaoImpl extends CassandraBaseDao<VersionInfoDeletedEntity> implements VersionInfoDeletedDao {

    private final CqlSession session;

    public VersionInfoDeletedDaoImpl(CqlSession session) {
        super(session);
        this.session = session;
    }

    @Override
    protected Object[] getKeys(VersionInfoDeletedEntity entity) {
        return new Object[]{entity.getEntityType(), entity.getEntityId()};
    }

    @Override
    public Collection<VersionInfoDeletedEntity> list(VersionInfoDeletedEntity entity) {
        return getAll(entity.getEntityType());
    }


    public List<VersionInfoDeletedEntity> getAll(String entityType) {
        String query = "SELECT * FROM version_info_deleted WHERE entity_type = ?";
        PreparedStatement ps = session.prepare(query);
        ResultSet rs = session.execute(ps.bind(entityType));

        List<VersionInfoDeletedEntity> results = new ArrayList<>();
        for (Row row : rs) {
            results.add(mapRow(row));
        }
        return results;
    }

    public Optional<VersionInfoDeletedEntity> get(String entityType, String entityId) {
        String query = "SELECT * FROM version_info_deleted WHERE entity_type = ? AND entity_id = ?";
        PreparedStatement ps = session.prepare(query);
        Row row = session.execute(ps.bind(entityType, entityId)).one();
        return Optional.ofNullable(mapRow(row));
    }

    public void create(VersionInfoDeletedEntity entity) {
        String query = "INSERT INTO version_info_deleted " +
                "(entity_type, entity_id, active_version, status, candidate, viewable_versions, latest_final_version) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = session.prepare(query);
        session.execute(ps.bind(
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getActiveVersion(),
                entity.getStatus(),
                entity.getCandidate(),
                entity.getViewableVersions(),
                entity.getLatestFinalVersion()
        ));
    }

    public void update(VersionInfoDeletedEntity entity) {
        // INSERT in Cassandra = UPSERT
        create(entity);
    }

    public void delete(String entityType, String entityId) {
        String query = "DELETE FROM version_info_deleted WHERE entity_type = ? AND entity_id = ?";
        PreparedStatement ps = session.prepare(query);
        session.execute(ps.bind(entityType, entityId));
    }

    @Override
    protected String getTableName() {
        return "version_info_deleted";
    }

    @Override
    protected String[] getColumns(VersionInfoDeletedEntity entity) {
        return new String[]{
                "entity_type",
                "entity_id",
                "active_version",
                "status",
                "candidate",
                "viewable_versions",
                "latest_final_version"
        };
    }

    @Override
    protected Object[] getValues(VersionInfoDeletedEntity entity) {
        return new Object[]{
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getActiveVersion(),
                entity.getStatus(),
                entity.getCandidate(),
                entity.getViewableVersions(),
                entity.getLatestFinalVersion()
        };
    }

    /* ---------- Row → Entity ---------- */
    private VersionInfoDeletedEntity mapRow(Row row) {
        if (row == null) {
            return null;
        }
        VersionInfoDeletedEntity entity = new VersionInfoDeletedEntity();
        entity.setEntityType(row.getString("entity_type"));
        entity.setEntityId(row.getString("entity_id"));
        entity.setActiveVersion(row.get("active_version", Version.class));
        entity.setStatus(row.get("status", VersionStatus.class));
        entity.setCandidate(row.get("candidate", UserCandidateVersion.class));
        entity.setViewableVersions(row.getSet("viewable_versions", Version.class));
        entity.setLatestFinalVersion(row.get("latest_final_version", Version.class));
        return entity;
    }
}

