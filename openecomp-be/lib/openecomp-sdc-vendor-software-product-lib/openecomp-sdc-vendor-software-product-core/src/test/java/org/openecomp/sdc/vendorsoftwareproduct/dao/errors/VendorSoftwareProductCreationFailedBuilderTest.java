package org.openecomp.sdc.vendorsoftwareproduct.dao.errors;

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.FAILED_TO_CREATE_VSP;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.testng.Assert;

public class VendorSoftwareProductCreationFailedBuilderTest {

    private static final String VSP_ID = "testVsp1";
    private VendorSoftwareProductCreationFailedBuilder vendorSoftwareProductCreationFailedBuilder;

    @Before
    public void setUp() {
        vendorSoftwareProductCreationFailedBuilder = new VendorSoftwareProductCreationFailedBuilder(VSP_ID);
    }

    @Test
    public void shouldReturnVspNotFoundErrorCode(){
        ErrorCode actual = vendorSoftwareProductCreationFailedBuilder.build();
        Assert.assertEquals(actual.category(), ErrorCategory.APPLICATION);
        Assert.assertEquals(actual.id(), FAILED_TO_CREATE_VSP);
    }
}
