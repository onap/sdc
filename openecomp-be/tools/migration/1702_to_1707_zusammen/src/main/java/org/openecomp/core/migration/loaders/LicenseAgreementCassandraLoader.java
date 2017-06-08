package org.openecomp.core.migration.loaders;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;

import java.util.Collection;

/**
 * Created by ayalaben on 4/25/2017.
 */
public class LicenseAgreementCassandraLoader {

    private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
    private static final LicenseAgreementCassandraLoader.LicenseAgreementAccessor accessor =
            noSqlDb.getMappingManager().createAccessor(LicenseAgreementCassandraLoader.LicenseAgreementAccessor.class);

    public Collection<LicenseAgreementEntity> list() {
        return accessor.getAll().all();
    }

    @Accessor
    interface LicenseAgreementAccessor {
        @Query("SELECT * FROM license_agreement")
        Result<LicenseAgreementEntity> getAll();

    }
}
