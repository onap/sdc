package org.openecomp.sdc.healing.healers;

import org.openecomp.sdc.common.errors.SdcRuntimeException;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.itempermissions.dao.ItemPermissionsDao;
import org.openecomp.sdc.itempermissions.dao.ItemPermissionsDaoFactory;
import org.openecomp.sdc.itempermissions.impl.types.PermissionTypes;
import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;
import org.openecomp.sdc.notification.dao.SubscribersDao;
import org.openecomp.sdc.notification.factories.SubscribersDaoFactory;
import org.openecomp.sdc.versioning.dao.ItemDao;
import org.openecomp.sdc.versioning.dao.ItemDaoFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.Item;

import java.util.*;

/**
 * Created by ayalaben on 8/28/2017
 */
public class OwnerHealer implements Healer {
  private static final String HEALING_USER_SUFFIX = "_healer";
  private static final ItemPermissionsDao permissionsDao =
      ItemPermissionsDaoFactory.getInstance().createInterface();
  private static final ItemDao itemDao = ItemDaoFactory.getInstance().createInterface();

  private static final SubscribersDao subscribersDao = SubscribersDaoFactory.getInstance()
      .createInterface();

  @Override
  public boolean isHealingNeeded(String itemId, Version version) {
    return permissionsDao.listItemPermissions(itemId).stream().noneMatch(this::isOwnerPermission) ||
        isOwnerMissingOnItem(itemId);
  }

  public void heal(String itemId, Version version) {
    Collection<ItemPermissionsEntity> itemPermissions = permissionsDao.listItemPermissions(itemId);

    if (itemPermissions.stream().noneMatch(this::isOwnerPermission)) {
      String currentUserId =
          SessionContextProviderFactory.getInstance().createInterface().get().getUser().getUserId()
              .replace(HEALING_USER_SUFFIX, "");

      permissionsDao.updateItemPermissions(itemId, PermissionTypes.Owner.name(),
          Collections.singleton(currentUserId), new HashSet<>());

      updateItemOwner(itemId, currentUserId);

      subscribersDao.subscribe(currentUserId, itemId);

    } else if (isOwnerMissingOnItem(itemId)) {
      Optional<ItemPermissionsEntity> ownerOpt =
          itemPermissions.stream().filter(this::isOwnerPermission).findFirst();
      if (ownerOpt.isPresent()) {
        updateItemOwner(itemId, ownerOpt.get().getUserId());
      } else {
        throw new SdcRuntimeException("Unexpected error in Owner Healer. Item id: " + itemId);
      }
    }
  }

  private void updateItemOwner(String itemId, String userId) {
    Item item = new Item();
    item.setId(itemId);
    Item retrievedItem = itemDao.get(item);
    if (Objects.nonNull(retrievedItem)) {
      retrievedItem.setOwner(userId);
      itemDao.update(retrievedItem);
    }
  }

  private boolean isOwnerMissingOnItem(String itemId) {
    Item item = new Item();
    item.setId(itemId);
    Item retrievedItem = itemDao.get(item);
    return Objects.nonNull(retrievedItem) && Objects.isNull(retrievedItem.getOwner());
  }

  private boolean isOwnerPermission(ItemPermissionsEntity permissionsEntity) {
    return permissionsEntity.getPermission().equals(PermissionTypes.Owner.name());
  }
}
