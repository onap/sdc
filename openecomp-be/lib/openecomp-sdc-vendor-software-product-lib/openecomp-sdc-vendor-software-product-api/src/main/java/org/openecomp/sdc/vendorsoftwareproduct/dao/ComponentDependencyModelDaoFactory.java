package org.openecomp.sdc.vendorsoftwareproduct.dao;


import org.openecomp.core.factory.api.AbstractComponentFactory;
import org.openecomp.core.factory.api.AbstractFactory;

public abstract class ComponentDependencyModelDaoFactory extends
    AbstractComponentFactory<ComponentDependencyModelDao> {

  public static ComponentDependencyModelDaoFactory getInstance() {
    return AbstractFactory.getInstance(ComponentDependencyModelDaoFactory.class);
  }
}
