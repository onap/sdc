package org.openecomp.sdc.versioning.impl;

import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.itempermissions.PermissionsServices;
import org.openecomp.sdc.notification.services.SubscriptionService;
import org.openecomp.sdc.versioning.ItemManager;
import org.openecomp.sdc.versioning.dao.ItemDao;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ItemManagerImpl implements ItemManager {
  public static final String CREATE_ITEM = "Create_Item";

  private ItemDao itemDao;
  private PermissionsServices permissionsServices;
  private SubscriptionService subscriptionService;

  public ItemManagerImpl(ItemDao itemDao, PermissionsServices permissionsServices,
                         SubscriptionService subscriptionService) {
    this.itemDao = itemDao;
    this.permissionsServices = permissionsServices;
    this.subscriptionService = subscriptionService;
  }

  @Override
  public Collection<Item> list(Predicate<Item> predicate) {
    return itemDao.list().stream().filter(predicate).collect(Collectors.toList());
  }

  @Override
  public Item get(String itemId) {
    Item item = new Item();
    item.setId(itemId);
    return itemDao.get(item);
  }

  @Override
  public Item create(Item item) {
    Item createdItem = itemDao.create(item);

    String userId = SessionContextProviderFactory.getInstance()
        .createInterface().get().getUser().getUserId();
    String itemId = createdItem.getId();
    permissionsServices.execute(itemId, userId, CREATE_ITEM);
    subscriptionService.subscribe(userId, itemId);

    return createdItem;
  }

  @Override
  public void updateVersionStatus(String itemId, VersionStatus addedVersionStatus,
                                  VersionStatus removedVersionStatus) {
    Item item = get(itemId);
    if (item == null) {
      return;
    }

    item.addVersionStatus(addedVersionStatus);
    if (removedVersionStatus != null) {
      item.removeVersionStatus(removedVersionStatus);
    }
    itemDao.update(item);
  }
}
