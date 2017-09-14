package org.openecomp.core.tools.Commands.exportdata;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Item;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.ItemDao;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.ItemDaoFactory;
import com.google.common.base.Strings;
import org.openecomp.core.tools.Commands.ExportDataCommand;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.io.File.separator;
import static java.nio.file.Files.*;
import static org.openecomp.core.tools.Commands.exportdata.ImportProperties.JSON_POSTFIX;

public class ItemHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExportDataCommand.class);


    public ItemHandler() {
        ImportProperties.initParams();
    }

    public void createItemsData(SessionContext context, Set<String> filteredItem) throws URISyntaxException, IOException {
        final List<Item> items = new ArrayList<>();
        if (filteredItem.isEmpty()) {
            items.addAll(getItemDao(context).list(context));
        } else {
            Optional<Item> item = addItem(context, filteredItem.iterator().next(), items);
            item.ifPresent(foundedItem -> {
                Object vendorId =  foundedItem.getInfo().getProperty("vendorId");
                if (vendorId != null) {
                    String vendorIdStr = vendorId.toString();
                    addItem(context, vendorIdStr, items);
                    filteredItem.add(vendorIdStr);
                }
            });
        }

        items.parallelStream().forEach(item -> createItemDirectoryAndFiles(item, filteredItem));

    }



    private Optional<Item> addItem(SessionContext context, String filteredItem, List<Item> items) {
        Optional<Item> item = getItemDao(context).get(context, new Id(filteredItem));
        item.ifPresent(itemData -> items.add(itemData));
        return item;
    }

    private final void createItemDirectoryAndFiles(Item item, Set<String> filteredItem) {
        try {
            String itemId = item.getId().getValue();
            if (!filteredItem.isEmpty() && !filteredItem.contains(itemId)) {
                return;
            }
            Path itemPath = Paths.get(ImportProperties.ROOT_DIRECTORY + separator + itemId);
            Path itemFilePath = Paths.get(ImportProperties.ROOT_DIRECTORY + separator +
                    itemId + separator + itemId + JSON_POSTFIX);
            if (notExists(itemPath)) {
                createDirectories(itemPath);
            }
            String itemJson = JsonUtil.object2Json(item);
            write(itemFilePath, itemJson.getBytes());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

    }


    private ItemDao getItemDao(SessionContext context) {
        return ItemDaoFactory.getInstance().createInterface(context);
    }
}
