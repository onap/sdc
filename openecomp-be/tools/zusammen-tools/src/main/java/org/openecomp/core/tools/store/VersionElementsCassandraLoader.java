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

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import org.openecomp.core.nosqldb.impl.cassandra.CassandraSessionFactory;
import org.openecomp.core.tools.store.zusammen.datatypes.VersionElementsEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VersionElementsCassandraLoader {

    private static final CqlSession session = CassandraSessionFactory.getSession();

    public List<VersionElementsEntity> listVersionElementsByPK(String space, String itemId, String versionId) {
    String cql = "SELECT space, item_id, version_id, revision_id, element_ids " +
                 "FROM zusammen_dox.version_elements WHERE space=? AND item_id=? AND version_id=?";

    BoundStatement stmt = session.prepare(cql).bind(space, itemId, versionId);
    ResultSet rs = session.execute(stmt);

    List<VersionElementsEntity> list = new ArrayList<>();
    for (Row row : rs) {
        VersionElementsEntity entity = new VersionElementsEntity();
        entity.setSpace(row.getString("space"));
        entity.setItemId(row.getString("item_id"));
        entity.setVersionId(row.getString("version_id"));
        entity.setRevisionId(row.getString("revision_id"));

        Set<String> elements = row.getSet("element_ids", String.class);
        Map<String, String> elementMap = new HashMap<>();
        if (elements != null) {
            for (String e : elements) {
                elementMap.put(e, e); // adjust mapping if needed
            }
        }
        entity.setElementIds(elementMap);

        list.add(entity);
    }
    return list;
}

}
