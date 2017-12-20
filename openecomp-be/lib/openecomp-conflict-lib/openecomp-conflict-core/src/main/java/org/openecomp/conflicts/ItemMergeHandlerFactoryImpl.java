package org.openecomp.conflicts;

import org.openecomp.conflicts.dao.ConflictsDaoFactory;
import org.openecomp.conflicts.impl.VspMergeHandler;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.datatypes.model.ItemType;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspMergeDaoFactory;
import org.openecomp.sdc.versioning.ItemManagerFactory;
import org.openecomp.sdc.versioning.errors.EntityNotExistErrorBuilder;
import org.openecomp.sdc.versioning.types.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ItemMergeHandlerFactoryImpl extends ItemMergeHandlerFactory {
  // TODO: 11/1/2017 read this map from configuration, move Vsp merge handler to vsp lib, rearrange lib deps
  private static final Map<ItemType, ItemMergeHandler> MERGE_HANDLER_BY_ITEM_TYPE =
      new HashMap<>();

  static {
    MERGE_HANDLER_BY_ITEM_TYPE.put(ItemType.vsp,
        new VspMergeHandler(ConflictsDaoFactory.getInstance().createInterface(),
            VspMergeDaoFactory.getInstance().createInterface()));
  }

  @Override
  public Optional<ItemMergeHandler> createInterface(String itemId) {
    Item item = ItemManagerFactory.getInstance().createInterface().get(itemId);
    if (item == null) {
      throw new CoreException(new EntityNotExistErrorBuilder("", itemId).build());
    }
    return Optional.ofNullable(MERGE_HANDLER_BY_ITEM_TYPE.get(ItemType.valueOf(item.getType())));
  }

  @Override
  public ItemMergeHandler createInterface() {
    return null; // call the one with the item id arg
  }
}
