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
package org.openecomp.sdcrests.vendorlicense.rest.services;

import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.PERMISSION_USER;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.ITEM_ID;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.ITEM_NAME;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.SUBMIT_DESCRIPTION;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.VERSION_ID;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.VERSION_NAME;
import static org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelActionRequestDto.VendorLicenseModelAction.Submit;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import org.openecomp.core.dao.UniqueValueDaoFactory;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.activitylog.ActivityLogManagerFactory;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.activitylog.dao.type.ActivityType;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.model.ItemType;
import org.openecomp.sdc.healing.factory.HealingManagerFactory;
import org.openecomp.sdc.itempermissions.PermissionsManager;
import org.openecomp.sdc.itempermissions.PermissionsManagerFactory;
import org.openecomp.sdc.itempermissions.impl.types.PermissionTypes;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.notification.dtos.Event;
import org.openecomp.sdc.notification.factories.NotificationPropagationManagerFactory;
import org.openecomp.sdc.notification.services.NotificationPropagationManager;
import org.openecomp.sdc.vendorlicense.VendorLicenseConstants;
import org.openecomp.sdc.vendorlicense.VendorLicenseManager;
import org.openecomp.sdc.vendorlicense.VendorLicenseManagerFactory;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.versioning.AsdcItemManager;
import org.openecomp.sdc.versioning.AsdcItemManagerFactory;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.types.ItemStatus;
import org.openecomp.sdc.versioning.types.NotificationEventTypes;
import org.openecomp.sdcrests.item.rest.mapping.MapItemToDto;
import org.openecomp.sdcrests.item.rest.mapping.MapVersionToDto;
import org.openecomp.sdcrests.item.types.ItemCreationDto;
import org.openecomp.sdcrests.item.types.ItemDto;
import org.openecomp.sdcrests.item.types.VersionDto;
import org.openecomp.sdcrests.vendorlicense.rest.VendorLicenseModels;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapVendorLicenseModelEntityToDto;
import org.openecomp.sdcrests.vendorlicense.rest.mapping.MapVendorLicenseModelRequestDtoToVendorLicenseModelEntity;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelActionRequestDto;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelEntityDto;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelRequestDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Named
@Service("vendorLicenseModels")
@Scope(value = "prototype")
@Validated
public class VendorLicenseModelsImpl implements VendorLicenseModels {

    private static final String SUBMIT_ITEM_ACTION = "Submit_Item";
    private static final String SUBMIT_HEALED_VERSION_ERROR = "VLM Id %s: Error while submitting version %s created based on Certified version %s for healing purpose.";
    private static final Logger LOGGER = LoggerFactory.getLogger(VendorLicenseModelsImpl.class);
    private PermissionsManager permissionsManager = PermissionsManagerFactory.getInstance().createInterface();
    private NotificationPropagationManager notifier = NotificationPropagationManagerFactory.getInstance().createInterface();
    private AsdcItemManager asdcItemManager = AsdcItemManagerFactory.getInstance().createInterface();
    private VersioningManager versioningManager = VersioningManagerFactory.getInstance().createInterface();
    private VendorLicenseManager vendorLicenseManager = VendorLicenseManagerFactory.getInstance().createInterface();
    private ActivityLogManager activityLogManager = ActivityLogManagerFactory.getInstance().createInterface();
    private UniqueValueUtil uniqueValueUtil = new UniqueValueUtil(UniqueValueDaoFactory.getInstance().createInterface());

    @Override
    public Response listLicenseModels(String versionStatus, String itemStatus, String user) {
        Predicate<Item> itemPredicate = createItemPredicate(versionStatus, itemStatus, user);
        GenericCollectionWrapper<ItemDto> results = new GenericCollectionWrapper<>();
        MapItemToDto mapper = new MapItemToDto();
        asdcItemManager.list(itemPredicate).stream().sorted((o1, o2) -> o2.getModificationTime().compareTo(o1.getModificationTime()))
            .forEach(item -> results.add(mapper.applyMapping(item, ItemDto.class)));
        return Response.ok(results).build();
    }

    @Override
    public Response createLicenseModel(VendorLicenseModelRequestDto request, String user) {
        Item item = new Item();
        item.setType(ItemType.vlm.name());
        item.setOwner(user);
        item.setStatus(ItemStatus.ACTIVE);
        item.setName(request.getVendorName());
        item.setDescription(request.getDescription());
        uniqueValueUtil.validateUniqueValue(VendorLicenseConstants.UniqueValues.VENDOR_NAME, item.getName());
        item = asdcItemManager.create(item);
        uniqueValueUtil.createUniqueValue(VendorLicenseConstants.UniqueValues.VENDOR_NAME, item.getName());
        Version version = versioningManager.create(item.getId(), new Version(), null);
        VendorLicenseModelEntity vlm = new MapVendorLicenseModelRequestDtoToVendorLicenseModelEntity()
            .applyMapping(request, VendorLicenseModelEntity.class);
        vlm.setId(item.getId());
        vlm.setVersion(version);
        vendorLicenseManager.createVendorLicenseModel(vlm);
        versioningManager.publish(item.getId(), version, "Initial vlm:" + vlm.getVendorName());
        ItemCreationDto itemCreationDto = new ItemCreationDto();
        itemCreationDto.setItemId(item.getId());
        itemCreationDto.setVersion(new MapVersionToDto().applyMapping(version, VersionDto.class));
        activityLogManager.logActivity(new ActivityLogEntity(vlm.getId(), version, ActivityType.Create, user, true, "", ""));
        return Response.ok(itemCreationDto).build();
    }

    @Override
    public Response updateLicenseModel(VendorLicenseModelRequestDto request, String vlmId, String versionId, String user) {
        VendorLicenseModelEntity vlm = new MapVendorLicenseModelRequestDtoToVendorLicenseModelEntity()
            .applyMapping(request, VendorLicenseModelEntity.class);
        vlm.setId(vlmId);
        vlm.setVersion(new Version(versionId));
        vendorLicenseManager.updateVendorLicenseModel(vlm);
        return Response.ok().build();
    }

    @Override
    public Response getLicenseModel(String vlmId, String versionId, String user) {
        Version version = versioningManager.get(vlmId, new Version(versionId));
        VendorLicenseModelEntity vlm = vendorLicenseManager.getVendorLicenseModel(vlmId, version);
        try {
            HealingManagerFactory.getInstance().createInterface().healItemVersion(vlmId, version, ItemType.vlm, false).ifPresent(healedVersion -> {
                vlm.setVersion(healedVersion);
                if (version.getStatus() == VersionStatus.Certified) {
                    submitHealedVersion(vlmId, healedVersion, versionId, user);
                }
            });
        } catch (Exception e) {
            LOGGER.error(String.format("Error while auto healing VLM with Id %s and version %s", vlmId, versionId), e);
        }
        VendorLicenseModelEntityDto vlmDto = new MapVendorLicenseModelEntityToDto().applyMapping(vlm, VendorLicenseModelEntityDto.class);
        return Response.ok(vlmDto).build();
    }

    @Override
    public Response deleteLicenseModel(String vlmId, String user) {
        Item vlm = asdcItemManager.get(vlmId);
        if (!vlm.getType().equals(ItemType.vlm.name())) {
            throw new CoreException((new ErrorCode.ErrorCodeBuilder().withMessage(String.format("Vlm with id %s does not exist.", vlmId)).build()));
        }
        Integer certifiedVersionsCounter = vlm.getVersionStatusCounters().get(VersionStatus.Certified);
        if (Objects.isNull(certifiedVersionsCounter) || certifiedVersionsCounter == 0) {
            asdcItemManager.delete(vlm);
            permissionsManager.deleteItemPermissions(vlmId);
            uniqueValueUtil.deleteUniqueValue(VendorLicenseConstants.UniqueValues.VENDOR_NAME, vlm.getName());
            notifyUsers(vlmId, vlm.getName(), null, null, user, NotificationEventTypes.DELETE);
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity(new Exception(Messages.DELETE_VLM_ERROR.getErrorMessage())).build();
        }
    }

    @Override
    public Response actOnLicenseModel(VendorLicenseModelActionRequestDto request, String vlmId, String versionId, String user) {
        Version version = new Version(versionId);
        if (request.getAction() == Submit) {
            if (!permissionsManager.isAllowed(vlmId, user, SUBMIT_ITEM_ACTION)) {
                return Response.status(Response.Status.FORBIDDEN).entity(new Exception(Messages.PERMISSIONS_ERROR.getErrorMessage())).build();
            }
            String message = request.getSubmitRequest() == null ? "Submit" : request.getSubmitRequest().getMessage();
            submit(vlmId, version, message, user);
            notifyUsers(vlmId, null, version, message, user, NotificationEventTypes.SUBMIT);
        }
        return Response.ok().build();
    }

    private void submit(String vlmId, Version version, String message, String user) {
        vendorLicenseManager.validate(vlmId, version);
        versioningManager.submit(vlmId, version, message);
        activityLogManager.logActivity(new ActivityLogEntity(vlmId, version, ActivityType.Submit, user, true, "", message));
    }

    private void submitHealedVersion(String vlmId, Version healedVersion, String baseVersionId, String user) {
        try {
            submit(vlmId, healedVersion, "Submit after heal", user);
        } catch (Exception ex) {
            LOGGER.error(String.format(SUBMIT_HEALED_VERSION_ERROR, vlmId, healedVersion.getId(), baseVersionId), ex);
        }
    }

    private void notifyUsers(String itemId, String itemName, Version version, String message, String userName, NotificationEventTypes eventType) {
        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(ITEM_NAME, itemName == null ? asdcItemManager.get(itemId).getName() : itemName);
        eventProperties.put(ITEM_ID, itemId);
        if (version != null) {
            eventProperties.put(VERSION_NAME, version.getName() == null ? versioningManager.get(itemId, version).getName() : version.getName());
            eventProperties.put(VERSION_ID, version.getId());
        }
        eventProperties.put(SUBMIT_DESCRIPTION, message);
        eventProperties.put(PERMISSION_USER, userName);
        Event syncEvent = new SyncEvent(eventType.getEventName(), itemId, eventProperties, itemId);
        try {
            notifier.notifySubscribers(syncEvent, userName);
        } catch (Exception e) {
            LOGGER.error("Failed to send sync notification to users subscribed o item '" + itemId);
        }
    }

    private boolean userHasPermission(String itemId, String userId) {
        return permissionsManager.getUserItemPermission(itemId, userId)
            .map(permission -> permission.matches(PermissionTypes.Contributor.name() + "|" + PermissionTypes.Owner.name())).orElse(false);
    }

    private Predicate<Item> createItemPredicate(String versionStatus, String itemStatus, String user) {
        Predicate<Item> itemPredicate = item -> ItemType.vlm.name().equals(item.getType());
        if (ItemStatus.ARCHIVED.name().equals(itemStatus)) {
            itemPredicate = itemPredicate.and(item -> ItemStatus.ARCHIVED.equals(item.getStatus()));
        } else {
            itemPredicate = itemPredicate.and(item -> ItemStatus.ACTIVE.equals(item.getStatus()));
            if (VersionStatus.Certified.name().equals(versionStatus)) {
                itemPredicate = itemPredicate.and(item -> item.getVersionStatusCounters().containsKey(VersionStatus.Certified));
            } else if (VersionStatus.Draft.name().equals(versionStatus)) {
                itemPredicate = itemPredicate
                    .and(item -> item.getVersionStatusCounters().containsKey(VersionStatus.Draft) && userHasPermission(item.getId(), user));
            }
        }
        return itemPredicate;
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
}
