package org.openecomp.sdc.asdctool.migration.core.task;

public class MigrationResult {

    private String msg;
    private MigrationStatus migrationStatus;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public MigrationStatus getMigrationStatus() {
        return migrationStatus;
    }

    public void setMigrationStatus(MigrationStatus migrationStatus) {
        this.migrationStatus = migrationStatus;
    }

    public enum MigrationStatus {
        COMPLETED,
        COMPLETED_WITH_ERRORS,
        FAILED
    }


}
