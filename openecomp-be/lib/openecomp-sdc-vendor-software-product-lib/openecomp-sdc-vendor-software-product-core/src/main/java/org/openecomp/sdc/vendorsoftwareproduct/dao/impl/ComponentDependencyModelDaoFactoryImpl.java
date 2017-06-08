package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;


import org.openecomp.core.zusammen.api.ZusammenAdaptorFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDependencyModelDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDependencyModelDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.ComponentDependencyModelDaoZusammenImpl;

public class ComponentDependencyModelDaoFactoryImpl extends ComponentDependencyModelDaoFactory {

  private static final ComponentDependencyModelDao INSTANCE = new
      ComponentDependencyModelDaoZusammenImpl( ZusammenAdaptorFactory.getInstance().createInterface());

  @Override
  public ComponentDependencyModelDao createInterface() {
    return INSTANCE;
  }
}
