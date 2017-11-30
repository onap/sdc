package org.openecomp.sdc.be.dao.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;


/**
 * Created by chaya on 7/5/2017.
 */
@Accessor
public interface ArtifactAccessor {
    // *****  get the number of artifacts with a specific id
    @Query("SELECT COUNT(*) FROM sdcartifact.resources WHERE ID = :uniqueId")
    ResultSet getNumOfArtifactsById(@Param("uniqueId") String uniqueId);
}
