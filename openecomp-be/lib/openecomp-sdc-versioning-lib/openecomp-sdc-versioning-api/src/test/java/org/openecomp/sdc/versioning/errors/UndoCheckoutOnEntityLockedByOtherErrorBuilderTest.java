package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;

public class UndoCheckoutOnEntityLockedByOtherErrorBuilderTest {
    @Test
    public void test() {

        UndoCheckoutOnEntityLockedByOtherErrorBuilder builder = new UndoCheckoutOnEntityLockedByOtherErrorBuilder("entityType",
                "entityId", "lockingUser");
        Assert.assertNotNull(builder);
    }
}