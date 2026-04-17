package org.openecomp.sdc.activitylog.dao;


import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.DaoKeyspace;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface ActivityLogMapper {
    @DaoFactory
    ActivityLogDaoInternal activityLogDao(@DaoKeyspace String keyspace);
}