package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;

public class UndoCheckoutOnUnlockedEntityErrorBuilderTest {
    @Test
    public void test() {
        UndoCheckoutOnUnlockedEntityErrorBuilder builder = new UndoCheckoutOnUnlockedEntityErrorBuilder("entityType",
                "entityId");
        Assert.assertNotNull(builder);
    }
}