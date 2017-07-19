package org.openecomp.sdc.asdctool.migration.dao;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;

@Accessor
public interface MigrationTasksAccessor {

    @Query("SELECT minor_version FROM sdcrepository.migrationTasks WHERE major_version = :majorVersion order by minor_version desc limit 1")
    ResultSet getLatestMinorVersion(@Param("majorVersion") Long majorVersion);

    @Query("DELETE FROM sdcrepository.migrationTasks WHERE major_version = :majorVersion")
    void deleteTasksForMajorVersion(@Param("majorVersion") Long majorVersion);

}
