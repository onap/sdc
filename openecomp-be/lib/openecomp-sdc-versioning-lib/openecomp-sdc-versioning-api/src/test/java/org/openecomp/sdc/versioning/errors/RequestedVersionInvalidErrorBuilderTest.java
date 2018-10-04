package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class RequestedVersionInvalidErrorBuilderTest {
    @Test
    public void test() {
        RequestedVersionInvalidErrorBuilder builder = new RequestedVersionInvalidErrorBuilder();
        ErrorCode build = builder.build();
        Assert.assertEquals(VersioningErrorCodes.REQUESTED_VERSION_INVALID, build.id());
        Assert.assertEquals(ErrorCategory.APPLICATION, build.category());
        Assert.assertEquals(RequestedVersionInvalidErrorBuilder.REQUESTED_VERSION_INVALID_MSG, build.message());
    }
}