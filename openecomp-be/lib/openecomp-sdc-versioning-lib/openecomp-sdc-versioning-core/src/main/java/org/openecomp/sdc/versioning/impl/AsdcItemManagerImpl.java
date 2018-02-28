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

package org.openecomp.sdc.versioning.impl;

import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.itempermissions.PermissionsServices;
import org.openecomp.sdc.notification.services.SubscriptionService;
import org.openecomp.sdc.versioning.AsdcItemManager;
import org.openecomp.sdc.versioning.dao.ItemDao;
import org.openecomp.sdc.versioning.types.Item;

public class AsdcItemManagerImpl extends ItemManagerImpl implements AsdcItemManager {
  private static final String CREATE_ITEM = "Create_Item";

  private PermissionsServices permissionsServices;
  private SubscriptionService subscriptionService;

  public AsdcItemManagerImpl(ItemDao itemDao, PermissionsServices permissionsServices,
                             SubscriptionService subscriptionService) {
    super(itemDao);

    this.permissionsServices = permissionsServices;
    this.subscriptionService = subscriptionService;
  }

  @Override
  public Item create(Item item) {
    Item createdItem = super.create(item);

    String userId = SessionContextProviderFactory.getInstance()
        .createInterface().get().getUser().getUserId();
    String itemId = createdItem.getId();
    permissionsServices.execute(itemId, userId, CREATE_ITEM);
    subscriptionService.subscribe(userId, itemId);

    return createdItem;
  }


  @Override
  public void updateOwner(String itemId, String owner) {
    Item item = get(itemId);
    if (item == null) {
      return;
    }
    item.setOwner(owner);
    super.update(item);
  }

  @Override
  public void delete(Item item) {
    super.delete(item);
  }


}
