package org.openecomp.sdc.asdctool.migration.core.task;

import org.openecomp.sdc.asdctool.migration.core.DBVersion;

public interface IMigrationStage {
	
	String description();

    DBVersion getVersion();
    
    MigrationResult migrate();
    
    AspectMigrationEnum getAspectMigration();
    
	public enum AspectMigrationEnum {
		BEFORE_MIGRATION,
		MIGRATION,
		AFTER_MIGRATION;
	}
}
