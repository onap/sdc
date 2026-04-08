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
import com.datastax.oss.driver.api.core.cql.Row;
import org.openecomp.core.nosqldb.impl.cassandra.CassandraSessionFactory;
import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;

import java.util.*;

public class PermissionHandler {

    private static final CqlSession session = CassandraSessionFactory.getSession();

    public Optional<String> getItemUserPermission(String itemId, String user) {
        String cql = "SELECT permission FROM dox.item_permissions WHERE item_id=? AND user_id=?";
        BoundStatement stmt = session.prepare(cql).bind(itemId, user);
        Row row = session.execute(stmt).one();
        if (row != null) {
            return Optional.of(row.getString("permission"));
        }
        return Optional.empty();
    }

    public void setItemUserPermission(String itemId, String user, String permission) {
        String cql = "INSERT INTO dox.item_permissions (item_id,user_id,permission) VALUES (?,?,?)";
        BoundStatement stmt = session.prepare(cql).bind(itemId, user, permission);
        session.execute(stmt);
    }

    public void addItem(Set<String> items, String userId, String permission) {
        String cql = "UPDATE dox.user_permission_items SET item_list = item_list + ? WHERE user_id = ? AND permission = ?";
        BoundStatement stmt = session.prepare(cql).bind(items, userId, permission);
        session.execute(stmt);
    }

    public List<ItemPermissionsEntity> getAll() {
        String cql = "SELECT * FROM dox.item_permissions";
        List<ItemPermissionsEntity> list = new ArrayList<>();
        session.execute(cql).forEach(row -> {
            ItemPermissionsEntity entity = new ItemPermissionsEntity();
            entity.setItemId(row.getString("item_id"));
            entity.setUserId(row.getString("user_id"));
            entity.setPermission(row.getString("permission"));
            list.add(entity);
        });
        return list;
    }
}

