package org.openecomp.core.tools.Commands.exportdata;

import org.openecomp.core.tools.Commands.ExportDataCommand;
import org.openecomp.core.tools.store.VersionInfoCassandraLoader;
import org.openecomp.core.tools.store.VersionCassandraLoader;

import org.openecomp.core.tools.store.zusammen.datatypes.VersionEntity;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;

import static java.io.File.separator;
import static java.nio.file.Files.*;
import static org.openecomp.core.tools.Commands.exportdata.ImportProperties.*;
import static org.openecomp.core.tools.Commands.exportdata.ImportProperties.ROOT_DIRECTORY;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class VersionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExportDataCommand.class);

    public VersionHandler() {
    }

    public void loadVersions(Set<String> filteredItem) {
        VersionCassandraLoader versionCassandraLoader = new VersionCassandraLoader();
        versionCassandraLoader.list().forEach(versionEntity -> handleVersionEntity(versionEntity,filteredItem));
        VersionInfoCassandraLoader versionInfoCassandraLoader = new VersionInfoCassandraLoader();
        versionInfoCassandraLoader.list().forEach(versionInfoEntity ->  handleVersionInfo(versionInfoEntity,filteredItem));
    }

    private void handleVersionEntity(VersionEntity versionEntity, Set<String> filteredItem) {
        try {
            String itemId = versionEntity.getItemId();
            if (!filteredItem.isEmpty()  && !filteredItem.contains(itemId)){
                return;
            }
            String versionId = versionEntity.getVersionId();
            String space = versionEntity.getSpace();
            Path versionDirectoryPath = Paths.get( ROOT_DIRECTORY + separator + itemId
                    + separator + versionId + separator + space);
            Path versionFilePath = Paths.get(versionDirectoryPath.toString() + separator + VERSION_FILE_PREFIX
                    + versionId + JSON_POSTFIX);
            if (notExists(versionDirectoryPath)) {
                createDirectories(versionDirectoryPath);
            }
            String versionJson = JsonUtil.object2Json(versionEntity);
            write(versionFilePath, versionJson.getBytes());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            ex.printStackTrace();
        }

    }

    private void handleVersionInfo(VersionInfoEntity versionInfoEntity, Set<String> filteredItem) {
        try {
            String itemId = versionInfoEntity.getEntityId();
            Path itemDirectory = Paths.get( ROOT_DIRECTORY + separator + itemId);
            Path versionInfoFilePath = Paths.get(itemDirectory.toString() + separator + VERSION_INFO_FILE_PREFIX
                    + itemId + JSON_POSTFIX);
            if (exists(itemDirectory)) {
                String versionJson = JsonUtil.object2Json(versionInfoEntity);
                write(versionInfoFilePath, versionJson.getBytes());
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            ex.printStackTrace();
        }

    }
}
