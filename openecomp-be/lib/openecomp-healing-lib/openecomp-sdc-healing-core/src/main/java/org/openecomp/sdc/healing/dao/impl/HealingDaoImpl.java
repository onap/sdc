package org.openecomp.sdc.healing.dao.impl;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.sdc.healing.dao.HealingDao;


/**
 * Created by ayalaben on 11/5/2017
 */
public class HealingDaoImpl implements HealingDao {

  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static HealingDaoImpl.HealingAccessor accessor =
      noSqlDb.getMappingManager().createAccessor(HealingDaoImpl.HealingAccessor.class);

  @Override
  public boolean getItemHealingFlag(String space, String itemId, String versionId) {
    ResultSet result =  accessor.getItemHealingFlag(space,itemId,versionId);
    if (result.getAvailableWithoutFetching() < 1) {
      return false;
    }
    return result.one().getBool(0);
  }

  @Override
  public void setItemHealingFlag(boolean flag, String space, String itemId, String versionId) {
    accessor.setItemHealingFlag(flag,space,itemId,versionId);
  }


  @Accessor
  interface HealingAccessor {

    @Query("select healing_needed from healing WHERE  space = ? AND item_id = ? AND version_id = ?")
    ResultSet getItemHealingFlag(String space, String itemId, String versionId);

    @Query("UPDATE healing SET healing_needed=? WHERE space = ? AND item_id = ? AND version_id = ?")
    void setItemHealingFlag(boolean flag,String space, String itemId, String versionId);

  }
}
