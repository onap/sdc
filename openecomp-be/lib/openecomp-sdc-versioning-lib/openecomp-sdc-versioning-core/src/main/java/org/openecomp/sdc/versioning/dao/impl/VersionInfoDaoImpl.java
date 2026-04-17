/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.sdc.versioning.dao.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;

import org.openecomp.sdc.versioning.dao.VersionInfoDao;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.dao.types.UserCandidateVersion;
import org.openecomp.sdc.versioning.dao.types.Version;

public class VersionInfoDaoImpl implements VersionInfoDao {

    private final CqlSession session;

    public VersionInfoDaoImpl(CqlSession session) {
        this.session = session;
    }

    @Override
    public Collection<VersionInfoEntity> list(VersionInfoEntity probe) {
        return list(probe.getEntityType());
    }


    public List<VersionInfoEntity> getAll(String entityType) {
        return list(entityType);
    }


    public Optional<VersionInfoEntity> get(String entityType, String entityId) {
        String query = "SELECT * FROM version_info WHERE entity_type = ? AND entity_id = ?";
        PreparedStatement ps = session.prepare(query);
        Row row = session.execute(ps.bind(entityType, entityId)).one();
        return Optional.ofNullable(mapRow(row));
    }


    public List<VersionInfoEntity> list(String entityType) {
        String query = "SELECT * FROM version_info WHERE entity_type = ?";
        PreparedStatement ps = session.prepare(query);
        ResultSet rs = session.execute(ps.bind(entityType));
        List<VersionInfoEntity> results = new ArrayList<>();
        for (Row row : rs) {
            results.add(mapRow(row));
        }
        return results;
    }

    /* ---------- CRUD helpers ---------- */

    public void create(VersionInfoEntity entity) {
        String query = "INSERT INTO version_info " +
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

    public void update(VersionInfoEntity entity) {
        // Cassandra INSERT is an upsert
        create(entity);
    }

    public void delete(String entityType, String entityId) {
        String query = "DELETE FROM version_info WHERE entity_type = ? AND entity_id = ?";
        PreparedStatement ps = session.prepare(query);
        session.execute(ps.bind(entityType, entityId));
    }

    /* ---------- Row -> Entity mapper ---------- */

    private VersionInfoEntity mapRow(Row row) {
        if (row == null) {
            return null;
        }
        VersionInfoEntity entity = new VersionInfoEntity();
        entity.setEntityType(row.getString("entity_type"));
        entity.setEntityId(row.getString("entity_id"));
        entity.setActiveVersion(row.get("active_version", Version.class));
        entity.setStatus(row.get("status", VersionStatus.class));
        entity.setCandidate(row.get("candidate", UserCandidateVersion.class));
        entity.setViewableVersions(row.getSet("viewable_versions", Version.class));
        entity.setLatestFinalVersion(row.get("latest_final_version", Version.class));
        return entity;
    }

    @Override
    public VersionInfoEntity get(VersionInfoEntity entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'get'");
    }

    @Override
    public void delete(VersionInfoEntity entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }
}
