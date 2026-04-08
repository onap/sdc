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
package org.openecomp.core.tools.loaders;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openecomp.core.nosqldb.impl.cassandra.CassandraSessionFactory;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.dao.types.UserCandidateVersion;

public class VersionInfoCassandraLoader {

    private static final CqlSession session = CassandraSessionFactory.getSession();

    public static List<VersionInfoEntity> list() {
        List<VersionInfoEntity> result = new ArrayList<>();
        ResultSet rs = session.execute("SELECT * FROM version_info");
        for (Row row : rs) {
            VersionInfoEntity entity = new VersionInfoEntity();
            entity.setEntityType(row.getString("entity_type"));
            entity.setEntityId(row.getString("entity_id"));

            // Map remaining fields, assuming Version, VersionStatus, and UserCandidateVersion are serializable
            entity.setActiveVersion(row.get("active_version", Version.class));
            entity.setStatus(row.get("status", VersionStatus.class));
            entity.setCandidate(row.get("candidate", UserCandidateVersion.class));

            Set<Version> viewableVersions = row.getSet("viewable_versions", Version.class);
            entity.setViewableVersions(viewableVersions != null ? viewableVersions : new HashSet<>());

            entity.setLatestFinalVersion(row.get("latest_final_version", Version.class));

            result.add(entity);
        }
        return result;
    }
}
