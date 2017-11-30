package org.openecomp.sdc.asdctool.migration.core.execution;

import java.util.Date;

import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
import org.openecomp.sdc.be.resources.data.MigrationTaskEntry;

public class MigrationExecutionResult {

    private MigrationResult.MigrationStatus migrationStatus;
    private String msg;
    private double executionTime;
    private DBVersion version;
    private String taskName;
    private String description;

    public MigrationTaskEntry toMigrationTaskEntry() {
        MigrationTaskEntry migrationTaskEntry = new MigrationTaskEntry();
        migrationTaskEntry.setMajorVersion(this.getVersion().getMajor().longValue());
        migrationTaskEntry.setMinorVersion(this.getVersion().getMinor().longValue());
        migrationTaskEntry.setTimestamp(new Date());
        migrationTaskEntry.setTaskName(this.getTaskName());
        migrationTaskEntry.setTaskStatus(this.getMigrationStatus().name());
        migrationTaskEntry.setMessage(this.getMsg());
        migrationTaskEntry.setExecutionTime(this.getExecutionTime());
        migrationTaskEntry.setDescription(this.getDescription());
        return migrationTaskEntry;
    }


    public MigrationResult.MigrationStatus getMigrationStatus() {
        return migrationStatus;
    }

    void setMigrationStatus(MigrationResult.MigrationStatus migrationStatus) {
        this.migrationStatus = migrationStatus;
    }

    public String getMsg() {
        return msg;
    }

    void setMsg(String msg) {
        this.msg = msg;
    }

    double getExecutionTime() {
        return executionTime;
    }

    void setExecutionTime(double executionTime) {
        this.executionTime = executionTime;
    }

    public DBVersion getVersion() {
        return version;
    }

    public void setVersion(DBVersion version) {
        this.version = version;
    }

    String getTaskName() {
        return taskName;
    }

    void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
