package org.openecomp.core.tools.store;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Query;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import org.openecomp.core.tools.store.zusammen.datatypes.HealingEntity;

import java.util.ArrayList;

/**
 * Created by ayalaben on 10/15/2017
 */
public class HealingHandler {

  private static NoSqlDb nnoSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static HealingAccessor accessor =
      nnoSqlDb.getMappingManager().createAccessor(HealingAccessor.class);


  public void populateHealingTable(ArrayList<HealingEntity> healingEntities) {
    healingEntities.forEach(healingEntity -> {
      if (isHealingRecordExist(healingEntity)) {
        accessor.updateFlag(healingEntity.getHealingFlag(), healingEntity.getSpace(),
            healingEntity.getItemId(), healingEntity.getVersionId());
      } else {
        accessor.create(healingEntity.getSpace(), healingEntity.getItemId(),
            healingEntity.getVersionId(), healingEntity.getHealingFlag(),
            healingEntity.getOldVersion());
      }
    });
  }

  private boolean isHealingRecordExist(HealingEntity healingEntity) {
    return accessor.getFlag(healingEntity.getSpace(), healingEntity.getItemId(),
        healingEntity.getVersionId()).getAvailableWithoutFetching() == 1;
  }

  @Accessor
  interface HealingAccessor {

    @Query("SELECT healing_needed FROM healing WHERE space=? AND item_id=? AND version_id=?")
    ResultSet getFlag(String space, String itemId, String versionId);

    @Query("Insert into healing (space, item_id, version_id, healing_needed, old_version) " +
        "values (?,?,?,?,?)")
    void create(String space, String itemId, String versionId, boolean flag, String oldVersion);

    @Query("UPDATE healing SET healing_needed=? WHERE space=? AND item_id=? AND version_id=?")
    void updateFlag(boolean flag, String space, String itemId, String versionId);
  }

}