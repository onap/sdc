package org.openecomp.sdc.action.dao;


import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface ActionArtifactMapper {
    @DaoFactory
    ActionArtifactDaoInternal actionArtifactDao();
}