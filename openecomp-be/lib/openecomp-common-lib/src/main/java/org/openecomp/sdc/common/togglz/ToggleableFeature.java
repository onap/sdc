package org.openecomp.sdc.common.togglz;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum ToggleableFeature implements Feature {

  @Label("Archive Item")
  ARCHIVE_ITEM,

  @Label("Filter")
  FILTER,

  @Label(("MRN"))
  MRN;

  public boolean isActive() {
    return FeatureContext.getFeatureManager().isActive(this);
  }
}
