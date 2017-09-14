package org.openecomp.core.tools.Commands.importdata;

import com.amdocs.zusammen.datatypes.SessionContext;
import org.openecomp.core.tools.store.VersionCassandraLoader;
import org.openecomp.core.tools.store.zusammen.datatypes.VersionEntity;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.io.File.separator;
import static org.openecomp.core.tools.Commands.exportdata.ImportProperties.JSON_POSTFIX;
import static org.openecomp.core.tools.Commands.exportdata.ImportProperties.VERSION_FILE_PREFIX;

public class VersionImport {
    private static final Logger logger = LoggerFactory.getLogger(VersionImport.class);

    public void loadPath(SessionContext sessionContext, Path versionDir , String versionId){
        try {
            Path versionPath = Paths.get(versionDir.toString() + separator + VERSION_FILE_PREFIX
                    + versionId + JSON_POSTFIX);
            if (!Files.exists(versionPath)) {
                return;
            }
            String versionJson = new String(Files.readAllBytes(versionPath));
            if (versionJson.trim().isEmpty()) {
                return;
            }
            VersionEntity versionEntity = JsonUtil.json2Object(versionJson, VersionEntity.class);
            VersionCassandraLoader versionCassandraLoader = new VersionCassandraLoader();
            versionCassandraLoader.insertVersion(versionEntity);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }


}
