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
package org.openecomp.conflicts;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.openecomp.conflicts.dao.ConflictsDaoFactory;
import org.openecomp.conflicts.impl.VspMergeHandler;
import org.openecomp.sdc.datatypes.model.ItemType;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspMergeDaoFactory;
import org.openecomp.sdc.versioning.AsdcItemManagerFactory;
import org.openecomp.sdc.versioning.errors.EntityNotExistErrorBuilder;
import org.openecomp.sdc.versioning.types.Item;

public class ItemMergeHandlerFactoryImpl extends ItemMergeHandlerFactory {

    // TODO: 11/1/2017 read this map from configuration, move Vsp merge handler to vsp lib, rearrange lib deps
    private static final Map<ItemType, ItemMergeHandler> MERGE_HANDLER_BY_ITEM_TYPE = new HashMap<>();

    static {
        MERGE_HANDLER_BY_ITEM_TYPE.put(ItemType.vsp,
            new VspMergeHandler(ConflictsDaoFactory.getInstance().createInterface(), VspMergeDaoFactory.getInstance().createInterface()));
    }

    @Override
    public Optional<ItemMergeHandler> createInterface(String itemId) {
        Item item = AsdcItemManagerFactory.getInstance().createInterface().get(itemId);
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
