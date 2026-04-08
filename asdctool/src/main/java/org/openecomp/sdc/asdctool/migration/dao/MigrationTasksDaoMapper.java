package org.openecomp.sdc.asdctool.migration.dao;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface MigrationTasksDaoMapper {
    @DaoFactory
    MigrationTasksAccessor migrationTasksAccessor();
}
