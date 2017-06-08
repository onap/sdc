package org.openecomp.core.migration.loaders;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;

import java.util.Collection;

/**
 * Created by ayalaben on 4/24/2017
 */
public class EntitlementPoolCassandraLoader {

    private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();

    private static final EntitlementPoolCassandraLoader.EntitlementPoolAccessor accessor =
            noSqlDb.getMappingManager().createAccessor(EntitlementPoolCassandraLoader.EntitlementPoolAccessor.class);

    public Collection<EntitlementPoolEntity> list() {
        return accessor.list().all();
    }

    @Accessor
    interface EntitlementPoolAccessor {
        @Query("select * from entitlement_pool ")
        Result<EntitlementPoolEntity> list();
    }
}
