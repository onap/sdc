package org.openecomp.core.tools.Commands.importdata;

import com.amdocs.zusammen.datatypes.SessionContext;
import org.openecomp.core.tools.store.VersionInfoCassandraLoader;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.io.File.separator;
import static org.openecomp.core.tools.Commands.exportdata.ImportProperties.JSON_POSTFIX;
import static org.openecomp.core.tools.Commands.exportdata.ImportProperties.VERSION_INFO_FILE_PREFIX;

public class VersionInfoImport {
    private static final Logger logger = LoggerFactory.getLogger(VersionInfoImport.class);

    public void loadPath(SessionContext sessionContext, Path itemPath, String itemId) {
        try {
             Path versionInfoFilePath = Paths.get(itemPath.toString() + separator + VERSION_INFO_FILE_PREFIX
                    + itemId + JSON_POSTFIX);
            if (!Files.exists(versionInfoFilePath)) {
                return;
            }
            String versionInfoJson = new String(Files.readAllBytes(versionInfoFilePath));
            if (versionInfoJson.trim().isEmpty()) {
                return;
            }
            VersionInfoEntity versionInfoEntity = JsonUtil.json2Object(versionInfoJson, VersionInfoEntity.class);
            VersionInfoCassandraLoader versionInfoCassandraLoader = new VersionInfoCassandraLoader();
            versionInfoCassandraLoader.insertVersionInfo(versionInfoEntity);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }


}
