package org.openecomp.core.tools.store;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.core.tools.store.zusammen.datatypes.HealingEntity;

import java.util.ArrayList;

/**
 * Created by ayalaben on 11/5/2017
 */
public class HealingHandler {


  private static NoSqlDb nnoSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static HealingHandler.HealingAccessor accessor =
      nnoSqlDb.getMappingManager().createAccessor(HealingHandler.HealingAccessor.class);


  public void populateHealingTable(ArrayList<HealingEntity> healingEntities) {

    healingEntities.forEach(healingEntity -> {
      if (isExists(healingEntity.getSpace(),healingEntity.getItemId(),healingEntity
          .getVersionId()) && !isHealingNeeded(healingEntity.getSpace(),
          healingEntity.getItemId(),healingEntity.getVersionId())) {

        accessor.update(healingEntity.getHealingFlag(),healingEntity
            .getSpace(),healingEntity.getItemId(),healingEntity.getVersionId());

      } else if (!isExists(healingEntity.getSpace(),healingEntity.getItemId(),healingEntity
          .getVersionId())) {

        accessor.create(healingEntity.getSpace(),healingEntity.getItemId(),healingEntity
            .getVersionId(),healingEntity.getHealingFlag(),healingEntity.getOldVersion());
      }
    });
  }

  private boolean isHealingNeeded(String space, String itemId, String versionId) {
    ResultSet result =  accessor.getItemHealingFlag(space,itemId,versionId);
    if (result.getAvailableWithoutFetching() < 1) {
      return false;
    }
    return result.one().getBool(0);
  }

  private boolean isExists(String space, String itemId, String versionId) {
    ResultSet result =  accessor.getItemHealingFlag(space,itemId,versionId);
    if (result.getAvailableWithoutFetching() < 1) {
      return false;
    }
    return true;
  }


  @Accessor
  interface HealingAccessor {

    @Query("UPDATE dox.healing set healing_needed=? WHERE space=? AND item_id=? AND" +
        " version_id=?" )
    void update(boolean flag,String space, String itemId, String versionId);

    @Query("select healing_needed from healing WHERE  space = ? AND item_id = ? AND version_id = ?")
    ResultSet getItemHealingFlag(String space, String itemId, String versionId);

    @Query("Insert into dox.healing (space,item_id,version_id,healing_needed,old_version) values " +
        "(?,?,?,?,?)")
    void create(String space, String itemId, String versionId,boolean flag,String oldVersion);

  }
}
