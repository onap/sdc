package org.openecomp.sdcrests.item.rest.services;

import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.activitylog.ActivityLogManagerFactory;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.activitylog.dao.type.ActivityType;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.notification.dtos.Event;
import org.openecomp.sdc.notification.factories.NotificationPropagationManagerFactory;
import org.openecomp.sdc.notification.services.NotificationPropagationManager;
import org.openecomp.sdc.versioning.ItemManager;
import org.openecomp.sdc.versioning.ItemManagerFactory;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.types.NotificationEventTypes;
import org.openecomp.sdcrests.item.rest.Items;
import org.openecomp.sdcrests.item.rest.mapping.MapItemToDto;
import org.openecomp.sdcrests.item.types.ItemActionRequestDto;
import org.openecomp.sdcrests.item.types.ItemDto;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.PERMISSION_USER;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.*;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.SUBMIT_DESCRIPTION;

public class ItemsImpl implements Items {

  private ItemManager itemManager =
      ItemManagerFactory.getInstance().createInterface();

    private ActivityLogManager activityLogManager =
            ActivityLogManagerFactory.getInstance().createInterface();

    private VersioningManager versioningManager =
            VersioningManagerFactory.getInstance().createInterface();

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemsImpl.class);

    private NotificationPropagationManager notifier =
            NotificationPropagationManagerFactory.getInstance().createInterface();

  @Override
  public Response actOn(ItemActionRequestDto request, String itemId, String user) {
      Item item  = itemManager.get(itemId);
      if( item == null){
          return Response.status(Response.Status.NOT_FOUND)
                  .entity(new Exception("Item does not exist.")).build();
      }
      switch (request.getAction()) {
          case ARCHIVE:
              itemManager.archive(item);
              notifyUsers(item.getId(), item.getName(), null, "Item was archived", user,
                      NotificationEventTypes.ARCHIVE);
              activityLogManager.logActivity(new ActivityLogEntity(itemId, getLatestVersion(itemId),
                      ActivityType.Archive, user, true, "", ""));
                break;
          case RESTORE:
              itemManager.restore(item);
              notifyUsers(item.getId(), item.getName(), null, "Item was restored", user,
                      NotificationEventTypes.RESTORE);
              activityLogManager.logActivity(new ActivityLogEntity(itemId, getLatestVersion(itemId),
                      ActivityType.Restore, user, true, "", ""));
              break;
          default:
        }
      return Response.ok().build();
    }

    @Override
    public Response getItem(String itemId, String user) {
        Item item  = itemManager.get(itemId);
        ItemDto itemDto = new MapItemToDto().applyMapping(item, ItemDto.class);

        return Response.ok(itemDto).build();
    }

    private Version getLatestVersion(String itemId){
        List<Version> list = versioningManager.list(itemId);
       return list.stream().max(Version::compareTo).get();
    }


    private void notifyUsers(String itemId, String itemName, Version version, String message,
                             String userName, NotificationEventTypes eventType) {
        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(ITEM_NAME, itemName == null ? itemManager.get(itemId).getName() : itemName);
        eventProperties.put(ITEM_ID, itemId);

        eventProperties.put(SUBMIT_DESCRIPTION, message);
        eventProperties.put(PERMISSION_USER, userName);

        Event syncEvent = new SyncEvent(eventType.getEventName(), itemId, eventProperties, itemId);
        try {
            notifier.notifySubscribers(syncEvent, userName);
        } catch (Exception e) {
            LOGGER.error("Failed to send sync notification to users subscribed o item '" + itemId);
        }
    }

    private class SyncEvent implements Event {

        private String eventType;
        private String originatorId;
        private Map<String, Object> attributes;
        private String entityId;

        SyncEvent(String eventType, String originatorId,
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
