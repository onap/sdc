//package org.openecomp.sdc.asdctool.migration.tasks.mig1710;//package org.openecomp.sdc.migration.tasks.mig1710;
//
//import org.openecomp.sdc.asdctool.migration.core.DBVersion;
//import org.openecomp.sdc.asdctool.migration.core.task.Migration;
//import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;
//import org.springframework.stereotype.Component;
//
//@Component
//public class ExampleMigration implements Migration {
//
//    @Override
//    public String description() {
//        return "some description";
//    }
//
//    @Override
//    public DBVersion getVersion() {
//        return DBVersion.fromString("1710.0");
//    }
//
//    @Override
//    public MigrationResult migrate() {
//        MigrationResult migrationResult = new MigrationResult();
//        migrationResult.setMigrationStatus(MigrationResult.MigrationStatus.COMPLETED);
//        return migrationResult;
//    }
//}