package org.openecomp.core.tools.Commands;

import com.amdocs.zusammen.datatypes.SessionContext;
import org.apache.commons.io.FileUtils;
import org.openecomp.core.tools.Commands.importdata.TreeWalker;
import org.openecomp.core.tools.Commands.exportdata.ImportProperties;
import org.openecomp.core.tools.util.ZipUtils;
import org.openecomp.core.zusammen.impl.CassandraConnectionInitializer;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ImportCommand {

    private static final Logger logger = LoggerFactory.getLogger(ImportCommand.class);


    public static void importData(SessionContext context, String zippedFile, String filterItem) {
        try {
            ImportProperties.initParams();
            CassandraConnectionInitializer.setCassandraConnectionPropertiesToSystem();
            if (zippedFile == null){
                logger.error("Import must have a valid file as an input.");
            }
            if (zippedFile != null) {
                zippedFile = zippedFile.replaceAll("\\r", "");
                if (filterItem != null) {
                    filterItem = filterItem.replaceAll("\\r", "");
                }
                Path rootDir = Paths.get(ImportProperties.ROOT_DIRECTORY);
                ExportDataCommand.initDir(rootDir);
                ZipUtils.unzip(Paths.get(zippedFile), rootDir);
                TreeWalker.walkFiles(context, rootDir, filterItem);
                FileUtils.forceDelete(rootDir.toFile()); // clear all unzip data at the end.
            }


        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

}
