package org.openecomp.core.zusammen.plugin.dao;

import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.utils.facade.api.AbstractComponentFactory;
import com.amdocs.zusammen.utils.facade.api.AbstractFactory;

public abstract class ElementSynchronizationStateRepositoryFactory
    extends AbstractComponentFactory<ElementSynchronizationStateRepository> {
  public static ElementSynchronizationStateRepositoryFactory getInstance() {
    return AbstractFactory.getInstance(ElementSynchronizationStateRepositoryFactory.class);
  }

  public abstract ElementSynchronizationStateRepository createInterface(SessionContext context);
}
