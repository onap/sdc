package org.openecomp.sdc.asdctool.migration.resolver;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.IMigrationStage;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.PostMigration;
import org.openecomp.sdc.asdctool.migration.service.SdcRepoService;

public class SpringBeansMigrationResolver implements MigrationResolver {

    private List<Migration> migrations = new ArrayList<>();
    private List<PostMigration> postMigrations = new ArrayList<>();
    
    private SdcRepoService sdcRepoService;

    public SpringBeansMigrationResolver(List<Migration> migrations, List<PostMigration> postMigrations, SdcRepoService sdcRepoService) {
        this.migrations = migrations;
        this.postMigrations = postMigrations;
        this.sdcRepoService = sdcRepoService;
    }

    @Override
    public List<IMigrationStage> resolveMigrations() {
        migrations.sort(Comparator.comparing(Migration::getVersion));
        List<IMigrationStage> allTasks = resolveNonExecutedMigrations();
        allTasks.addAll(postMigrations);
        return allTasks;
    }

    //package private for testing
    void setMigrations(List<Migration> migrations) {
        this.migrations = migrations;
    }
    
    //package private for testing
    void setPostMigrations(List<PostMigration> postMigrations) {
        this.postMigrations = postMigrations;
    }

    private List<IMigrationStage> resolveNonExecutedMigrations() {
        DBVersion latestDBVersion = sdcRepoService.getLatestDBVersion();
        return migrations.stream()
                .filter(mig -> isMigrationVersionGreaterThanLatestVersion(latestDBVersion, mig))
                .collect(Collectors.toList());
    }

    private boolean isMigrationVersionGreaterThanLatestVersion(DBVersion latestDBVersion, Migration mig) {
        return mig.getVersion().compareTo(latestDBVersion) > 0;
    }
}
