package org.openecomp.sdc.vendorsoftwareproduct.dao.errors;

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.VSP_NOT_FOUND;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.testng.Assert;

public class VendorSoftwareProductNotFoundErrorBuilderTest {

    private static final String VSP_ID = "testVsp1";
    private VendorSoftwareProductNotFoundErrorBuilder vendorSoftwareProductNotFoundErrorBuilder;

    @Before
    public void setUp() {
        vendorSoftwareProductNotFoundErrorBuilder = new VendorSoftwareProductNotFoundErrorBuilder(VSP_ID);
    }

    @Test
    public void shouldReturnVspNotFoundErrorCode(){
        ErrorCode actual = vendorSoftwareProductNotFoundErrorBuilder.build();
        Assert.assertEquals(actual.category(), ErrorCategory.APPLICATION);
        Assert.assertEquals(actual.id(), VSP_NOT_FOUND);
    }
}
