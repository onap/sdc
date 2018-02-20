package org.openecomp.activityspec.mocks;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.ItemManager;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;

public class ItemManagerMock implements ItemManager {

  public String id;
  public Item item;


  @Override
  public Collection<Item> list(Predicate<Item> predicate) {
    List<Item> items = new ArrayList<>();
    items.add(item);
    Collection<Item> collection = items;
    return items;
  }

  @Override
  public Item get(String itemId) {
    return null;
  }

  @Override
  public Item create(Item item) {
    this.id = UUID.randomUUID().toString();
    item.setId(this.id);
    this.item  = item;
    return item;
  }

  @Override
  public void updateVersionStatus(String itemId, VersionStatus addedVersionStatus,
                                  VersionStatus removedVersionStatus) {

  }



  @Override
  public void updateName(String itemId, String name) {

  }

  @Override
  public void update(Item item) {

  }

  @Override
  public void delete(Item item) {

  }

}
