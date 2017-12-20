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

package org.openecomp.core.tools.store;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import com.datastax.driver.mapping.annotations.QueryParameters;
import com.google.common.collect.Sets;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.core.tools.store.zusammen.datatypes.ElementEntity;
import org.openecomp.core.tools.store.zusammen.datatypes.VersionEntity;

import java.util.Date;
import java.util.Set;

public class VersionCassandraLoader {

    private static NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
    private static Mapper<VersionEntity> mapper = noSqlDb.getMappingManager().mapper(VersionEntity.class);
    private static VersionAccessor accessor = noSqlDb.getMappingManager().createAccessor(VersionAccessor.class);

    public void insertElementToVersion(ElementEntity elementEntity) {
        accessor.addElements(Sets.newHashSet(elementEntity.getElement_id()), elementEntity.getSpace(), elementEntity.getItemId(), elementEntity.getVersionId());
    }

    public void insertVersion(VersionEntity versionEntity) {
        accessor.insertVersion(               versionEntity.getSpace(),
                versionEntity.getItemId(),
                versionEntity.getVersionId(),
                versionEntity.getBaseVersionId(),
                versionEntity.getCreationTime(),
                versionEntity.getInfo(),
                versionEntity.getModificationTime(),
                versionEntity.getRelations());
    }


    public Result<VersionEntity> list() {
        return accessor.getAll();
    }

    public ResultSet listItemVersion() { return accessor.getAllItemVersion();}

    @Accessor
    interface VersionAccessor {

        @Query("UPDATE zusammen_dox.version_elements SET element_ids=element_ids+? " +
                "WHERE space=? AND item_id=? AND version_id=?")
        void addElements(Set<String> elementIds, String space, String itemId, String versionId);

        @Query("insert into zusammen_dox.version (space,item_id,version_id,base_version_id,creation_time,info,modification_time,relations) values (?,?,?,?,?,?,?,?)")
        void insertVersion(String space, String itemId, String versionId, String baseVersionId, Date createTime, String info, Date modificationTime, String relations);


        @Query("select * from zusammen_dox.version ")
        @QueryParameters(fetchSize = 400)
        Result<VersionEntity> getAll();

        @Query("select space,item_id,version_id from zusammen_dox.version ")
        ResultSet getAllItemVersion();
    }
}
