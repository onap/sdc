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
import org.openecomp.sdc.datatypes.model.ItemType;
import org.openecomp.sdc.itempermissions.PermissionsManager;
import org.openecomp.sdc.itempermissions.PermissionsManagerFactory;
import org.openecomp.sdc.itempermissions.impl.types.PermissionTypes;
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
import org.openecomp.sdc.versioning.types.ItemStatus;
import org.openecomp.sdc.versioning.types.NotificationEventTypes;
import org.openecomp.sdcrests.item.rest.Items;
import org.openecomp.sdcrests.item.rest.mapping.MapItemToDto;
import org.openecomp.sdcrests.item.types.ItemAction;
import org.openecomp.sdcrests.item.types.ItemActionRequestDto;
import org.openecomp.sdcrests.item.types.ItemDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.PERMISSION_USER;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.ITEM_ID;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.ITEM_NAME;

@Named
@Service("items")
@Scope(value = "prototype")
@Validated
public class ItemsImpl implements Items {

    private ItemManager itemManager = ItemManagerFactory.getInstance().createInterface();

    private static ActivityLogManager activityLogManager = ActivityLogManagerFactory.getInstance().createInterface();

    private VersioningManager versioningManager = VersioningManagerFactory.getInstance().createInterface();

    private final PermissionsManager permissionsManager = PermissionsManagerFactory.getInstance().createInterface();

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemsImpl.class);

    private NotificationPropagationManager notifier =
            NotificationPropagationManagerFactory.getInstance().createInterface();



    private Map<ItemAction, ActionSideAffects> actionSideAffectsMap = new EnumMap<>(ItemAction.class);

    @PostConstruct
    public void initActionSideAffectsMap() {
        actionSideAffectsMap
                .put(ItemAction.ARCHIVE, new ActionSideAffects(ActivityType.Archive, NotificationEventTypes.ARCHIVE));
        actionSideAffectsMap
                .put(ItemAction.RESTORE, new ActionSideAffects(ActivityType.Restore, NotificationEventTypes.RESTORE));
    }

    private static final String ONBOARDING_METHOD = "onboardingMethod";


    @Override
    public Response actOn(ItemActionRequestDto request, String itemId, String user) {

        Item item = itemManager.get(itemId);
        if (item == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new Exception("Item does not exist.")).build();
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

        actionSideAffectsMap.get(request.getAction()).execute(item, user);
        try {
            CatalogNotifier catalogNotifier = new CatalogNotifier();
            catalogNotifier.execute(Collections.singleton(itemId), request.getAction(), 2);
        } catch (Exception e){
            LOGGER.error("Failed to send catalog notification on item " + itemId + " Error: " + e.getMessage());
        }

        return Response.ok().build();
    }

    @Override
    public Response list(String itemStatusFilter, String versionStatusFilter, String itemTypeFilter,
            String permissionFilter, String onboardingMethodFilter, String user) {

        Predicate<Item> itemPredicate =
                createItemPredicate(itemStatusFilter, versionStatusFilter, itemTypeFilter, onboardingMethodFilter,
                        permissionFilter, user);

        GenericCollectionWrapper<ItemDto> results = new GenericCollectionWrapper<>();
        MapItemToDto mapper = new MapItemToDto();
        itemManager.list(itemPredicate).stream()
                   .sorted((o1, o2) -> o2.getModificationTime().compareTo(o1.getModificationTime()))
                   .forEach(item -> results.add(mapper.applyMapping(item, ItemDto.class)));

        return Response.ok(results).build();

    }

    @Override
    public Response getItem(String itemId, String user) {
        Item item = itemManager.get(itemId);
        ItemDto itemDto = new MapItemToDto().applyMapping(item, ItemDto.class);

        return Response.ok(itemDto).build();
    }

    private class SyncEvent implements Event {

        private String eventType;
        private String originatorId;
        private Map<String, Object> attributes;
        private String entityId;

        SyncEvent(String eventType, String originatorId, Map<String, Object> attributes, String entityId) {
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

    private class ActionSideAffects {

        private ActivityType activityType;
        private NotificationEventTypes notificationType;

        private ActionSideAffects(ActivityType activityType, NotificationEventTypes notificationType) {
            this.activityType = activityType;
            this.notificationType = notificationType;

        }

        private Version getLatestVersion(String itemId) {
            List<Version> list = versioningManager.list(itemId);
            Optional<Version> max = list.stream().max(Version::compareTo);

            return max.orElse(null);
        }

        private void execute(Item item, String user) {
            notifyUsers(item.getId(), item.getName(), user, this.notificationType);
            activityLogManager.logActivity(
                    new ActivityLogEntity(item.getId(), getLatestVersion(item.getId()), this.activityType, user, true,
                            "", ""));
        }

        private void notifyUsers(String itemId, String itemName, String userName, NotificationEventTypes eventType) {
            Map<String, Object> eventProperties = new HashMap<>();
            eventProperties.put(ITEM_NAME, itemName == null ? itemManager.get(itemId).getName() : itemName);
            eventProperties.put(ITEM_ID, itemId);

            eventProperties.put(PERMISSION_USER, userName);

            Event syncEvent = new SyncEvent(eventType.getEventName(), itemId, eventProperties, itemId);
            try {
                notifier.notifySubscribers(syncEvent, userName);
            } catch (Exception e) {
                LOGGER.error("Failed to send sync notification to users subscribed to item '" + itemId);
            }
        }
    }

    private Predicate<Item> createItemPredicate(String itemStatusFilter, String versionStatusFilter,
            String itemTypeFilter, String onboardingMethodFilter, String permissionsFilter, String user) {
        Predicate<Item> itemPredicate = item -> true;

        if (itemStatusFilter != null) {
            validateItemStatusValue(itemStatusFilter);
            itemPredicate = itemPredicate.and(createItemStatusPredicate(itemStatusFilter));
        }
        if (versionStatusFilter != null) {
            validateVersionStatusValue(versionStatusFilter);
            itemPredicate = itemPredicate.and(createVersionStatusPredicate(versionStatusFilter));
        }
        if (itemTypeFilter != null) {
            validateItemTypeValue(itemTypeFilter);
            itemPredicate = itemPredicate.and(createItemTypePredicate(itemTypeFilter));
        }
        if (onboardingMethodFilter != null) {
            validateOnboardingMethodValue(onboardingMethodFilter);
            itemPredicate = itemPredicate.and(createOnboardingMethodPredicate(onboardingMethodFilter));
        }
        if (permissionsFilter != null) {
            validatePermissionValue(permissionsFilter);
            itemPredicate = itemPredicate.and(createPermissionsPredicate(user, permissionsFilter));
        }
        return itemPredicate;
    }

    private String formatFilter(String filterValue) {
        return filterValue.replace(",", "|");
    }

    private Predicate<Item> createItemStatusPredicate(String filterValue) {
        return item -> item.getStatus().name().matches(formatFilter(filterValue));
    }

    private Predicate<Item> createVersionStatusPredicate(String filterValue) {
        Set<VersionStatus> versionStatuses =
                Arrays.stream(filterValue.split(",")).map(VersionStatus::valueOf).collect(Collectors.toSet());
        return item -> item.getVersionStatusCounters().keySet().stream().anyMatch(versionStatuses::contains);
    }

    private Predicate<Item> createItemTypePredicate(String filterValue) {
        return item -> item.getType().matches(formatFilter(filterValue));
    }

    private Predicate<Item> createOnboardingMethodPredicate(String filterValue) {
        return item -> !ItemType.vsp.name().equals(item.getType()) || ((String) item.getProperties()
                                                                                    .get(ONBOARDING_METHOD))
                                                                              .matches(formatFilter(filterValue));
    }

    private Predicate<Item> createPermissionsPredicate(String user, String filterValue) {
        String[] permissions = filterValue.split(",");
        Set<String> itemIds = new HashSet<>();
        for (String permission : permissions) {
            itemIds.addAll(permissionsManager.listUserPermittedItems(user, permission));
        }
        return item -> itemIds.contains(item.getId());
    }

    private void validateItemStatusValue(String itemStatusFilter) {
        String[] values = itemStatusFilter.split(",");
        for (String value : values) {
            ItemStatus.valueOf(value);
        }
    }

    private void validateVersionStatusValue(String versionStatusFilter) {
        String[] values = versionStatusFilter.split(",");
        for (String value : values) {
            VersionStatus.valueOf(value);
        }

    }

    private void validateItemTypeValue(String itemTypeFilter) {
        String[] values = itemTypeFilter.split(",");
        for (String value : values) {
            ItemType.valueOf(value);
        }
    }

    private void validateOnboardingMethodValue(String onboardingMethodFilter) {
        String[] values = onboardingMethodFilter.split(",");
        for (String value : values) {
            OnboardingMethod.valueOf(value);
        }
    }

    private void validatePermissionValue(String permissionsFilter) {
        String[] values = permissionsFilter.split(",");
        for (String value : values) {
            PermissionTypes.valueOf(value);
        }
    }

    //Do not delete - is in use, duplicates code to prevent dependency on openecomp-sdc-vendor-software-product-api
    private enum OnboardingMethod {
        NetworkPackage, Manual;
    }
}