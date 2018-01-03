package org.openecomp.core.tools.store;

import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;

import java.util.HashSet;
import java.util.Set;

public class NotificationHandler {

  public void registerNotificationForUserOnEntity(String user, String entityId) {

    Set<String> userSet = new HashSet<>();
    userSet.add(user);
    NoSqlDbFactory.getInstance().createInterface().getMappingManager()
        .createAccessor(NotificationAccessor.class)
        .updateNotificationSubscription(userSet, entityId);
  }

  @Accessor
  interface NotificationAccessor {

    @Query("UPDATE dox.notification_subscribers SET subscribers = subscribers + ? where " +
        "entity_id = ?")
    void updateNotificationSubscription(Set<String> users, String entityId);
  }
}