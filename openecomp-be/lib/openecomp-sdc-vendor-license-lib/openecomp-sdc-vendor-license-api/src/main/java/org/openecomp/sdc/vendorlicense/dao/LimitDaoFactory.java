package org.openecomp.sdc.vendorlicense.dao;

import org.openecomp.core.factory.api.AbstractComponentFactory;
import org.openecomp.core.factory.api.AbstractFactory;

public abstract class LimitDaoFactory extends AbstractComponentFactory<LimitDao> {
  public static LimitDaoFactory getInstance() {
    return AbstractFactory.getInstance(LimitDaoFactory.class);
  }
}
