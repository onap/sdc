package org.openecomp.sdc.asdctool.migration.service;

import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.dao.MigrationTasksDao;
import org.openecomp.sdc.be.resources.data.MigrationTaskEntry;

import java.math.BigInteger;

public class SdcRepoService {

    private MigrationTasksDao migrationTasksDao;

    public SdcRepoService(MigrationTasksDao migrationTasksDao) {
        this.migrationTasksDao = migrationTasksDao;
    }

    public DBVersion getLatestDBVersion() {
        BigInteger currentMajorVersion = DBVersion.CURRENT_VERSION.getMajor();
        BigInteger latestMinorVersion = migrationTasksDao.getLatestMinorVersion(currentMajorVersion);
        return latestMinorVersion == null ? DBVersion.from(currentMajorVersion, BigInteger.valueOf(Integer.MIN_VALUE)) : DBVersion.from(currentMajorVersion, latestMinorVersion);
    }

    public void clearTasksForCurrentMajor() {
        BigInteger currentMajorVersion = DBVersion.CURRENT_VERSION.getMajor();
        migrationTasksDao.deleteAllTasksForVersion(currentMajorVersion);
    }

    public void createMigrationTask(MigrationTaskEntry migrationTaskEntry) {
        migrationTasksDao.createMigrationTask(migrationTaskEntry);
    }



}
