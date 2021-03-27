/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
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

    private static Option buildEnforceAllOption() {
        return Option.builder("e").longOpt("enforceAll").desc("enforce running all migration steps for current version").build();
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

    @Override
    protected Class<?> getSpringConfigurationClass() {
        return MigrationSpringConfig.class;
    }
}
