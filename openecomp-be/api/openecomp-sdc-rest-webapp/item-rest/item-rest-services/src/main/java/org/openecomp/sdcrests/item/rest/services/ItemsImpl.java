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

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.PERMISSION_USER;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.ITEM_ID;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.ITEM_NAME;

import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.activitylog.dao.type.ActivityType;
import org.openecomp.sdc.be.csar.storage.StorageFactory;
import org.openecomp.sdc.common.errors.ErrorCode.ErrorCodeBuilder;
import org.openecomp.sdc.common.errors.ErrorCodeAndMessage;
import org.openecomp.sdc.datatypes.model.ItemType;
import org.openecomp.sdc.itempermissions.impl.types.PermissionTypes;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.notification.dtos.Event;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.types.ItemStatus;
import org.openecomp.sdc.versioning.types.NotificationEventTypes;
import org.openecomp.sdcrests.item.rest.Items;
import org.openecomp.sdcrests.item.rest.mapping.MapItemToDto;
import org.openecomp.sdcrests.item.rest.models.SyncEvent;
import org.openecomp.sdcrests.item.rest.services.catalog.notification.NotifierFactory;
import org.openecomp.sdcrests.item.types.ItemAction;
import org.openecomp.sdcrests.item.types.ItemActionRequestDto;
import org.openecomp.sdcrests.item.types.ItemDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Named
@Service("items")
@Scope(value = "prototype")
@Validated
public class ItemsImpl implements Items {

    private static final String ONBOARDING_METHOD = "onboardingMethod";
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemsImpl.class);
    private Map<ItemAction, ActionSideAffects> actionSideAffectsMap = new EnumMap<>(ItemAction.class);
    private ManagersProvider managersProvider;

    @PostConstruct
    public void initActionSideAffectsMap() {
        actionSideAffectsMap.put(ItemAction.ARCHIVE, new ActionSideAffects(ActivityType.Archive, NotificationEventTypes.ARCHIVE));
        actionSideAffectsMap.put(ItemAction.RESTORE, new ActionSideAffects(ActivityType.Restore, NotificationEventTypes.RESTORE));
    }

    @Override
    public Response actOn(final ItemActionRequestDto request, final String itemId, final String user) {
        final var item = getManagersProvider().getItemManager().get(itemId);
        if (item == null) {
            return Response.status(NOT_FOUND).entity(new Exception("Item does not exist.")).build();
        }
        final var action = request.getAction();
        switch (action) {
            case ARCHIVE:
                getManagersProvider().getItemManager().archive(item);
                break;
            case RESTORE:
                if (ItemType.vsp.getName().equalsIgnoreCase(item.getType())) {
                    final var artifactStorageManager = new StorageFactory().createArtifactStorageManager();
                    if (artifactStorageManager.isEnabled() && !artifactStorageManager.exists(itemId)) {
                        LOGGER.error("Unable to restore partially deleted item '{}'", itemId);
                        final var errorCode =
                            new ErrorCodeBuilder().withId(INTERNAL_SERVER_ERROR.name()).withMessage("Unable to restore partially deleted VSP, re-try VSP deletion").build();
                        return Response.status(INTERNAL_SERVER_ERROR).entity(new ErrorCodeAndMessage(INTERNAL_SERVER_ERROR, errorCode)).build();
                    }
                }
                getManagersProvider().getItemManager().restore(item);
                break;
            default:
        }
        actionSideAffectsMap.get(action).execute(item, user);
        try {
            NotifierFactory.getInstance().execute(Collections.singleton(itemId), action);
        } catch (Exception e) {
            LOGGER.error("Failed to send catalog notification on item {}", itemId, e);
        }
        return Response.ok().build();
    }

    @Override
    public Response list(String itemStatusFilter, String versionStatusFilter, String itemTypeFilter, String permissionFilter,
                         String onboardingMethodFilter, String user) {
        Predicate<Item> itemPredicate = createItemPredicate(itemStatusFilter, versionStatusFilter, itemTypeFilter, onboardingMethodFilter,
            permissionFilter, user);
        GenericCollectionWrapper<ItemDto> results = new GenericCollectionWrapper<>();
        MapItemToDto mapper = new MapItemToDto();
        getManagersProvider().getItemManager().list(itemPredicate).stream()
            .sorted((o1, o2) -> o2.getModificationTime().compareTo(o1.getModificationTime()))
            .forEach(item -> results.add(mapper.applyMapping(item, ItemDto.class)));
        return Response.ok(results).build();
    }

    @Override
    public Response getItem(String itemId, String user) {
        Item item = getManagersProvider().getItemManager().get(itemId);
        ItemDto itemDto = new MapItemToDto().applyMapping(item, ItemDto.class);
        return Response.ok(itemDto).build();
    }

    private Predicate<Item> createItemPredicate(String itemStatusFilter, String versionStatusFilter, String itemTypeFilter,
                                                String onboardingMethodFilter, String permissionsFilter, String user) {
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
        Set<VersionStatus> versionStatuses = Arrays.stream(filterValue.split(",")).map(VersionStatus::valueOf).collect(Collectors.toSet());
        return item -> item.getVersionStatusCounters().keySet().stream().anyMatch(versionStatuses::contains);
    }

    private Predicate<Item> createItemTypePredicate(String filterValue) {
        return item -> item.getType().matches(formatFilter(filterValue));
    }

    private Predicate<Item> createOnboardingMethodPredicate(String filterValue) {
        return item -> !ItemType.vsp.name().equals(item.getType()) || ((String) item.getProperties().get(ONBOARDING_METHOD))
            .matches(formatFilter(filterValue));
    }

    private Predicate<Item> createPermissionsPredicate(String user, String filterValue) {
        String[] permissions = filterValue.split(",");
        Set<String> itemIds = new HashSet<>();
        for (String permission : permissions) {
            itemIds.addAll(getManagersProvider().getPermissionsManager().listUserPermittedItems(user, permission));
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

    @VisibleForTesting
    Map<ItemAction, ActionSideAffects> getActionSideAffectsMap() {
        return actionSideAffectsMap;
    }

    private ManagersProvider getManagersProvider() {
        if (managersProvider == null) {
            managersProvider = new ManagersProvider();
        }
        return managersProvider;
    }

    @VisibleForTesting
    void setManagersProvider(ManagersProvider managersProvider) {
        this.managersProvider = managersProvider;
    }

    //Do not delete - is in use, duplicates code to prevent dependency on openecomp-sdc-vendor-software-product-api
    private enum OnboardingMethod {NetworkPackage, Manual}

    private class ActionSideAffects {

        private ActivityType activityType;
        private NotificationEventTypes notificationType;

        private ActionSideAffects(ActivityType activityType, NotificationEventTypes notificationType) {
            this.activityType = activityType;
            this.notificationType = notificationType;
        }

        private Version getLatestVersion(String itemId) {
            List<Version> list = getManagersProvider().getVersioningManager().list(itemId);
            Optional<Version> max = list.stream().max(Version::compareTo);
            return max.orElse(null);
        }

        private void execute(Item item, String user) {
            notifyUsers(item.getId(), item.getName(), user, this.notificationType);
            getManagersProvider().getActivityLogManager()
                .logActivity(new ActivityLogEntity(item.getId(), getLatestVersion(item.getId()), this.activityType, user, true, "", ""));
        }

        private void notifyUsers(String itemId, String itemName, String userName, NotificationEventTypes eventType) {
            Map<String, Object> eventProperties = new HashMap<>();
            eventProperties.put(ITEM_NAME, itemName == null ? getManagersProvider().getItemManager().get(itemId).getName() : itemName);
            eventProperties.put(ITEM_ID, itemId);
            eventProperties.put(PERMISSION_USER, userName);
            Event syncEvent = new SyncEvent(eventType.getEventName(), itemId, eventProperties, itemId);
            try {
                getManagersProvider().getNotificationPropagationManager().notifySubscribers(syncEvent, userName);
            } catch (Exception e) {
                LOGGER.error("Failed to send sync notification to users subscribed to item '{}'", itemId, e);
            }
        }
    }
}
