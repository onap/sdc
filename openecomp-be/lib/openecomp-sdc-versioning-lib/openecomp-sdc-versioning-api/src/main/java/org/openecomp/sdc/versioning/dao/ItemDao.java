package org.openecomp.sdc.versioning.dao;

import org.openecomp.sdc.versioning.types.Item;

import java.util.Collection;

public interface ItemDao {
  Collection<Item> list();

  Item get(Item item);

  Item create(Item item);

  void update(Item item);
}
