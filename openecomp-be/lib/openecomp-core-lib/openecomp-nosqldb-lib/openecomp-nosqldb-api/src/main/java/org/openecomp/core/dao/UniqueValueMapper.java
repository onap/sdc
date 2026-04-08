package org.openecomp.core.dao;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface UniqueValueMapper {
    @DaoFactory
    UniqueValueDao uniqueValueDao();
}
