package org.openecomp.sdc.versioning.impl;

import org.openecomp.sdc.versioning.VersionCalculator;
import org.openecomp.sdc.versioning.VersionCalculatorFactory;

public class MajorVersionCalculatorFactoryImpl extends VersionCalculatorFactory {
  private static final VersionCalculator INSTANCE =
      new MajorVersionCalculatorImpl();

  @Override
  public VersionCalculator createInterface() {
    return INSTANCE;
  }
}
