package org.openecomp.sdc.asdctool.migration.core.task;


import org.openecomp.sdc.asdctool.migration.core.DBVersion;

public interface Migration {

    String description();

    DBVersion getVersion();

    MigrationResult migrate();

}
