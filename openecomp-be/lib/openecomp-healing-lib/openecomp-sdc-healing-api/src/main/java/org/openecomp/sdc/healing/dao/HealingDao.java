package org.openecomp.sdc.healing.dao;

import java.util.Optional;

/**
 * Created by ayalaben on 10/17/2017
 */
public interface HealingDao {

  Optional<Boolean> getItemHealingFlag(String space, String itemId, String versionId);

  void setItemHealingFlag(boolean healingNeededFlag, String space, String itemId, String versionId);
}
