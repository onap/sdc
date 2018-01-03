package org.openecomp.core.tools.concurrent;

import org.openecomp.core.tools.store.NotificationHandler;
import org.openecomp.core.tools.store.PermissionHandler;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;


public class ItemAddContributorsTask implements Callable<String> {

  private static final String CONTRIBUTOR = "Contributor";
  private static final String SUCCESSFUL_RETURN_MESSAGE = "Users added successfully as " +
      "contributors to item id:%s.";
  private String itemId;
  private List<String> users;
  private PermissionHandler permissionHandler;
  private NotificationHandler notificationHandler;

  public ItemAddContributorsTask(PermissionHandler permissionHandler, NotificationHandler
      notificationHandler, String itemId, List<String> users) {
    this.itemId = itemId.trim();
    this.users = users;
    this.permissionHandler = permissionHandler;
    this.notificationHandler = notificationHandler;
  }

  @Override
  public String call() throws Exception {
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
