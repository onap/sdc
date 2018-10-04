package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class UndoCheckoutOnUnlockedEntityErrorBuilderTest {
    @Test
    public void test() {
        UndoCheckoutOnUnlockedEntityErrorBuilder builder = new UndoCheckoutOnUnlockedEntityErrorBuilder("entityType",
                "entityId");
        ErrorCode build = builder.build();
        Assert.assertEquals(VersioningErrorCodes.UNDO_CHECKOUT_ON_UNLOCKED_ENTITY, build.id());
        Assert.assertEquals(ErrorCategory.APPLICATION, build.category());
        Assert.assertEquals(String.format(UndoCheckoutOnUnlockedEntityErrorBuilder.UNDO_CHECKOUT_ON_UNLOCKED_ENTITY_MSG,
                "entityType", "entityId"), build.message());
    }
}