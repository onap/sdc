package org.openecomp.sdc.action.dao;

import java.util.Optional;

import org.openecomp.sdc.action.dao.types.ActionArtifactEntity;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Update;

@Dao
public interface ActionArtifactDaoInternal {
    @Select(customWhereClause = "effective_version <= :effectiveVersion AND artifactuuid = :artifactUuId LIMIT 1")
    Optional<ActionArtifactEntity> getArtifactByUuId(int effectiveVersion, String artifactUuId);

    @Insert
    void addArtifact(ActionArtifactEntity entity);

    @Update
    void updateArtifact(ActionArtifactEntity entity);
    }