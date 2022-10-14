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

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.versioning.ItemManager;
import org.openecomp.sdc.versioning.dao.ItemDao;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.types.ItemStatus;

public class ItemManagerImpl implements ItemManager {

    private ItemDao itemDao;

    public ItemManagerImpl(ItemDao itemDao) {
        this.itemDao = itemDao;
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
        return itemDao.create(item);
    }

    @Override
    public void updateVersionStatus(String itemId, VersionStatus addedVersionStatus, VersionStatus removedVersionStatus) {
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

    @Override
    public void delete(Item item) {
        itemDao.delete(item);
    }

    @Override
    public void updateName(String itemId, String name) {
        Item item = get(itemId);
        if (item == null) {
            return;
        }
        item.setName(name);
        itemDao.update(item);
    }

    @Override
    public void archive(Item item) {
        if (item.getStatus() == ItemStatus.ARCHIVED) {
            throw new CoreException(new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION)
                .withMessage(String.format("Archive item failed, item %s is already Archived", item.getId())).build());
        }
        item.setStatus(ItemStatus.ARCHIVED);
        itemDao.update(item);
    }

    @Override
    public void restore(Item item) {
        if (item.getStatus() == ItemStatus.ACTIVE) {
            throw new CoreException(new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION)
                .withMessage(String.format("Restore item failed, item %s is already Active", item.getId())).build());
        }
        item.setStatus(ItemStatus.ACTIVE);
        itemDao.update(item);
    }

    @Override
    public void update(Item item) {
        itemDao.update(item);
    }
}
