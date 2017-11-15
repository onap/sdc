package org.openecomp.sdc.vendorsoftwareproduct.impl.mock;

import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageInfoDaoFactory;

public class PackageInfoDaoFactoryImplMock extends PackageInfoDaoFactory {

    @Override
    public PackageInfoDao createInterface() {
        return null;
    }
}
