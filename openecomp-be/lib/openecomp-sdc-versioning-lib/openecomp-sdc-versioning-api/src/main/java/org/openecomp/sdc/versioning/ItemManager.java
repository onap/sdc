package org.openecomp.sdc.versioning;

import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;

import java.util.Collection;
import java.util.function.Predicate;

public interface ItemManager {

  Collection<Item> list(Predicate<Item> predicate);

  Item get(String itemId);

  Item create(Item item);

  void updateVersionStatus(String itemId, VersionStatus addedVersionStatus,
                           VersionStatus removedVersionStatus);

}
