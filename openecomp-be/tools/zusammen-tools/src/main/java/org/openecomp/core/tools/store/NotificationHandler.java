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
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import org.openecomp.core.nosqldb.impl.cassandra.CassandraSessionFactory;

import java.util.HashSet;
import java.util.Set;

public class NotificationHandler {

    private static final CqlSession session = CassandraSessionFactory.getSession();

    public void registerNotificationForUserOnEntity(String user, String entityId) {
        Set<String> userSet = new HashSet<>();
        userSet.add(user);

        String cql = "UPDATE dox.notification_subscribers SET subscribers = subscribers + ? WHERE entity_id = ?";
        SimpleStatement statement = SimpleStatement.newInstance(cql, userSet, entityId);

        session.execute(statement);
    }
}
