package org.openecomp.sdc.vendorsoftwareproduct;


import org.openecomp.core.factory.api.AbstractComponentFactory;
import org.openecomp.core.factory.api.AbstractFactory;

public abstract class DeploymentFlavorManagerFactory extends
    AbstractComponentFactory<DeploymentFlavorManager> {

  public static DeploymentFlavorManagerFactory getInstance() {
    return AbstractFactory.getInstance(DeploymentFlavorManagerFactory.class);
  }
}
