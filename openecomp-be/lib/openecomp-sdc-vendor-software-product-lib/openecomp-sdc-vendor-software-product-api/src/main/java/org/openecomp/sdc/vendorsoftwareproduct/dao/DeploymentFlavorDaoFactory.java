package org.openecomp.sdc.vendorsoftwareproduct.dao;


import org.openecomp.core.factory.api.AbstractComponentFactory;
import org.openecomp.core.factory.api.AbstractFactory;

public abstract class DeploymentFlavorDaoFactory extends AbstractComponentFactory<DeploymentFlavorDao> {
    public static DeploymentFlavorDaoFactory getInstance() {
        return AbstractFactory.getInstance(DeploymentFlavorDaoFactory.class);
    }
}
