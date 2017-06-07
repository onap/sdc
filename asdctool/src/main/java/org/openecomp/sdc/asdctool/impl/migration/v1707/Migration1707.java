package org.openecomp.sdc.asdctool.impl.migration.v1707;

import org.openecomp.sdc.asdctool.impl.migration.Migration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component("migration1707")
public class Migration1707 {

    private static Logger LOGGER = LoggerFactory.getLogger(Migration1707.class);

    private List<Migration> migrations;

    public Migration1707(List<Migration> migrations) {
        this.migrations = migrations;
    }

    public boolean migrate() {
        for (Migration migration : migrations) {
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
