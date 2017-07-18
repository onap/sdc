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
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.UniqueValueMetadata;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ComputeDaoCassandraImpl extends CassandraBaseDao<ComputeEntity> implements
    ComputeDao {

  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static final Mapper<ComputeEntity> mapper =
      noSqlDb.getMappingManager().mapper(ComputeEntity.class);
  private static final ComputeAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(ComputeDaoCassandraImpl.ComputeAccessor.class);
  private static final UDTMapper<Version> versionMapper =
      noSqlDb.getMappingManager().udtMapper(Version.class);

  @Override
  public void registerVersioning(String versionableEntityType) {
    VersionableEntityMetadata metadata = new VersionableEntityMetadata(
        mapper.getTableMetadata().getName(),
        mapper.getTableMetadata().getPartitionKey().get(0).getName(),
        mapper.getTableMetadata().getPartitionKey().get(1).getName());


    metadata.setUniqueValuesMetadata(Collections.singletonList(new UniqueValueMetadata(
        VendorSoftwareProductConstants.UniqueValues.COMPUTE_NAME,
        Arrays.asList("vsp_id", "version", "component_id", "name"))));

    VersioningManagerFactory.getInstance().createInterface()
        .register(versionableEntityType, metadata);
  }

  @Override
  public Collection<ComputeEntity> list(ComputeEntity entity) {
    return accessor.listByComponentId(entity.getVspId(),
        versionMapper.toUDT(entity.getVersion()), entity.getComponentId()).all();
  }

  @Override
  protected Mapper<ComputeEntity> getMapper() {
    return mapper;
  }

  @Override
  protected Object[] getKeys(ComputeEntity entity) {
    return new Object[]{entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
        entity.getComponentId(), entity.getId()};
  }

  @Override
  public Collection<ComputeEntity> listByVsp(String vspId, Version version) {
    return accessor.listByVspId(vspId, versionMapper.toUDT(version)).all();
  }

  @Override
  public void update(ComputeEntity entity) {
    accessor.updateCompositionData(entity.getCompositionData(), entity.getVspId(), versionMapper
            .toUDT(entity.getVersion()), entity.getComponentId(), entity.getId());
  }

  @Override
  public void updateQuestionnaireData(String vspId, Version version, String componentId,
                                      String computeId, String questionnaireData) {
    accessor.updateQuestionnaireData(questionnaireData, vspId, versionMapper.toUDT(version),
        componentId, computeId);
  }

  @Override
  public void deleteAll(String vspId, Version version) {
    accessor.deleteAll(vspId, version);
  }

  @Override
  public ComputeEntity getQuestionnaireData(String vspId, Version version, String componentId,
                                            String computeId) {
    return null;
  }

  @Accessor
  interface ComputeAccessor {

    @Query("select * from vsp_component_compute where vsp_id=? and version=? and component_id=?")
    Result<ComputeEntity> listByComponentId(String vspId, UDTValue version, String componentId);

    @Query("select * from vsp_component_compute where vsp_id=? and version=?")
    Result<ComputeEntity> listByVspId(String vspId, UDTValue version);

    @Query("update vsp_component_compute set composition_data=? where vsp_id=? and version=?"
        + " and component_id=? and compute_id=?")
    ResultSet updateCompositionData(String compositionData, String vspId, UDTValue version,
                                      String componentId, String computeId);

    @Query("update vsp_component_compute set questionnaire_data=? where vsp_id=? and version=?"
            + " and component_id=? and compute_id=?")
    ResultSet updateQuestionnaireData(String questionnaireData, String vspId, UDTValue version,
                                      String componentId, String computeId);

    @Query("delete from vsp_component_compute where vsp_id=? and version=?")
    ResultSet deleteAll(String vspId, Version version);
  }
}
