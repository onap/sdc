package org.openecomp.sdc.versioning.errors;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;


public class DeleteOnLockedEntityErrorBuilderTest {

    @Test
    public void test() {
        DeleteOnLockedEntityErrorBuilder builder = new DeleteOnLockedEntityErrorBuilder("entityType",
                "entityId", "lockingUser");

        ErrorCode build = builder.build();
        Assert.assertEquals(VersioningErrorCodes.DELETE_ON_LOCKED_ENTITY, build.id());
        Assert.assertEquals(ErrorCategory.APPLICATION, build.category());
        Assert.assertEquals(String.format(DeleteOnLockedEntityErrorBuilder.DELETE_ON_LOCKED_ENTITY_MSG,
                "entityType", "entityId", "lockingUser"), build.message());
    }
}