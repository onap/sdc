package org.openecomp.sdc.action.dao;

import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;

/**
 * Root mapper interface required by Datastax 4.x.
 *
 * In driver 4.x, all DAOs must be created through a @Mapper interface.
 * The mapper generates the implementation at build time and provides
 * factory methods (@DaoFactory) to obtain DAO instances.
 */

@Mapper
public interface ActionMapper {
    @DaoFactory
    ActionDaoMapper actionDaoMapper();
}
