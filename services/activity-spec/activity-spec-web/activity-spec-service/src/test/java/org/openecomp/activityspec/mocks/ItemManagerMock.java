/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.activityspec.mocks;


import java.util.ArrayList;
import java.util.List;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.ItemManager;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;

public class ItemManagerMock implements ItemManager {

  private String id;
  private Item item;


  @Override
  public Collection<Item> list(Predicate<Item> predicate) {
    List<Item> items = new ArrayList<>();
    items.add(item);
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
  public void archive(Item item) {

  }

  @Override
  public void restore(Item item) {

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
