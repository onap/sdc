package org.openecomp.sdc.itempermissions.dao;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public interface UserPermissionsMapper {

    @DaoFactory
    UserPermissionsDao userPermissionsDao();
    
}
