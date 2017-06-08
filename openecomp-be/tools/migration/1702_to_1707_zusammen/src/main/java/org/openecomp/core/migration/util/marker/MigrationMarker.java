package org.openecomp.core.migration.util.marker;

import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.migration.MigrationMain;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

/**
 * @author katyr
 * @since May 01, 2017
 */

public class MigrationMarker {

  private static Logger logger = LoggerFactory.getLogger(MigrationMain.class);

  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static final MigrationTableAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(MigrationMarker.MigrationTableAccessor.class);

  public static Boolean isMigrated() {

    MigrationMarkerEntity result = accessor.isMigrated();
    if (result == null) {
      return false;
    } else {
      return result.getMigrated();
    }
  }

  public static void markMigrated() {
    accessor.markMigrated();
  }

  @Accessor
  interface MigrationTableAccessor {
    @Query("SELECT * FROM migration where id='1'")
    MigrationMarkerEntity isMigrated();


    @Query("insert into migration (id,isMigrated) values('1',true)")
    void markMigrated();

  }

}
