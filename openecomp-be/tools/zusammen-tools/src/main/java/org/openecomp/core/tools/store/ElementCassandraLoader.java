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

import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import com.datastax.driver.mapping.annotations.QueryParameters;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.core.tools.store.zusammen.datatypes.ElementEntity;

import java.nio.ByteBuffer;
import java.util.Set;

public class ElementCassandraLoader {

    private static NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
    private static Mapper<ElementEntity> mapper = noSqlDb.getMappingManager().mapper(ElementEntity.class);
    private static ElementAccessor accessor = noSqlDb.getMappingManager().createAccessor(ElementAccessor.class);
    private String[] columns = {"space", "item_id", "version_id", "element_id", "data", "info", "namespace", "parent_id",
            "relations", "searchable_data", "sub_element_ids"};


    public void createEntity(ElementEntity elementEntity) {
        accessor.insertElement(elementEntity.getSpace(),
                elementEntity.getItemId(),
                elementEntity.getVersionId(),
                elementEntity.getElement_id(),
                elementEntity.getData(),
                elementEntity.getInfo(),
                elementEntity.getNamespace(),
                elementEntity.getParentId(),
                elementEntity.getRelations(),
                elementEntity.getSearchableData(),
                elementEntity.getSubElementIds());
    }

    public Result<ElementEntity> list() {
        return accessor.getAll();
    }

    @Accessor
    interface ElementAccessor {

        @Query("insert into  zusammen_dox.element (space,item_id,version_id,element_id,data,info,namespace,parent_id,relations,searchable_data,sub_element_ids) values (?,?,?,?,?,?,?,?,?,?,?)")
        void insertElement(String space, String itemId, String versionId, String elementId, ByteBuffer data, String info, String namespaceStr,
                           String parentId, String relations, ByteBuffer searchable, Set<String> subElementsIds);


        @Query("select * from zusammen_dox.element ")
        @QueryParameters(fetchSize = 100)
        Result<ElementEntity> getAll();
    }
}
