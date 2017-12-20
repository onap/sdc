package org.openecomp.sdc.vendorsoftwareproduct.dao;

import com.amdocs.zusammen.datatypes.item.Resolution;
import org.openecomp.sdc.versioning.dao.types.Version;

public interface VspMergeDao {

  boolean isVspModelConflicted(String vspId, Version version);

  void updateVspModelId(String vspId, Version version);

  // TODO: 11/7/2017 change to sdc Resolution
  void updateVspModelConflictResolution(String vspId, Version version, Resolution resolution);

  void applyVspModelConflictResolution(String vspId, Version version);
}
