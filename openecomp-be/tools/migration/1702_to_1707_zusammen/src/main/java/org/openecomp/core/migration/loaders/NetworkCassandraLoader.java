package org.openecomp.core.migration.loaders;

import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;

import java.util.Collection;

/**
 * @author katyr
 * @since April 23, 2017
 */

public class NetworkCassandraLoader {

  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static final NetworkCassandraLoader.NetworkAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(NetworkCassandraLoader.NetworkAccessor.class);


  public Collection<NetworkEntity> list() {
    return accessor.list().all();
  }

  @Accessor
  interface NetworkAccessor {

    @Query(
        "select * from vsp_network ")
    Result<NetworkEntity> list();
  }
}
