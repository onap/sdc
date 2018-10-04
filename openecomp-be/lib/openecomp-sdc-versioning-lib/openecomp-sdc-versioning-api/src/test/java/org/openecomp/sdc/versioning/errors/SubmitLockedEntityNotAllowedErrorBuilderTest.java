package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class SubmitLockedEntityNotAllowedErrorBuilderTest {
    @Test
    public void test() {
        SubmitLockedEntityNotAllowedErrorBuilder builder = new SubmitLockedEntityNotAllowedErrorBuilder("entityType",
                "entityId", "lockingUser");

        ErrorCode build = builder.build();
        Assert.assertEquals(VersioningErrorCodes.SUBMIT_LOCKED_ENTITY_NOT_ALLOWED, build.id());
        Assert.assertEquals(ErrorCategory.APPLICATION, build.category());
        Assert.assertEquals(String.format(SubmitLockedEntityNotAllowedErrorBuilder.SUBMIT_LOCKED_ENTITY_NOT_ALLOWED_MSG,
                "entityType", "entityId", "lockingUser"), build.message());
    }
}