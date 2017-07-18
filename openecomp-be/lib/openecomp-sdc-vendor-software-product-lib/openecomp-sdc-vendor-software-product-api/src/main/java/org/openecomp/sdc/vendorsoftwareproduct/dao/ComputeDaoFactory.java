package org.openecomp.sdc.vendorsoftwareproduct.dao;

import org.openecomp.core.factory.api.AbstractComponentFactory;
import org.openecomp.core.factory.api.AbstractFactory;

public abstract class ComputeDaoFactory extends AbstractComponentFactory<ComputeDao> {


  public static ComputeDaoFactory getInstance() {
    return AbstractFactory.getInstance(ComputeDaoFactory.class);
  }
}
