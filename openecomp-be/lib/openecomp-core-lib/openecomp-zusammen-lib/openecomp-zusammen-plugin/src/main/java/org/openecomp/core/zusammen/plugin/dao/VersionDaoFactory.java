package org.openecomp.core.zusammen.plugin.dao;

import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.utils.facade.api.AbstractComponentFactory;
import com.amdocs.zusammen.utils.facade.api.AbstractFactory;

public abstract class VersionDaoFactory extends AbstractComponentFactory<VersionDao> {
  public static VersionDaoFactory getInstance() {
    return AbstractFactory.getInstance(VersionDaoFactory.class);
  }

  public abstract VersionDao createInterface(SessionContext context);
}
