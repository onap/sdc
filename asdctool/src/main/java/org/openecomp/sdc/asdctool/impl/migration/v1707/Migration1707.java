package org.openecomp.sdc.asdctool.impl.migration.v1707;

import org.openecomp.sdc.asdctool.impl.migration.Migration1707Task;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component("migration1707")
public class Migration1707 {

    private static Logger LOGGER = LoggerFactory.getLogger(Migration1707.class);

    private List<Migration1707Task> migrations;

    public Migration1707(List<Migration1707Task> migrations) {
        this.migrations = migrations;
    }

    public boolean migrate() {
        int startMigrationFrom = Optional.ofNullable(ConfigurationManager.getConfigurationManager().getConfiguration().getStartMigrationFrom()).orElse(0);
        List<Migration1707Task> migrations = this.migrations.subList(startMigrationFrom, this.migrations.size());
        for (Migration1707Task migration : migrations) {
            LOGGER.info(String.format("Starting migration. %s", migration.description()));
            boolean migrationCompletedSuccessfully = migration.migrate();
            if (!migrationCompletedSuccessfully) {
                LOGGER.error(String.format("Migration of class %s has failed.", migration.getClass()));
                return false;
            }
            LOGGER.info(String.format("Completed migration. %s", migration.description()));
        }
        return true;
    }

}
