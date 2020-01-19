package org.openecomp.sdc.be.dao.cassandra;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.sdc.be.resources.data.togglz.FeatureToggleEvent;

@Accessor
public interface FeatureToggleAccessor {
    @Query("SELECT * FROM sdcrepository.featuretogglestate")
    Result<FeatureToggleEvent> getAllFeatures();
}
