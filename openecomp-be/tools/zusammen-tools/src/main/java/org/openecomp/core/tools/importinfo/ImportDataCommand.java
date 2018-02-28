package org.openecomp.core.tools.importinfo;


import org.apache.commons.io.FileUtils;
import org.openecomp.core.tools.exportinfo.ExportDataCommand;
import org.openecomp.core.tools.util.Utils;
import org.openecomp.core.tools.util.ZipUtils;
import org.openecomp.core.zusammen.impl.CassandraConnectionInitializer;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class ImportDataCommand {

    private static final Logger logger = LoggerFactory.getLogger(ImportDataCommand.class);

    public static void execute(String uploadFile) {
        try {
            CassandraConnectionInitializer.setCassandraConnectionPropertiesToSystem();
            Path outputFolder = Paths.get(ImportProperties.ROOT_DIRECTORY);
            ExportDataCommand.initDir(outputFolder); //clear old imports.
            ZipUtils.unzip(Paths.get(uploadFile), outputFolder);
            try( Stream<Path> files = Files.list(outputFolder)) {
                files.forEach(new ImportSingleTable()::importFile);
            }
            FileUtils.forceDelete(outputFolder.toFile()); // leaves directory clean
        } catch (IOException e) {
            Utils.logError(logger, e);
        }
    }
}
