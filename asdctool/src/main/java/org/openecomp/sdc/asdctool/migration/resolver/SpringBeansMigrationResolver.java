package org.openecomp.sdc.asdctool.migration.resolver;


import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.service.SdcRepoService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SpringBeansMigrationResolver implements MigrationResolver {

    private List<Migration> migrations = new ArrayList<>();

    private SdcRepoService sdcRepoService;

    public SpringBeansMigrationResolver(List<Migration> migrations, SdcRepoService sdcRepoService) {
        this.migrations = migrations;
        this.sdcRepoService = sdcRepoService;
    }

    @Override
    public List<Migration> resolveMigrations() {
        migrations.sort(Comparator.comparing(Migration::getVersion));
        return resolveNonExecutedMigrations();
    }

    //package private for testing
    void setMigrations(List<Migration> migrations) {
        this.migrations = migrations;
    }

    private List<Migration> resolveNonExecutedMigrations() {
        DBVersion latestDBVersion = sdcRepoService.getLatestDBVersion();
        return migrations.stream()
                .filter(mig -> isMigrationVersionGreaterThanLatestVersion(latestDBVersion, mig))
                .collect(Collectors.toList());
    }

    private boolean isMigrationVersionGreaterThanLatestVersion(DBVersion latestDBVersion, Migration mig) {
        return mig.getVersion().compareTo(latestDBVersion) > 0;
    }
}
