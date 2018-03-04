package org.openecomp.sdc.vendorsoftwareproduct.dao;

import com.amdocs.zusammen.datatypes.item.Resolution;
import org.openecomp.sdc.versioning.dao.types.Version;

public interface VspMergeDao {

  boolean isConflicted(String vspId, Version version);

  void updateHint(String vspId, Version version);

  void deleteHint(String vspId, Version version);

  // TODO: 11/7/2017 change to sdc Resolution
  void updateConflictResolution(String vspId, Version version, Resolution resolution);

  void applyConflictResolution(String vspId, Version version);
}
