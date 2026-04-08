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
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import org.openecomp.core.nosqldb.impl.cassandra.CassandraSessionFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ItemHandler {

    private static final CqlSession session = CassandraSessionFactory.getSession();
    private static final String SELECT_ITEMS = "SELECT item_id FROM zusammen_dox.item";

    public List<String> getItemList() {
        ResultSet resultSet = session.execute(SimpleStatement.newInstance(SELECT_ITEMS));
        List<Row> rows = resultSet.all();
        if (rows != null && !rows.isEmpty()) {
            return rows.stream()
                       .map(row -> row.getString("item_id"))
                       .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

