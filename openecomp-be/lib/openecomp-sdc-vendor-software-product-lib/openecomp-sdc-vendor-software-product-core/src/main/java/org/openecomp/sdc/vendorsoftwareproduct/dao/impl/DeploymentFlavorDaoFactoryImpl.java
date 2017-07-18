package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import org.openecomp.core.zusammen.api.ZusammenAdaptorFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.DeploymentFlavorDaoZusammenImpl;


public class DeploymentFlavorDaoFactoryImpl extends DeploymentFlavorDaoFactory{
    private static final  DeploymentFlavorDao INSTANCE = new DeploymentFlavorDaoZusammenImpl(
        ZusammenAdaptorFactory.getInstance().createInterface());

    @Override
    public DeploymentFlavorDao createInterface() {
        return INSTANCE;
    }
}
