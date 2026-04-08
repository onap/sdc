package org.openecomp.sdc.be.dao.cassandra;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.DaoKeyspace;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface ArtifactDaoMapper {
    @DaoFactory
    ArtifactDao artifactDao(@DaoKeyspace String keyspace);
}
