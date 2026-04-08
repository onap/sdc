package org.openecomp.core.utilities.applicationconfig.dao;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface ApplicationConfigMapper {
    @DaoFactory
    ApplicationConfigDaoAccessor applicationConfigDaoAccessor();
    }