package org.openecomp.sdc.itempermissions.dao.impl;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.itempermissions.ItemPermissionsManager;
import org.openecomp.sdc.itempermissions.PermissionsServices;
import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.notification.dtos.Event;
import org.openecomp.sdc.notification.services.NotificationPropagationManager;
import org.openecomp.sdc.notification.services.SubscriptionService;
import org.openecomp.sdc.versioning.ItemManager;
import org.openecomp.sdc.versioning.types.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.ITEM_ID_PROP;
import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.ITEM_NAME_PROP;
import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.PERMISSION_CHANGED;
import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.PERMISSION_GRANTED;
import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.PERMISSION_ITEM;
import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.PERMISSION_USER;

/**
 * Created by ayalaben on 6/18/2017.
 */
public class ItemPermissionsManagerImpl implements ItemPermissionsManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(ItemPermissionsManagerImpl.class);
  private static final String CHANGE_PERMISSIONS = "Change_Item_Permissions";

  private PermissionsServices permissionsServices;
  private ItemManager itemManager;
  private NotificationPropagationManager notifier;
  private SubscriptionService subscriptionService;

  public ItemPermissionsManagerImpl(PermissionsServices permissionsServices,
                                    ItemManager itemManager,
                                    NotificationPropagationManager notificationPropagationManager,
                                    SubscriptionService subscriptionService) {
    this.permissionsServices = permissionsServices;
    this.itemManager = itemManager;
    this.notifier = notificationPropagationManager;
    this.subscriptionService = subscriptionService;
  }

  @Override
  public Collection<ItemPermissionsEntity> listItemPermissions(String itemId) {

    return permissionsServices.listItemPermissions(itemId);
  }

  @Override
  public void updateItemPermissions(String itemId, String permission, Set<String> addedUsersIds,
                                    Set<String> removedUsersIds) {

    String currentUser =
        SessionContextProviderFactory.getInstance().createInterface().get().getUser().getUserId();

    if (!permissionsServices.isAllowed(itemId, currentUser, CHANGE_PERMISSIONS)) {
      throw new CoreException(new ErrorCode.ErrorCodeBuilder()
          .withMessage(Messages.PERMISSIONS_ERROR.getErrorMessage())
          .withId(Messages.PERMISSIONS_ERROR.getErrorMessage())
          .withCategory(ErrorCategory.SECURITY).build());
    }

    permissionsServices
        .updateItemPermissions(itemId, permission, addedUsersIds, removedUsersIds);
    sendNotifications(itemId, permission, addedUsersIds, removedUsersIds, currentUser);
  }

  private void sendNotifications(String itemId, String permission, Set<String> addedUsersIds,
                                   Set<String> removedUsersIds, String userName) {

    Item item = itemManager.get(itemId);
    addedUsersIds.forEach(affectedUser -> {
      notifyUser(userName, true, item.getName(), itemId, affectedUser, permission);
      subscriptionService.subscribe(affectedUser, itemId);
    });
    removedUsersIds.forEach(affectedUser -> {
      notifyUser(userName, false, item.getName(), itemId, affectedUser, permission);
      subscriptionService.unsubscribe(affectedUser, itemId);
    });

  }

  private void notifyUser(String userName, boolean granted, String itemName, String itemId,
                          String affectedUser, String permission) {
    Map<String, Object> details = new HashMap<>();
    details.put(PERMISSION_ITEM, permission);
    details.put(ITEM_ID_PROP, itemId);
    details.put(ITEM_NAME_PROP, itemName);
    details.put(PERMISSION_GRANTED, granted);
    details.put(PERMISSION_USER, userName);
    PermissionEvent permissionEvent = new PermissionEvent(PERMISSION_CHANGED, affectedUser,
        details, affectedUser);

    try {
      notifier.directNotification(permissionEvent, affectedUser);
    } catch (Exception e) {
      LOGGER.error("Failed to send notification on permission changed for user '" +
          affectedUser + "'");
    }

  }

  @Override
  public boolean isAllowed(String itemId, String userId, String action) {
    return permissionsServices.isAllowed(itemId, userId, action);
  }

  @Override
  public String getUserItemPermiission(String itemId, String userId) {
    return permissionsServices.getUserItemPermiission(itemId, userId);
  }

  private class PermissionEvent implements Event {

    private String eventType;
    private String originatorId;
    private Map<String, Object> attributes;
    private String entityId;

    private PermissionEvent(String eventType, String originatorId,
                            Map<String, Object> attributes, String entityId) {
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
