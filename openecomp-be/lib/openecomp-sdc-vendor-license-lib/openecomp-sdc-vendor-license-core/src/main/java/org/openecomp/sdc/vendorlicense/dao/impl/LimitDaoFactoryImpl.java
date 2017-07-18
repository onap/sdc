package org.openecomp.sdc.vendorlicense.dao.impl;

import org.openecomp.core.zusammen.api.ZusammenAdaptorFactory;
import org.openecomp.sdc.vendorlicense.dao.LimitDao;
import org.openecomp.sdc.vendorlicense.dao.LimitDaoFactory;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.LimitZusammenDaoImpl;

public class LimitDaoFactoryImpl extends LimitDaoFactory {

  private static LimitDao INSTANCE = new LimitZusammenDaoImpl(ZusammenAdaptorFactory.getInstance()
      .createInterface());

  @Override
  public LimitDao createInterface() {
    return INSTANCE;
  }
}
