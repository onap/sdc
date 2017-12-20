package org.openecomp.core.zusammen.plugin.dao;

import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.utils.facade.api.AbstractComponentFactory;
import com.amdocs.zusammen.utils.facade.api.AbstractFactory;

public abstract class ElementStageRepositoryFactory
    extends AbstractComponentFactory<ElementStageRepository> {
  public static ElementStageRepositoryFactory getInstance() {
    return AbstractFactory.getInstance(ElementStageRepositoryFactory.class);
  }

  public abstract ElementStageRepository createInterface(SessionContext context);
}
