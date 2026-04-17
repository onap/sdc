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
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import org.openecomp.core.nosqldb.impl.cassandra.CassandraSessionFactory;
import org.openecomp.core.tools.store.zusammen.datatypes.ElementEntity;

public class ElementNamespaceHandler {

    private static final CqlSession session = CassandraSessionFactory.getSession();

    public void createElementNamespace(ElementEntity elementEntity) {
        String query = "UPDATE zusammen_dox.element_namespace "
                     + "SET namespace = ? "
                     + "WHERE space = ? AND item_id = ? AND element_id = ?";
        PreparedStatement ps = session.prepare(query);

        BoundStatement bound = ps.bind(
            elementEntity.getNamespace(),
            elementEntity.getSpace(),
            elementEntity.getItemId(),
            elementEntity.getElementId()
        );

        session.execute(bound);
    }
}
