package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class UndoCheckoutOnEntityLockedByOtherErrorBuilderTest {
    @Test
    public void test() {

        UndoCheckoutOnEntityLockedByOtherErrorBuilder builder = new UndoCheckoutOnEntityLockedByOtherErrorBuilder("entityType",
                "entityId", "lockingUser");

        ErrorCode build = builder.build();
        Assert.assertEquals(VersioningErrorCodes.UNDO_CHECKOUT_ON_ENTITY_LOCKED_BY_OTHER_USER, build.id());
        Assert.assertEquals(ErrorCategory.APPLICATION, build.category());
        Assert.assertEquals(String.format("Can not undo checkout on versionable entity %s with id %s since it is checked "
                +  "out by other user: %s.", "entityType", "entityId", "lockingUser"), build.message());
    }
}