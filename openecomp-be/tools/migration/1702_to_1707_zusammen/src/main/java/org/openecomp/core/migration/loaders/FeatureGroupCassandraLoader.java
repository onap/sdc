package org.openecomp.core.migration.loaders;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;

import java.util.Collection;

/**
 * Created by ayalaben on 4/25/2017.
 */
public class FeatureGroupCassandraLoader {

    private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
    private static final FeatureGroupCassandraLoader.FGAccessor accessor =
            noSqlDb.getMappingManager().createAccessor(FeatureGroupCassandraLoader.FGAccessor.class);

    public Collection<FeatureGroupEntity> list() {
        return accessor.getAll().all();
    }

    @Accessor
    interface FGAccessor {
        @Query("SELECT * FROM feature_group")
        Result<FeatureGroupEntity> getAll();
    }
}
