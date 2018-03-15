/*
 * Copyright © 2018 European Support Limited
 *
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
 */
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
import org.openecomp.sdcrests.item.types.ItemAction;
import org.openecomp.sdcrests.item.types.ItemActionRequestDto;
import org.openecomp.sdcrests.item.types.ItemDto;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.PERMISSION_USER;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.*;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.SUBMIT_DESCRIPTION;

@Named
@Service("items")
@Scope(value = "prototype")
public class ItemsImpl implements Items {

    private ItemManager itemManager =
            ItemManagerFactory.getInstance().createInterface();

    private static ActivityLogManager activityLogManager =
            ActivityLogManagerFactory.getInstance().createInterface();

    private VersioningManager versioningManager =
            VersioningManagerFactory.getInstance().createInterface();

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemsImpl.class);

    private NotificationPropagationManager notifier =
            NotificationPropagationManagerFactory.getInstance().createInterface();

    private Map<ItemAction, ActionSideAffects> actionSideAffectsMap = new EnumMap<>(ItemAction.class);

    {
    actionSideAffectsMap.put(ItemAction.ARCHIVE, new ActionSideAffects(ActivityType.Archive,
                    NotificationEventTypes.ARCHIVE));
    actionSideAffectsMap.put(ItemAction.RESTORE, new  ActionSideAffects(ActivityType.Restore,
                    NotificationEventTypes.RESTORE));
    }

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
                break;
          case RESTORE:
              itemManager.restore(item);
              break;
          default:
        }

      actionSideAffectsMap.get(request.getAction()).execute(item,user);

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

    private void notifyUsers(String itemId, String itemName, String message,
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
            LOGGER.error("Failed to send sync notification to users subscribed to item '" + itemId);
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

    private class ActionSideAffects{
      private ActivityType activityType;
      private NotificationEventTypes notificationType;

      public ActionSideAffects(ActivityType activityType, NotificationEventTypes notificationType){
          this.activityType = activityType;
          this.notificationType = notificationType;

      }
        public void execute(Item item, String user){
            notifyUsers(item.getId(), item.getName(), null, user,
                    this.notificationType);
            activityLogManager.logActivity(new ActivityLogEntity(item.getId(), getLatestVersion(item.getId()),
                   this.activityType, user, true, "", ""));
        }
    }

}
