package org.openecomp.core.zusammen.plugin.dao;

import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.utils.facade.api.AbstractComponentFactory;
import com.amdocs.zusammen.utils.facade.api.AbstractFactory;

public abstract class VersionSynchronizationStateRepositoryFactory
    extends AbstractComponentFactory<VersionSynchronizationStateRepository> {
  public static VersionSynchronizationStateRepositoryFactory getInstance() {
    return AbstractFactory.getInstance(VersionSynchronizationStateRepositoryFactory.class);
  }

  public abstract VersionSynchronizationStateRepository createInterface(SessionContext context);
}
