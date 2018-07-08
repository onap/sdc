package org.openecomp.core.tools.importinfo;


import static org.openecomp.core.tools.commands.CommandName.IMPORT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.io.FileUtils;
import org.openecomp.core.tools.commands.Command;
import org.openecomp.core.tools.commands.CommandName;
import org.openecomp.core.tools.exportinfo.ExportDataCommand;
import org.openecomp.core.tools.util.Utils;
import org.openecomp.core.tools.util.ZipUtils;
import org.openecomp.core.zusammen.impl.CassandraConnectionInitializer;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

public class ImportDataCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportDataCommand.class);
    private static final String FILE_OPTION = "f";

    public ImportDataCommand() {
        options.addOption(Option.builder(FILE_OPTION).hasArg().argName("file").desc("export file (zip), mandatory").build());
    }

    @Override
    public boolean execute(String[] args) {
        CommandLine cmd = parseArgs(args);

        if (!cmd.hasOption(FILE_OPTION) || cmd.getOptionValue(FILE_OPTION) == null) {
            LOGGER.error("Argument f is mandatory");
            return false;
        }
        try {
            CassandraConnectionInitializer.setCassandraConnectionPropertiesToSystem();
            Path outputFolder = Paths.get(ImportProperties.ROOT_DIRECTORY);
            ExportDataCommand.initDir(outputFolder); //clear old imports.
            ZipUtils.unzip(Paths.get(cmd.getOptionValue(FILE_OPTION)), outputFolder);
            try (Stream<Path> files = Files.list(outputFolder)) {
                files.forEach(new ImportSingleTable()::importFile);
            }
            FileUtils.forceDelete(outputFolder.toFile()); // leaves directory clean
        } catch (IOException e) {
            Utils.logError(LOGGER, e);
        }
        return true;
    }

    @Override
    public CommandName getCommandName() {
        return IMPORT;
    }
}
