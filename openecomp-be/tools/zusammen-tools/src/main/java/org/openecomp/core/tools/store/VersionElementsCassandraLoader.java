/*
* Copyright © 2016-2018 European Support Limited
*
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
*/

package org.openecomp.core.tools.store;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import com.datastax.driver.mapping.annotations.QueryParameters;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.core.tools.store.zusammen.datatypes.VersionElementsEntity;

public class VersionElementsCassandraLoader {
    private static NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
    private static VersionElementsAccessor accessor = noSqlDb.getMappingManager().
            createAccessor(VersionElementsAccessor.class);

    public Result<VersionElementsEntity> listVersionElementsByPK(String space, String itemId, String versionId) {
        return accessor.getByPK(space, itemId, versionId);
    }

    @Accessor
    interface VersionElementsAccessor {

        @Query("SELECT space, item_id, version_id, revision_id, element_ids " +
                "FROM zusammen_dox.version_elements WHERE space=? and item_id=? and version_id=?")

        @QueryParameters(fetchSize = 400)
        Result<VersionElementsEntity> getByPK(String space, String itemId, String versionId);
    }
}
