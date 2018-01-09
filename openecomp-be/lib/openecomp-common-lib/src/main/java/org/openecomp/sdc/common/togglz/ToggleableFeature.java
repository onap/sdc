package org.openecomp.sdc.common.togglz;

import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum ToggleableFeature implements ToggleStatus {

  @Label ("Forwarder Capability")
  FORWARDER_CAPABILITY;

  @Override
  public boolean isActive() {
    return FeatureContext.getFeatureManager().isActive(this);
  }
}
