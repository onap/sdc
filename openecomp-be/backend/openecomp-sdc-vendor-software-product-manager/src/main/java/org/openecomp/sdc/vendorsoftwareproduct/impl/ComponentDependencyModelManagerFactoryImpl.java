package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.openecomp.sdc.vendorsoftwareproduct.ComponentDependencyModelManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentDependencyModelManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDependencyModelDaoFactory;

public class ComponentDependencyModelManagerFactoryImpl extends
    ComponentDependencyModelManagerFactory {

  private static final ComponentDependencyModelManager INSTANCE =
      new ComponentDependencyModelManagerImpl(
          ComponentManagerFactory.getInstance().createInterface(),
          ComponentDependencyModelDaoFactory.getInstance().createInterface()
      );

  @Override
  public ComponentDependencyModelManager createInterface() {
    return INSTANCE;
  }

}
