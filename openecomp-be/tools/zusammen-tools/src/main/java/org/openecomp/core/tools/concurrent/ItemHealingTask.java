package org.openecomp.core.tools.concurrent;

import org.openecomp.sdc.healing.api.HealingManager;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.concurrent.Callable;

/**
 * Created by ayalaben on 11/7/2017
 */
public class ItemHealingTask implements Callable<String> {

  private String itemId;
  private Version version;
  private VendorSoftwareProductManager vspManager;
  private HealingManager healingManager;

  public String getItemId() {
    return itemId;
  }

  public String getVersionId() {
//    return version.getId();
    return null;
  }


  public ItemHealingTask(String itemId, String versionId,
                         VendorSoftwareProductManager vspManager,
                         HealingManager healingManager) {
//    this.itemId = itemId;
//    this.version = new Version(versionId);
    this.vspManager = vspManager;
    this.healingManager = healingManager;

  }

  @Override
  public String call() throws Exception {
//    VspDetails vspDetails = vspManager.getVsp(itemId, version);
//
//    try {
//      healingManager.healItemVersion(itemId, version, ItemType.vsp, true);
//    } catch (Exception e) {
//      return (String
//          .format("healing failed on vsp: %s with id: %s, versionId: %s, with message: %s",
//              vspDetails.getName(), itemId, version.getId(), e.getMessage()));
//    }
//
//    return String.format("healed vsp: %s, with id: %s, versionId: %s",
//        vspDetails.getName(), itemId, version.getId());
    return null;
  }
}
