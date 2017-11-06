package org.openecomp.sdc.healing.dao;

/**
 * Created by ayalaben on 11/5/2017
 */
public interface HealingDao {

  boolean getItemHealingFlag(String space,String itemId, String versionIdId);
  void setItemHealingFlag(boolean flag, String space,String itemId, String versionIdId );
}
