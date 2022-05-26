/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.sdc.itempermissions.dao.impl;

import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.ITEM_ID_PROP;
import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.ITEM_NAME_PROP;
import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.PERMISSION_CHANGED;
import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.PERMISSION_GRANTED;
import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.PERMISSION_ITEM;
import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.PERMISSION_USER;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.itempermissions.PermissionsManager;
import org.openecomp.sdc.itempermissions.PermissionsServices;
import org.openecomp.sdc.itempermissions.impl.types.PermissionTypes;
import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.notification.dtos.Event;
import org.openecomp.sdc.notification.services.NotificationPropagationManager;
import org.openecomp.sdc.notification.services.SubscriptionService;
import org.openecomp.sdc.versioning.AsdcItemManager;
import org.openecomp.sdc.versioning.types.Item;

/**
 * Created by ayalaben on 6/18/2017.
 */
public class PermissionsManagerImpl implements PermissionsManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsManagerImpl.class);
    private static final String CHANGE_PERMISSIONS = "Change_Item_Permissions";
    private PermissionsServices permissionsServices;
    private AsdcItemManager asdcItemManager;
    private NotificationPropagationManager notifier;
    private SubscriptionService subscriptionService;

    public PermissionsManagerImpl(PermissionsServices permissionsServices, AsdcItemManager asdcItemManager,
                                  NotificationPropagationManager notificationPropagationManager, SubscriptionService subscriptionService) {
        this.permissionsServices = permissionsServices;
        this.asdcItemManager = asdcItemManager;
        this.notifier = notificationPropagationManager;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public Collection<ItemPermissionsEntity> listItemPermissions(String itemId) {
        return permissionsServices.listItemPermissions(itemId);
    }

    @Override
    public Set<String> listUserPermittedItems(String userId, String permission) {
        return permissionsServices.listUserPermittedItems(userId, permission);
    }

    @Override
    public void updateItemPermissions(String itemId, String permission, Set<String> addedUsersIds, Set<String> removedUsersIds) {
        String currentUser = SessionContextProviderFactory.getInstance().createInterface().get().getUser().getUserId();
        if (!permissionsServices.isAllowed(itemId, currentUser, CHANGE_PERMISSIONS)) {
            throw new CoreException(
                new ErrorCode.ErrorCodeBuilder().withMessage(Messages.PERMISSIONS_ERROR.getErrorMessage()).withId(Messages.PERMISSIONS_ERROR.name())
                    .withCategory(ErrorCategory.SECURITY).build());
        }
        if (permission.equals(PermissionTypes.Owner.name()) && !addedUsersIds.isEmpty()) {
            if (addedUsersIds.size() == 1) {
                asdcItemManager.updateOwner(itemId, addedUsersIds.iterator().next());
            } else {
                throw new CoreException(new ErrorCode.ErrorCodeBuilder().withMessage(Messages.PERMISSIONS_OWNER_ERROR.getErrorMessage())
                    .withId(Messages.PERMISSIONS_OWNER_ERROR.name()).withCategory(ErrorCategory.SECURITY).build());
            }
        }
        permissionsServices.updateItemPermissions(itemId, permission, addedUsersIds, removedUsersIds);
        sendNotifications(itemId, permission, addedUsersIds, removedUsersIds, currentUser);
    }

    private void sendNotifications(String itemId, String permission, Set<String> addedUsersIds, Set<String> removedUsersIds, String userName) {
        Item item = asdcItemManager.get(itemId);
        String itemName = null != item ? item.getName() : "";
        addedUsersIds.forEach(affectedUser -> {
            notifyUser(userName, true, itemName, itemId, affectedUser, permission);
            subscriptionService.subscribe(affectedUser, itemId);
        });
        removedUsersIds.forEach(affectedUser -> {
            notifyUser(userName, false, itemName, itemId, affectedUser, permission);
            subscriptionService.unsubscribe(affectedUser, itemId);
        });
    }

    private void notifyUser(String userName, boolean granted, String itemName, String itemId, String affectedUser, String permission) {
        Map<String, Object> details = new HashMap<>();
        details.put(PERMISSION_ITEM, permission);
        details.put(ITEM_ID_PROP, itemId);
        details.put(ITEM_NAME_PROP, itemName);
        details.put(PERMISSION_GRANTED, granted);
        details.put(PERMISSION_USER, userName);
        PermissionEvent permissionEvent = new PermissionEvent(PERMISSION_CHANGED, affectedUser, details, affectedUser);
        try {
            notifier.directNotification(permissionEvent, affectedUser);
        } catch (Exception e) {
            LOGGER.error("Failed to send notification on permission changed for user '" + affectedUser + "'");
        }
    }

    @Override
    public boolean isAllowed(String itemId, String userId, String action) {
        return permissionsServices.isAllowed(itemId, userId, action);
    }

    @Override
    public Optional<String> getUserItemPermission(String itemId, String userId) {
        return permissionsServices.getUserItemPermission(itemId, userId);
    }

    @Override
    public void deleteItemPermissions(String itemId) {
        permissionsServices.deleteItemPermissions(itemId);
    }

    private class PermissionEvent implements Event {

        private String eventType;
        private String originatorId;
        private Map<String, Object> attributes;
        private String entityId;

        private PermissionEvent(String eventType, String originatorId, Map<String, Object> attributes, String entityId) {
            this.eventType = eventType;
            this.originatorId = originatorId;
            this.attributes = attributes;
            this.entityId = entityId;
        }

        @Override
        public String getEventType() {
            return eventType;
        }

        @Override
        public String getOriginatorId() {
            return originatorId;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public String getEntityId() {
            return entityId;
        }
    }
}
