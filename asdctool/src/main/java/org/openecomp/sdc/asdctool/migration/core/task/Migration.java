package org.openecomp.sdc.asdctool.migration.core.task;

public interface Migration extends IMigrationStage{
	@Override
	default
    AspectMigrationEnum getAspectMigration(){
    	return AspectMigrationEnum.MIGRATION;
    }

}
