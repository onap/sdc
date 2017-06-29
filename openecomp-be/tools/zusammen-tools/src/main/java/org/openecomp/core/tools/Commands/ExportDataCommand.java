package org.openecomp.core.tools.Commands;

import com.amdocs.zusammen.datatypes.SessionContext;
import org.apache.commons.io.FileUtils;
import org.openecomp.core.tools.Commands.exportdata.ElementHandler;
import org.openecomp.core.tools.Commands.exportdata.ImportProperties;
import org.openecomp.core.tools.Commands.exportdata.ItemHandler;
import org.openecomp.core.tools.Commands.exportdata.VersionHandler;
import org.openecomp.core.tools.util.ZipUtils;
import org.openecomp.core.zusammen.impl.CassandraConnectionInitializer;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.nio.file.Files.createDirectories;
public class ExportDataCommand {
    private static final Logger logger = LoggerFactory.getLogger(ExportDataCommand.class);

    public static void exportData(SessionContext context, String filterItem) {
        try {
            ImportProperties.initParams();
            CassandraConnectionInitializer.setCassandraConnectionPropertiesToSystem();
            Path rootDir = Paths.get(ImportProperties.ROOT_DIRECTORY);
            initDir(rootDir);
            if (filterItem != null) {
                filterItem = filterItem.replaceAll("\\r", "");
            }
            new ItemHandler().createItemsData(context, filterItem);
            new VersionHandler().loadVersions(filterItem);
            new ElementHandler().loadElements(filterItem);
            zipPath(rootDir,filterItem);
            FileUtils.forceDelete(rootDir.toFile());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            ex.printStackTrace();
        }

    }
    private static void zipPath(Path rootDir,String filterItem ) throws Exception{
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        String dateStr = date.format(formatter);
        String zipFile = System.getProperty("user.home")+ File.separatorChar+"onboarding_import"+ dateStr + ".zip";
        ZipUtils.createZip(zipFile, rootDir,filterItem);
    }


    public static void initDir(Path rootDir ) throws IOException{
        if (Files.exists(rootDir)) {
            FileUtils.forceDelete(rootDir.toFile());
        }
        createDirectories(rootDir);
    }

}