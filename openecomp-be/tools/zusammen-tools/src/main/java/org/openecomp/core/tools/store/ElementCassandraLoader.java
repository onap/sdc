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
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openecomp.core.nosqldb.impl.cassandra.CassandraSessionFactory;
import org.openecomp.core.tools.store.zusammen.datatypes.ElementEntity;

public class ElementCassandraLoader {

    private static final CqlSession session = CassandraSessionFactory.getSession();

    public void createEntity(ElementEntity elementEntity) {
        String query = "INSERT INTO zusammen_dox.element (space,item_id,version_id,element_id,data,info,namespace,parent_id,relations,searchable_data,sub_element_ids) "
                     + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = session.prepare(query);

        BoundStatement bound = ps.bind(
            elementEntity.getSpace(),
            elementEntity.getItemId(),
            elementEntity.getVersionId(),
            elementEntity.getElementId(),
            elementEntity.getData(),
            elementEntity.getInfo(),
            elementEntity.getNamespace(),
            elementEntity.getParentId(),
            elementEntity.getRelations(),
            elementEntity.getSearchableData(),
            elementEntity.getSubElementIds() != null ? elementEntity.getSubElementIds() : new HashSet<>()
        );

        session.execute(bound);
    }

    public List<ElementEntity> list() {
        List<ElementEntity> result = new ArrayList<>();
        ResultSet rs = session.execute("SELECT * FROM zusammen_dox.element");

        for (Row row : rs) {
            ElementEntity entity = mapRowToEntity(row);
            result.add(entity);
        }
        return result;
    }

    public ElementEntity getByPK(String space, String itemId, String versionId, String elementId, String revisionId) {
        String query = "SELECT * FROM zusammen_dox.element WHERE space = ? AND item_id = ? AND version_id = ? AND element_id = ? AND revision_id = ?";
        PreparedStatement ps = session.prepare(query);
        BoundStatement bound = ps.bind(space, itemId, versionId, elementId, revisionId);

        Row row = session.execute(bound).one();
        return row != null ? mapRowToEntity(row) : null;
    }

    private ElementEntity mapRowToEntity(Row row) {
        ElementEntity entity = new ElementEntity();
        entity.setSpace(row.getString("space"));
        entity.setItemId(row.getString("item_id"));
        entity.setVersionId(row.getString("version_id"));
        entity.setElementId(row.getString("element_id"));
        entity.setData(row.getByteBuffer("data"));
        entity.setInfo(row.getString("info"));
        entity.setNamespace(row.getString("namespace"));
        entity.setParentId(row.getString("parent_id"));
        entity.setRelations(row.getString("relations"));
        entity.setSearchableData(row.getByteBuffer("searchable_data"));
        Set<String> subElementIds = row.getSet("sub_element_ids", String.class);
        entity.setSubElementIds(subElementIds != null ? subElementIds : new HashSet<>());
        entity.setVisualization(row.getByteBuffer("visualization"));
        return entity;
    }
}
