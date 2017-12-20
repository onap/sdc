package org.openecomp.core.zusammen.plugin.dao;

import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.utils.facade.api.AbstractComponentFactory;
import com.amdocs.zusammen.utils.facade.api.AbstractFactory;

public abstract class VersionStageRepositoryFactory extends AbstractComponentFactory<VersionStageRepository> {
  public static VersionStageRepositoryFactory getInstance() {
    return AbstractFactory.getInstance(VersionStageRepositoryFactory.class);
  }

  public abstract VersionStageRepository createInterface(SessionContext context);
}
