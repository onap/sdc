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
import com.datastax.driver.mapping.annotations.Query;
import java.util.HashSet;
import java.util.Set;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;

public class NotificationHandler {

    public void registerNotificationForUserOnEntity(String user, String entityId) {
        Set<String> userSet = new HashSet<>();
        userSet.add(user);
        NoSqlDbFactory.getInstance().createInterface().getMappingManager().createAccessor(NotificationAccessor.class)
            .updateNotificationSubscription(userSet, entityId);
    }

    @Accessor
    interface NotificationAccessor {

        @Query("UPDATE dox.notification_subscribers SET subscribers = subscribers + ? where " + "entity_id = ?")
        void updateNotificationSubscription(Set<String> users, String entityId);
    }
}
