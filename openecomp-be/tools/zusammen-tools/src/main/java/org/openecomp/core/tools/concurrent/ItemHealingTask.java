package org.openecomp.core.tools.concurrent;

import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;

import java.util.concurrent.Callable;

/**
 * Created by ayalaben on 11/7/2017
 */
public class ItemHealingTask implements Callable<String> {

  private String itemId;
  private String versionId;
  private String user;
  private VendorSoftwareProductManager vspManager;
  private VersioningManager versioningManager;

  public String getItemId() {
    return itemId;
  }

  public String getVersionId() {
    return versionId;
  }


  public ItemHealingTask(String itemId, String versionId, String user,
                         VendorSoftwareProductManager vspManager,
                         VersioningManager versioningManager) {
    this.itemId = itemId;
    this.versionId = versionId;
    this.user = user;
    this.versioningManager = versioningManager;
    this.vspManager = vspManager;
  }

  @Override
  public String call() throws Exception {

    VersionInfo versionInfo = getVersionInfo(itemId, VersionableEntityAction.Read, user);
    Version resolvedVersion = VersioningUtil.resolveVersion(Version.valueOf(versionId),
        versionInfo, user);
    VspDetails vspDetails = vspManager.getVsp(itemId, resolvedVersion, user);

    try {
      vspManager.callAutoHeal(itemId, versionInfo, vspDetails, user);

    } catch (Exception e) {
      return ("healing failed on vsp: " + vspDetails.getName() + "with id: " + itemId + " " +
          ", versionId" + versionId + ", resolved Version:" + resolvedVersion
       + ", with message: " + e.getMessage());
    }

    return "healed vsp: " + vspDetails.getName() + " with id: " + itemId
        + ", versionId: " + versionId + ", resolved version: " +
        resolvedVersion;
  }

  private VersionInfo getVersionInfo(String vendorSoftwareProductId, VersionableEntityAction action,
                                     String user) {
    return versioningManager.getEntityVersionInfo(
        VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE,
        vendorSoftwareProductId, user, action);
  }
}
