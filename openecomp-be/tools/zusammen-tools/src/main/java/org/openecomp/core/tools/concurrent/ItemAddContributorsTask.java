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
package org.openecomp.core.tools.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.openecomp.core.tools.store.NotificationHandler;
import org.openecomp.core.tools.store.PermissionHandler;

public class ItemAddContributorsTask implements Callable<String> {

    private static final String CONTRIBUTOR = "Contributor";
    private static final String SUCCESSFUL_RETURN_MESSAGE = "Users added successfully as " + "contributors to item id:%s.";
    private final String itemId;
    private final List<String> users;
    private final PermissionHandler permissionHandler;
    private final NotificationHandler notificationHandler;

    public ItemAddContributorsTask(PermissionHandler permissionHandler, NotificationHandler notificationHandler, String itemId, List<String> users) {
        this.itemId = itemId.trim();
        this.users = new ArrayList<>(users);
        this.permissionHandler = permissionHandler;
        this.notificationHandler = notificationHandler;
    }

    @Override
    public String call() {
        users.forEach(this::handleUser);
        return String.format(SUCCESSFUL_RETURN_MESSAGE, itemId);
    }

    private void handleUser(String user) {
        Optional<String> userPermission = getUserPermission(user);
        if (!userPermission.isPresent()) {
            setUserPermission(user, CONTRIBUTOR);
            registerUserNotificationSubscription(user);
        }
    }

    private void registerUserNotificationSubscription(String user) {
        notificationHandler.registerNotificationForUserOnEntity(user, itemId);
    }

    private void setUserPermission(String user, String permission) {
        permissionHandler.setItemUserPermission(itemId, user, permission);
    }

    private Optional<String> getUserPermission(String user) {
        return permissionHandler.getItemUserPermission(itemId, user);
    }
}
