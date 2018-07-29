package org.openecomp.sdc.asdctool.migration.main;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.openecomp.sdc.asdctool.cli.CLIToolData;
import org.openecomp.sdc.asdctool.cli.SpringCLITool;
import org.openecomp.sdc.asdctool.migration.config.MigrationSpringConfig;
import org.openecomp.sdc.asdctool.migration.core.SdcMigrationTool;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.context.support.AbstractApplicationContext;

public class MigrationMenu extends SpringCLITool {

    private static final Logger LOGGER = Logger.getLogger(MigrationMenu.class);

    public static void main(String[] args) {
        MigrationMenu migrationMenu = new MigrationMenu();
        CLIToolData cliToolData = migrationMenu.init(args);
        boolean enforceAll = cliToolData.getCommandLine().hasOption("e");
        migrationMenu.doMigrate(enforceAll, cliToolData.getSpringApplicationContext());
    }

    private void doMigrate(boolean enforceAll, AbstractApplicationContext context) {
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

    @Override
    protected Options buildCmdLineOptions() {
        Options options = super.buildCmdLineOptions();
        Option enforceAll = buildEnforceAllOption();
        options.addOption(enforceAll);
        return options;
    }

    @Override
    protected String commandName() {
        return "sdc-migration";
    }

    private static Option buildEnforceAllOption() {
        return Option.builder("e")
                .longOpt("enforceAll")
                .desc("enforce running all migration steps for current version")
                .build();
    }

    @Override
    protected Class<?> getSpringConfigurationClass() {
        return MigrationSpringConfig.class;
    }
}
