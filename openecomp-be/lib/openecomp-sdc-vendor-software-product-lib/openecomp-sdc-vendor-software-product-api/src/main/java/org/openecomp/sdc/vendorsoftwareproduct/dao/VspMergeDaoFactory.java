package org.openecomp.sdc.vendorsoftwareproduct.dao;

import org.openecomp.core.factory.api.AbstractComponentFactory;
import org.openecomp.core.factory.api.AbstractFactory;

public abstract class VspMergeDaoFactory extends AbstractComponentFactory<VspMergeDao> {

  public static VspMergeDaoFactory getInstance() {
    return AbstractFactory.getInstance(VspMergeDaoFactory.class);
  }
}