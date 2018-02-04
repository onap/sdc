package org.openecomp.sdc.common.togglz;

import org.togglz.core.Feature;

public interface ToggleStatus extends Feature {
  boolean isActive();
}
