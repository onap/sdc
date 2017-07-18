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
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.UniqueValueMetadata;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ImageDaoImpl extends CassandraBaseDao<ImageEntity> implements ImageDao {

  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static final Mapper<ImageEntity> mapper =
      noSqlDb.getMappingManager().mapper(ImageEntity.class);
  private static final ImageAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(ImageAccessor.class);
  private static final UDTMapper<Version> versionMapper =
      noSqlDb.getMappingManager().udtMapper(Version.class);

  @Override
  public void registerVersioning(String versionableEntityType) {

    VersionableEntityMetadata metadata = new VersionableEntityMetadata(
        mapper.getTableMetadata().getName(),
        mapper.getTableMetadata().getPartitionKey().get(0).getName(),
        mapper.getTableMetadata().getPartitionKey().get(1).getName());


    metadata.setUniqueValuesMetadata(Collections.singletonList(new UniqueValueMetadata(
        VendorSoftwareProductConstants.UniqueValues.IMAGE_NAME,
        Arrays.asList("vsp_id", "version", "component_id", "name"))));

    VersioningManagerFactory.getInstance().createInterface()
        .register(versionableEntityType, metadata);
  }

  @Override
  protected Mapper<ImageEntity> getMapper() {
    return mapper;
  }

  @Override
  protected Object[] getKeys(ImageEntity entity) {
    return new Object[]{entity.getVspId(),
        versionMapper.toUDT(entity.getVersion()), entity.getComponentId(), entity.getId() };
  }

  @Override
  public Collection<ImageEntity> list(ImageEntity entity) {
    return accessor.list(entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
        entity.getComponentId()).all();
  }

  @Override
  public void update(ImageEntity entity) {
    accessor.updateCompositionData(entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
        entity.getComponentId(), entity.getId(), entity.getCompositionData());
  }

  @Override
  public void updateQuestionnaireData(String vspId, Version version, String componentId,
                                      String imageId, String questionnaireData) {
    accessor.updateQuestionnaireData(questionnaireData, vspId, versionMapper.toUDT(version),
        componentId, imageId);
  }

  @Override
  public void delete(ImageEntity entity) {
    super.delete(entity);
  }

  @Override
  public void deleteByVspId(String vspId, Version version) {
    accessor.deleteByVspId(vspId, versionMapper.toUDT(version));
  }

  @Override
  public Collection<ImageEntity> listByVsp(String vspId, Version version) {
    return accessor.listByVspId(vspId, versionMapper.toUDT(version)).all();
  }

  @Override
  public ImageEntity getQuestionnaireData(String vspId, Version version, String componentId,
                                          String computeId) {
    return null;
  }

  @Accessor
  interface ImageAccessor {

    @Query("select vsp_id, version, component_id, image_id, composition_data from "
        + "vsp_component_image where vsp_id=? and version=? and component_id=?")
    Result<ImageEntity> list(String vspId, UDTValue version, String componentId);

    @Query(
        "insert into vsp_component_image (vsp_id, version, component_id, image_id, "
            + "composition_data) values (?,?,?,?,?)")
    ResultSet updateCompositionData(String vspId, UDTValue version, String componentId, String id,
                                    String compositionData);

    @Query("update vsp_component_image set questionnaire_data=? where vsp_id=? and version=?"
        + " and component_id=? and image_id=?")
    ResultSet updateQuestionnaireData(String questionnaireData, String vspId, UDTValue version,
                                      String componentId, String computeId);

    @Query("delete from vsp_component_image where vsp_id=? and version=?")
    ResultSet deleteByVspId(String vspId, UDTValue version);

    @Query("select * from vsp_component_image where vsp_id=? and version=?")
    Result<ImageEntity> listByVspId(String vspId, UDTValue version);

  }

}
