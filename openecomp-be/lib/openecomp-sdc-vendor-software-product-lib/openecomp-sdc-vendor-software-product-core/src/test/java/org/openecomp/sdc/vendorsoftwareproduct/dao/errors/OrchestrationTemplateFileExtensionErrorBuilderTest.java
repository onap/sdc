package org.openecomp.sdc.vendorsoftwareproduct.dao.errors;

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.INVALID_EXTENSION;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.testng.Assert;

public class OrchestrationTemplateFileExtensionErrorBuilderTest {

    private OrchestrationTemplateFileExtensionErrorBuilder orchestrationTemplateFileExtensionErrorBuilder;

    @Before
    public void setUp() {
        orchestrationTemplateFileExtensionErrorBuilder = new OrchestrationTemplateFileExtensionErrorBuilder();
    }

    @Test
    public void shouldReturnInvalidExtentionErrorCode(){
        ErrorCode actual = orchestrationTemplateFileExtensionErrorBuilder.build();
        Assert.assertEquals(actual.category(), ErrorCategory.APPLICATION);
        Assert.assertEquals(actual.id(), INVALID_EXTENSION);
    }
}
