package org.openecomp.sdc.asdctool.migration;


import org.openecomp.sdc.asdctool.migration.core.DBVersion;
import org.openecomp.sdc.asdctool.migration.core.task.Migration;
import org.openecomp.sdc.asdctool.migration.core.task.MigrationResult;

public class DummyMigrationFactory {

    public static Migration SUCCESSFUL_MIGRATION = new Migration() {
        @Override
        public String description() {
            return "success mig";
        }

        @Override
        public DBVersion getVersion() {
            return DBVersion.fromString("1710.22");
        }

        @Override
        public MigrationResult migrate() {
            MigrationResult migrationResult = new MigrationResult();
            migrationResult.setMigrationStatus(MigrationResult.MigrationStatus.COMPLETED);
            migrationResult.setMsg("myMsg");
            return migrationResult;
        }
    };

    public static Migration FAILED_MIGRATION = new Migration() {
        @Override
        public String description() {
            return "failed mig";
        }

        @Override
        public DBVersion getVersion() {
            return DBVersion.fromString("1710.22");
        }

        @Override
        public MigrationResult migrate() {
            MigrationResult migrationResult = new MigrationResult();
            migrationResult.setMigrationStatus(MigrationResult.MigrationStatus.FAILED);
            migrationResult.setMsg("myMsg");
            return migrationResult;
        }
    };

    public static Migration getMigration(String version, MigrationResult.MigrationStatus status) {
        return new Migration() {
            @Override
            public String description() {
                return "success mig";
            }

            @Override
            public DBVersion getVersion() {
                return DBVersion.fromString(version);
            }

            @Override
            public MigrationResult migrate() {
                MigrationResult migrationResult = new MigrationResult();
                migrationResult.setMigrationStatus(status);
                migrationResult.setMsg("myMsg");
                return migrationResult;
            }
        };
    }

}
