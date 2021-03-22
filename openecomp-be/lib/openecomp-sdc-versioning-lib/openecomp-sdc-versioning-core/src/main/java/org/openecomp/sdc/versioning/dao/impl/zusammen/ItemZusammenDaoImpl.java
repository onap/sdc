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
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.versioning.dao.ItemDao;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.types.ItemStatus;

public class ItemZusammenDaoImpl implements ItemDao {

    private ZusammenAdaptor zusammenAdaptor;

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
        zusammenItem.getInfo().getProperties().entrySet().forEach(property -> addPropertyToItem(property.getKey(), property.getValue(), item));
        item.setCreationTime(zusammenItem.getCreationTime());
        item.setModificationTime(zusammenItem.getModificationTime());
        if (item.getStatus() == null) {
            item.setStatus(ItemStatus.ACTIVE);
            update(item);
        }
        return item;
    }

    private void addPropertyToItem(String propertyKey, Object propertyValue, Item item) {
        switch (propertyKey) {
            case InfoPropertyName.ITEM_TYPE:
                item.setType((String) propertyValue);
                break;
            case InfoPropertyName.ITEM_OWNER:
                item.setOwner((String) propertyValue);
                break;
            case InfoPropertyName.ITEM_STATUS:
                item.setStatus(ItemStatus.valueOf((String) propertyValue));
                break;
            case InfoPropertyName.ITEM_VERSIONS_STATUSES:
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
        info.addProperty(InfoPropertyName.ITEM_TYPE, item.getType());
        info.addProperty(InfoPropertyName.ITEM_OWNER, item.getOwner());
        if (item.getStatus() != null) {
            info.addProperty(InfoPropertyName.ITEM_STATUS, item.getStatus());
        }
        info.addProperty(InfoPropertyName.ITEM_VERSIONS_STATUSES, item.getVersionStatusCounters());
        item.getProperties().entrySet().forEach(property -> info.addProperty(property.getKey(), property.getValue()));
        return info;
    }

    private static final class InfoPropertyName {

        private static final String ITEM_TYPE = "item_type";
        private static final String ITEM_VERSIONS_STATUSES = "item_versions_statuses";
        private static final String ITEM_OWNER = "Owner";
        private static final String ITEM_STATUS = "status";

        private InfoPropertyName() {
            throw new IllegalStateException("Constants class");
        }
    }
}
