package org.openecomp.core.migration.loaders;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;

import java.util.Collection;

/**
 * Created by ayalaben on 4/24/2017
 */
public class LKGCassandraLoader {

    private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
    private static final LKGCassandraLoader.LKGAccessor accessor =
            noSqlDb.getMappingManager().createAccessor(LKGCassandraLoader.LKGAccessor.class);

    public Collection<LicenseKeyGroupEntity> list() {
        return accessor.getAll().all();
    }

    @Accessor
    interface LKGAccessor {
        @Query("SELECT * FROM license_key_group")
        Result<LicenseKeyGroupEntity> getAll();

    }
}
