package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.UDTMapper;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.dao.impl.CassandraBaseDao;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDependencyModelDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.UniqueValueMetadata;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ComponentDependencyModelDaoCassandraImpl extends CassandraBaseDao
    <ComponentDependencyModelEntity> implements ComponentDependencyModelDao {

  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static final Mapper<ComponentDependencyModelEntity> mapper =
      noSqlDb.getMappingManager().mapper(ComponentDependencyModelEntity.class);
  private static final ComponentDependencyModelDaoCassandraImpl.ComponentDependencyModelAccessor
      accessor = noSqlDb.getMappingManager().createAccessor(
      ComponentDependencyModelDaoCassandraImpl.ComponentDependencyModelAccessor.class);
  private static final UDTMapper<Version> versionMapper =
      noSqlDb.getMappingManager().udtMapper(Version.class);

  @Override
  protected Mapper<ComponentDependencyModelEntity> getMapper() {
    return mapper;
  }

  @Override
  protected Object[] getKeys(ComponentDependencyModelEntity entity) {
    return new Object[]{entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
        entity.getId()};
  }

  @Override
  public Collection<ComponentDependencyModelEntity> list(ComponentDependencyModelEntity entity) {
    return accessor
        .list(entity.getVspId(), versionMapper.toUDT(entity.getVersion())).all();
  }

  @Override
  public void registerVersioning(String versionableEntityType) {
    VersionableEntityMetadata metadata = new VersionableEntityMetadata(
        mapper.getTableMetadata().getName(),
        mapper.getTableMetadata().getPartitionKey().get(0).getName(),
        mapper.getTableMetadata().getPartitionKey().get(1).getName());

    metadata.setUniqueValuesMetadata(Collections.singletonList(new UniqueValueMetadata(
        VendorSoftwareProductConstants.UniqueValues.PROCESS_NAME,
        Arrays.asList("vsp_id", "version", "component_id", "name"))));

    VersioningManagerFactory.getInstance().createInterface()
        .register(versionableEntityType, metadata);

  }

  @Accessor
  interface ComponentDependencyModelAccessor {
    @Query("delete from vsp_component_dependency_model where vsp_id=? and version=?")
    ResultSet deleteAll(String vspId, UDTValue version);

    @Query(
        "select * from vsp_component_dependency_model where vsp_id=? and version=?")
    Result<ComponentDependencyModelEntity> list(String vspId, UDTValue version);
  }
}
