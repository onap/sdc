package org.openecomp.sdc.healing.dao.impl;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.healing.dao.HealingDao;

import java.util.Optional;

/**
 * Created by ayalaben on 10/17/2017
 */
public class HealingDaoImpl implements HealingDao {

  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static HealingAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(HealingAccessor.class);

  @Override
  public Optional<Boolean> getItemHealingFlag(String space, String itemId, String versionId) {
    ResultSet result = accessor.getItemHealingFlag(space, itemId, versionId);
    return result.getAvailableWithoutFetching() < 1
        ? Optional.empty()
        : Optional.of(result.one().getBool("healing_needed"));
  }

  @Override
  public void setItemHealingFlag(boolean healingNeededFlag, String space, String itemId,
                                 String versionId) {
    accessor.setItemHealingFlag(healingNeededFlag, space, itemId, versionId);
  }


  @Accessor
  interface HealingAccessor {

    @Query("SELECT healing_needed FROM healing WHERE space=? AND item_id=? AND version_id=?")
    ResultSet getItemHealingFlag(String space, String itemId, String versionId);

    @Query("UPDATE healing SET healing_needed=? WHERE space=? AND item_id=? AND version_id=?")
    void setItemHealingFlag(boolean flag, String space, String itemId, String versionId);

  }
}
