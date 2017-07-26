package org.openecomp.core.tools.Commands.importdata;

import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Item;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.ItemDao;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.ItemDaoFactory;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.openecomp.core.tools.Commands.exportdata.ImportProperties.JSON_POSTFIX;

public class ItemImport {
    private static final Logger logger = LoggerFactory.getLogger(ItemImport.class);

    public void loadPath(SessionContext sessionContext, Path itemDir, String itemName) {
        try {
            Path itemPath = Paths.get(itemDir.toString() + File.separator + itemName + JSON_POSTFIX);
            if (!Files.exists(itemPath)) {
                return;
            }
            String itemJson = new String(Files.readAllBytes(itemPath));
            if (itemJson == null || itemJson.trim().isEmpty()) {
                return;
            }
            Item item = JsonUtil.json2Object(itemJson, Item.class);
            ItemDao itemDao = getItemDao(sessionContext);
            itemDao.create(sessionContext, item.getId(), item.getInfo(), item.getCreationTime());
            logger.info("Item Created :"+item.getInfo().getName()+" , "+item.getId());
            System.out.println("Item Created :"+item.getInfo().getName()+" , "+item.getId());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            ex.printStackTrace();
        }
    }

    private ItemDao getItemDao(SessionContext context) {
        return ItemDaoFactory.getInstance().createInterface(context);
    }
}
