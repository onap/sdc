package org.openecomp.sdc.asdctool.migration.main;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.openecomp.sdc.asdctool.configuration.ConfigurationUploader;
import org.openecomp.sdc.asdctool.migration.config.MigrationSpringConfig;
import org.openecomp.sdc.asdctool.migration.core.SdcMigrationTool;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MigrationMenu {

    private final static Logger LOGGER = LoggerFactory.getLogger(MigrationMenu.class);

    public static void main(String[] args) {
        CommandLine commandLine = initCmdLineOptions(args);
        String appConfigDir = commandLine.getOptionValue("c");
        boolean enforceAll = commandLine.hasOption("e");
        ConfigurationUploader.uploadConfigurationFiles(appConfigDir);
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MigrationSpringConfig.class);
        doMigrate(enforceAll, context);

    }

    private static void doMigrate(boolean enforceAll, AnnotationConfigApplicationContext context) {
        SdcMigrationTool migrationTool = context.getBean(SdcMigrationTool.class);
        boolean migrate = migrationTool.migrate(enforceAll);
        if (migrate) {
            LOGGER.info("migration completed successfully");
            System.exit(0);
        } else {
            LOGGER.error("migration failed");
            System.exit(1);
        }
    }

    private static CommandLine initCmdLineOptions(String[] args) {
        Options options = buildCmdLineOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            return parser.parse( options, args );
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
            usageAndExit(options);
        }
        return null;
    }

    private static void usageAndExit(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "yy", options );
        System.exit(1);
    }

    private static Options buildCmdLineOptions() {
        Option configPath = buildConfigPathOption();

        Option enforceAll = buildEnforceAllOption();

        Options options = new Options();
        options.addOption(configPath);
        options.addOption(enforceAll);
        return options;
    }

    private static Option buildEnforceAllOption() {
        return Option.builder("e")
                .longOpt("enforceAll")
                .desc("enforce running all migration steps for current version")
                .build();
    }

    private static Option buildConfigPathOption() {
        return Option.builder("c")
                    .longOpt("configFolderPath")
                    .required()
                    .hasArg()
                    .desc("path to sdc configuration folder - required")
                    .build();
    }

}
