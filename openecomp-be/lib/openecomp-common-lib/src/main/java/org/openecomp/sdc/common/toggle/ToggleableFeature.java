package org.openecomp.sdc.common.toggle;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum ToggleableFeature implements Feature {

  @Label("Port Mirroring") PORT_MIRRORING;

  public boolean isActive() {
    return FeatureContext.getFeatureManager().isActive(this);
  }
}
