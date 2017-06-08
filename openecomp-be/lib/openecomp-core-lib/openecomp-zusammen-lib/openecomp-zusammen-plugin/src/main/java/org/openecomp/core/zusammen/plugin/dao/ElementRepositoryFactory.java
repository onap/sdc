package org.openecomp.core.zusammen.plugin.dao;

import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.utils.facade.api.AbstractComponentFactory;
import com.amdocs.zusammen.utils.facade.api.AbstractFactory;

public abstract class ElementRepositoryFactory extends AbstractComponentFactory<ElementRepository> {
  public static ElementRepositoryFactory getInstance() {
    return AbstractFactory.getInstance(ElementRepositoryFactory.class);
  }

  public abstract ElementRepository createInterface(SessionContext context);
}

