package org.openecomp.sdc.asdctool.migration.core.task;

import org.openecomp.sdc.asdctool.migration.core.DBVersion;

public interface PostMigration extends IMigrationStage {
  
	@Override
	default
	public DBVersion getVersion() {
		return DBVersion.CURRENT_VERSION;
	}
	
	@Override
	default
    AspectMigrationEnum getAspectMigration(){
    	return AspectMigrationEnum.AFTER_MIGRATION;
    }

}
