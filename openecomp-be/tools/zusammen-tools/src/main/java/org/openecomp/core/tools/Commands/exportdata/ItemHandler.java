package org.openecomp.core.tools.Commands.exportdata;

import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Item;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.ItemDao;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.ItemDaoFactory;
import org.openecomp.core.tools.Commands.ExportDataCommand;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.file.Files.*;
import static java.io.File.separator;
import static org.openecomp.core.tools.Commands.exportdata.ImportProperties.*;

public class ItemHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExportDataCommand.class);


    public ItemHandler() {
        ImportProperties.initParams();
    }

    public void createItemsData(SessionContext context, String filteredItem) throws URISyntaxException, IOException {

        List<Item> items = getItemDao(context).list(context);
        items.parallelStream().forEach(item -> createItemDirectoryAndFiles(item,filteredItem));

    }

    private final void createItemDirectoryAndFiles(Item item,String filteredItem) {
        try {
            String itemId = item.getId().getValue();
            if (filteredItem != null && !itemId.contains(filteredItem)){
                return;
            }
            Path itemPath = Paths.get( ImportProperties.ROOT_DIRECTORY + separator + itemId);
            Path itemFilePath = Paths.get( ImportProperties.ROOT_DIRECTORY + separator +
                    itemId + separator + itemId + JSON_POSTFIX);
            if (notExists(itemPath)) {
                createDirectories(itemPath);
            }
            String itemJson = JsonUtil.object2Json(item);
            write(itemFilePath, itemJson.getBytes());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            ex.printStackTrace();
        }

    }


    private ItemDao getItemDao(SessionContext context) {
        return ItemDaoFactory.getInstance().createInterface(context);
    }
}
