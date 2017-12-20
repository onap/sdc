package org.openecomp.sdc.healing.healers;

import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.itempermissions.dao.ItemPermissionsDao;
import org.openecomp.sdc.itempermissions.dao.ItemPermissionsDaoFactory;
import org.openecomp.sdc.itempermissions.impl.types.PermissionTypes;
import org.openecomp.sdc.itempermissions.type.ItemPermissionsEntity;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Created by ayalaben on 8/28/2017
 */
public class OwnerHealer implements Healer {
  private static final String HEALING_USER_SUFFIX = "_healer";
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  private static final ItemPermissionsDao permissionsDao =
      ItemPermissionsDaoFactory.getInstance().createInterface();

  public Object heal(String itemId, Version version) {
    mdcDataDebugMessage.debugEntryMessage(null);

    Collection<ItemPermissionsEntity> itemPermissions = permissionsDao.listItemPermissions(itemId);

    if (itemPermissions.stream().noneMatch(this::isOwnerPermission)) {
      String currentUserId =
          SessionContextProviderFactory.getInstance().createInterface().get().getUser().getUserId()
              .replace(HEALING_USER_SUFFIX, "");

      permissionsDao.updateItemPermissions(itemId, PermissionTypes.Owner.name(),
          Collections.singleton(currentUserId), new HashSet<>());

      return currentUserId;
    }
    return itemPermissions.stream().filter(this::isOwnerPermission).findFirst().get().getUserId();
  }

  private boolean isOwnerPermission(ItemPermissionsEntity permissionsEntity) {
    return permissionsEntity.getPermission().equals(PermissionTypes.Owner.name());
  }
}
