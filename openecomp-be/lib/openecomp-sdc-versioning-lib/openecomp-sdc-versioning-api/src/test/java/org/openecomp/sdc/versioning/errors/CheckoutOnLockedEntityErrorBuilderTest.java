package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class CheckoutOnLockedEntityErrorBuilderTest {
    @Test
    public void test() {
        CheckoutOnLockedEntityErrorBuilder builder = new CheckoutOnLockedEntityErrorBuilder("entityType",
                "entityId", "lockingUser");

        ErrorCode build = builder.build();
        Assert.assertEquals(VersioningErrorCodes.CHECKOT_ON_LOCKED_ENTITY, build.id() );
        Assert.assertEquals(ErrorCategory.APPLICATION, build.category() );
        Assert.assertEquals(String.format(CheckoutOnLockedEntityErrorBuilder.CHECKOT_ON_LOCKED_ENTITY_MSG,
                "entityType", "entityId", "lockingUser") ,  build.message());
    }

}