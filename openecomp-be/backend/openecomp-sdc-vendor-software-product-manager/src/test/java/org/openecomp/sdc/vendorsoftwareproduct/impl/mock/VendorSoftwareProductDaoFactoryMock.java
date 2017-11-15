package org.openecomp.sdc.vendorsoftwareproduct.impl.mock;

import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;

public class VendorSoftwareProductDaoFactoryMock extends VendorSoftwareProductDaoFactory {

    @Override
    public VendorSoftwareProductDao createInterface() {
        return null;
    }
}
