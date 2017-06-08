package org.openecomp.sdc.vendorsoftwareproduct;


import org.openecomp.core.factory.api.AbstractComponentFactory;
import org.openecomp.core.factory.api.AbstractFactory;

public abstract class ComponentDependencyModelManagerFactory extends
    AbstractComponentFactory<ComponentDependencyModelManager> {

  public static ComponentDependencyModelManagerFactory getInstance() {
    return AbstractFactory.getInstance(ComponentDependencyModelManagerFactory.class);
  }
}
