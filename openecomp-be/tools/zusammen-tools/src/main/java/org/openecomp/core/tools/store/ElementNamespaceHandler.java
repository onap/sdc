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

import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.core.tools.store.zusammen.datatypes.ElementEntity;

public class ElementNamespaceHandler {

    private static NoSqlDb nnoSqlDb = NoSqlDbFactory.getInstance().createInterface();
    private static ElementNamespaceAccessor accessor = nnoSqlDb.getMappingManager().createAccessor(ElementNamespaceAccessor.class);

    public void createElementNamespace(ElementEntity elementEntity) {
        accessor.create(elementEntity.getSpace(), elementEntity.getItemId(), elementEntity.getElementId(), elementEntity.getNamespace());
    }

    @Accessor
    interface ElementNamespaceAccessor {

        @Query("UPDATE zusammen_dox.element_namespace SET namespace=:ns WHERE space=:space AND item_id=:item AND element_id=:id ")
        void create(@Param("space") String space, @Param("item") String item, @Param("id") String id, @Param("ns") String ns);
    }
}
