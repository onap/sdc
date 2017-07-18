package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import org.openecomp.core.zusammen.api.ZusammenAdaptorFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.ComputeDaoZusammenImpl;

public class ComputeDaoFactoryImpl extends ComputeDaoFactory {
  private static final ComputeDao INSTANCE = new ComputeDaoZusammenImpl(
      ZusammenAdaptorFactory.getInstance().createInterface());

  @Override
  public ComputeDao createInterface() {
    return INSTANCE;
  }
}
