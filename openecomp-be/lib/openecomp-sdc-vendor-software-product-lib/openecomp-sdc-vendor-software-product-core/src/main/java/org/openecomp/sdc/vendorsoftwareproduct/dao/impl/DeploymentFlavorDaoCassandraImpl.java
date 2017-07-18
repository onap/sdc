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
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.UniqueValueMetadata;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


public class DeploymentFlavorDaoCassandraImpl extends CassandraBaseDao<DeploymentFlavorEntity> implements DeploymentFlavorDao {
    private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
    private static final Mapper<DeploymentFlavorEntity> mapper = noSqlDb.getMappingManager().mapper(DeploymentFlavorEntity.class);
    private static final DeploymentFlavorAccessor accessor =
            noSqlDb.getMappingManager().createAccessor(DeploymentFlavorAccessor.class);
    private static final UDTMapper<Version> versionMapper =
            noSqlDb.getMappingManager().udtMapper(Version.class);
    @Override
    protected Mapper<DeploymentFlavorEntity> getMapper() {
        return mapper;
    }

    @Override
    protected Object[] getKeys(DeploymentFlavorEntity entity) {
        return new Object[]{entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
                entity.getId()};
    }

    @Override
    public Collection<DeploymentFlavorEntity> list(DeploymentFlavorEntity entity) {
        return accessor.list(entity.getVspId(), versionMapper.toUDT(entity.getVersion())).all();
    }

    @Override
    public void update(DeploymentFlavorEntity entity) {
        accessor.updateCompositionData(entity.getVspId(), versionMapper.toUDT(entity.getVersion()),
            entity.getId(), entity.getCompositionData());
    }

    @Override
    public void registerVersioning(String versionableEntityType) {
        VersionableEntityMetadata metadata = new VersionableEntityMetadata(
                mapper.getTableMetadata().getName(),
                mapper.getTableMetadata().getPartitionKey().get(0).getName(),
                mapper.getTableMetadata().getPartitionKey().get(1).getName());


        metadata.setUniqueValuesMetadata(Collections.singletonList(new UniqueValueMetadata(
                VendorSoftwareProductConstants.UniqueValues.DEPLOYMENT_FLAVOR_NAME,
                Arrays.asList(mapper.getTableMetadata().getPartitionKey().get(0).getName(),
                        mapper.getTableMetadata().getPartitionKey().get(1).getName(), "name"))));

        VersioningManagerFactory.getInstance().createInterface()
                .register(versionableEntityType, metadata);
    }

    @Override
    public void deleteAll(String vspId, Version version) {
        accessor.deleteAll(vspId, version);
    }

    @Accessor
    interface DeploymentFlavorAccessor {
        @Query(
                "select vsp_id, version, deployment_flavor_id, composition_data from vsp_deployment_flavor where vsp_id=?"
                        + " and version=?")
        Result<DeploymentFlavorEntity> list(String vspId, UDTValue version);

        @Query(
                "insert into vsp_deployment_flavor (vsp_id, version, deployment_flavor_id, composition_data) values (?,?,?,?)")
        ResultSet updateCompositionData(String vspId, UDTValue version, String id,
                                        String compositionData);

        @Query("delete from vsp_deployment_flavor where vsp_id=? and version=?")
        ResultSet deleteAll(String vspId, Version version);

    }
}
