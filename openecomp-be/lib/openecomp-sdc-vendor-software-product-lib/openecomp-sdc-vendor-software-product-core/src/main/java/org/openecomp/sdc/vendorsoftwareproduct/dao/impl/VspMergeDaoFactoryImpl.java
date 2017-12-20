package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;


import org.openecomp.core.zusammen.api.ZusammenAdaptorFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspMergeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspMergeDaoFactory;

public class VspMergeDaoFactoryImpl extends VspMergeDaoFactory {

  private static final VspMergeDao INSTANCE = new
      VspMergeDaoImpl(ZusammenAdaptorFactory.getInstance().createInterface());

  @Override
  public VspMergeDao createInterface() {
    return INSTANCE;
  }
}
