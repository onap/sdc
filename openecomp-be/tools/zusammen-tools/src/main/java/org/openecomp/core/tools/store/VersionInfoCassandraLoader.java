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
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.data.UdtValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.sdc.versioning.dao.VersionInfoDao;
import org.openecomp.sdc.versioning.dao.VersionInfoDaoFactory;
import org.openecomp.sdc.versioning.dao.types.UserCandidateVersion;


public class VersionInfoCassandraLoader {

    private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
    private static final CqlSession session = noSqlDb.getSession();
    private static final VersionInfoDao versionInfoDao = VersionInfoDaoFactory.getInstance().createInterface();

    public void insertVersionInfo(VersionInfoEntity versionInfoEntity) {
        versionInfoDao.create(versionInfoEntity);
    }

    public Collection<VersionInfoEntity> list() {
        String cql = "SELECT entity_type, entity_id, active_version, status, candidate, viewable_versions, latest_final_version FROM dox.version_info";
        ResultSet rs = session.execute(cql);
        Collection<VersionInfoEntity> result = new ArrayList<>();

        for (Row row : rs) {
            VersionInfoEntity entity = new VersionInfoEntity();
            entity.setEntityType(row.getString("entity_type"));
            entity.setEntityId(row.getString("entity_id"));

            // map UDT manually
            UdtValue activeVersionUdt = row.getUdtValue("active_version");
            if (activeVersionUdt != null) {
                entity.setActiveVersion(new Version(
                    activeVersionUdt.getInt("major"),
                    activeVersionUdt.getInt("minor")
                ));
            }

            // assuming VersionStatus and UserCandidateVersion are enums or classes that can be stored as String/UDT
            String statusStr = row.getString("status");
            if (statusStr != null) {
                entity.setStatus(VersionStatus.valueOf(statusStr));
            }

           UdtValue candidateUdt = row.getUdtValue("candidate");
            if (candidateUdt != null) {
                Version version = candidateUdt.get("version", Version.class); // or construct Version manually if needed
                String user = candidateUdt.getString("user"); // adjust field name according to your UDT
                entity.setCandidate(new UserCandidateVersion(user, version));
        }


            Set<UdtValue> viewableUdtSet = row.getSet("viewable_versions", UdtValue.class);
            if (viewableUdtSet != null) {
                Set<Version> viewable = new HashSet<>();
                for (UdtValue v : viewableUdtSet) {
                    viewable.add(new Version(v.getInt("major"), v.getInt("minor")));
                }
                entity.setViewableVersions(viewable);
            }

            UdtValue latestFinalUdt = row.getUdtValue("latest_final_version");
            if (latestFinalUdt != null) {
                entity.setLatestFinalVersion(new Version(
                    latestFinalUdt.getInt("major"),
                    latestFinalUdt.getInt("minor")
                ));
            }

            result.add(entity);
        }

        return result;
    }
}
