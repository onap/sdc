package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;

public class CheckoutOnLockedEntityErrorBuilderTest {
    @Test
    public void test() {
        CheckoutOnLockedEntityErrorBuilder builder = new CheckoutOnLockedEntityErrorBuilder("entityType",
                "entityId", "lockingUser");
        Assert.assertNotNull(builder);
    }

}