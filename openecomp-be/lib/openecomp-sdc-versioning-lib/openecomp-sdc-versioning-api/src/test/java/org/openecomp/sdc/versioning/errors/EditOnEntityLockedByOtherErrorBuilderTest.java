package org.openecomp.sdc.versioning.errors;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;


public class EditOnEntityLockedByOtherErrorBuilderTest {
    @Test
    public void test() {
        EditOnEntityLockedByOtherErrorBuilder builder = new EditOnEntityLockedByOtherErrorBuilder("entityType",
                "entityId", "lockingUser");

        ErrorCode build = builder.build();
        Assert.assertEquals(VersioningErrorCodes.EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER, build.id());
        Assert.assertEquals(ErrorCategory.APPLICATION, build.category());
        Assert.assertEquals(String.format(EditOnEntityLockedByOtherErrorBuilder.EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER_MSG,
                "entityType", "entityId", "lockingUser"),build.message());
    }
}