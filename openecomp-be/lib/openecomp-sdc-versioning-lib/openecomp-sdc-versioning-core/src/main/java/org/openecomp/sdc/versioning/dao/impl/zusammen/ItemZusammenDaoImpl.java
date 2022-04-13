/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.versioning.dao.impl.zusammen;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.item.Info;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.versioning.dao.ItemDao;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.types.ItemStatus;

public class ItemZusammenDaoImpl implements ItemDao {

    private final ZusammenAdaptor zusammenAdaptor;

    public ItemZusammenDaoImpl(ZusammenAdaptor zusammenAdaptor) {
        this.zusammenAdaptor = zusammenAdaptor;
    }

    @Override
    public Collection<Item> list() {
        return zusammenAdaptor.listItems(ZusammenUtil.createSessionContext()).stream().map(this::mapFromZusammenItem).collect(Collectors.toList());
    }

    @Override
    public Item get(Item item) {
        return mapFromZusammenItem(zusammenAdaptor.getItem(ZusammenUtil.createSessionContext(), new Id(item.getId())));
    }

    @Override
    public Item create(Item item) {
        Id itemId = zusammenAdaptor.createItem(ZusammenUtil.createSessionContext(), mapToZusammenItemInfo(item));
        item.setId(itemId.getValue());
        return item;
    }

    @Override
    public void delete(Item item) {
        zusammenAdaptor.deleteItem(ZusammenUtil.createSessionContext(), new Id(item.getId()));
    }

    @Override
    public void update(Item item) {
        zusammenAdaptor.updateItem(ZusammenUtil.createSessionContext(), new Id(item.getId()), mapToZusammenItemInfo(item));
    }

    private Item mapFromZusammenItem(com.amdocs.zusammen.datatypes.item.Item zusammenItem) {
        if (zusammenItem == null) {
            return null;
        }
        Item item = new Item();
        item.setId(zusammenItem.getId().getValue());
        item.setName(zusammenItem.getInfo().getName());
        item.setDescription(zusammenItem.getInfo().getDescription());
        zusammenItem.getInfo().getProperties().forEach((key, value) -> addPropertyToItem(key, value, item));
        item.setCreationTime(zusammenItem.getCreationTime());
        item.setModificationTime(zusammenItem.getModificationTime());
        if (item.getStatus() == null) {
            item.setStatus(ItemStatus.ACTIVE);
            update(item);
        }
        return item;
    }

    private void addPropertyToItem(String propertyKey, Object propertyValue, Item item) {
        final ItemInfoProperty itemInfoProperty = ItemInfoProperty.findByName(propertyKey).orElse(null);
        if (itemInfoProperty == null) {
            item.addProperty(propertyKey, propertyValue);
            return;
        }

        switch (itemInfoProperty) {
            case ITEM_TYPE:
                item.setType((String) propertyValue);
                break;
            case ITEM_OWNER:
                item.setOwner((String) propertyValue);
                break;
            case ITEM_STATUS:
                item.setStatus(ItemStatus.valueOf((String) propertyValue));
                break;
            case ITEM_VERSIONS_STATUSES:
                for (Map.Entry<String, Number> statusCounter : ((Map<String, Number>) propertyValue).entrySet()) {
                    item.getVersionStatusCounters().put(VersionStatus.valueOf(statusCounter.getKey()), statusCounter.getValue().intValue());
                }
                break;
            default:
                item.addProperty(propertyKey, propertyValue);
        }
    }

    private Info mapToZusammenItemInfo(Item item) {
        Info info = new Info();
        info.setName(item.getName());
        info.setDescription(item.getDescription());
        info.addProperty(ItemInfoProperty.ITEM_TYPE.getName(), item.getType());
        info.addProperty(ItemInfoProperty.ITEM_OWNER.getName(), item.getOwner());
        if (item.getStatus() != null) {
            info.addProperty(ItemInfoProperty.ITEM_STATUS.getName(), item.getStatus());
        }
        info.addProperty(ItemInfoProperty.ITEM_VERSIONS_STATUSES.getName(), item.getVersionStatusCounters());
        item.getProperties().forEach(info::addProperty);
        return info;
    }

    @AllArgsConstructor
    @Getter
    public enum ItemInfoProperty {
        ITEM_TYPE("item_type"),
        ITEM_VERSIONS_STATUSES("item_versions_statuses"),
        ITEM_OWNER("Owner"),
        ITEM_STATUS("status");

        private final String name;

        public static Optional<ItemInfoProperty> findByName(final String name) {
            return Arrays.stream(values()).filter(itemInfoProperty -> itemInfoProperty.getName().equals(name)).findFirst();
        }

    }
}
