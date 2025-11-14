package org.openecomp.sdc.action.dao;

import java.util.List;
import java.util.Optional;

import org.openecomp.sdc.action.dao.types.ActionEntity;
import org.openecomp.sdc.action.dao.types.OpenEcompComponentEntity;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import org.openecomp.sdc.versioning.dao.types.Version;

/**
 * DAO interface required by the Datastax 4.x Object Mapper.
 * 
 * The old driver 3.x MappingManager automatically generated DAOs at runtime,
 * but driver 4.x requires explicitly defined @Dao interfaces for all queries.
 * This class declares the CQL operations the mapper will implement.
 */

@Dao
public interface ActionDaoMapper {

        @Select
        List<ActionEntity> getAllActions();

        @Select(customWhereClause = "actioninvariantuuid = :actionInvariantUuId AND version IN :versions")
        List<ActionEntity> getActionsByInvId(String actionInvariantUuId, List<Version> versions);

        @Select(customWhereClause = "supportedModels CONTAINS :resource")
        List<ActionEntity> getActionsByModel(String resource);

        @Select(customWhereClause = "supportedComponents CONTAINS :resource")
        List<ActionEntity> getActionsByOpenEcompComponent(String resource);

        @Select(customWhereClause = "vendor_list CONTAINS :vendor")
        List<ActionEntity> getActionsByVendor(String vendor);

        @Select(customWhereClause = "category_list CONTAINS :vendor")
        List<ActionEntity> getActionsByCategory(String vendor);

        @Select(customWhereClause = "name = :name")
        List<ActionEntity> getInvIdByName(String name);

        @Select
        List<OpenEcompComponentEntity> getOpenEcompComponents();

        @Select(customWhereClause = "actionUUID = :actionUUID")
        Optional<ActionEntity> actionInvariantUuId(String actionUUID);

        // helper to get by pk (actioninvariantuuid + version)
        @Select(customWhereClause = "actioninvariantuuid = :actionInvariantUuId AND version = :version")
        ActionEntity getById(String actionInvariantUuId, Version version);

        @Query("INSERT INTO dox.\"action\" (actioninvariantuuid, version, name, actionuuid, user, timestamp, status) " + "VALUES (:actionInvariantUuId, :version, :name, :actionUuId, :user, :timestamp, :status)")
        void addAction(ActionEntity entity);
}
