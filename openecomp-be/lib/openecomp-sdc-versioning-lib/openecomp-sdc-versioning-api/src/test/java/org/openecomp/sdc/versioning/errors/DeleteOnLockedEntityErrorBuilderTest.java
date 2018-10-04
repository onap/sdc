package org.openecomp.sdc.versioning.errors;

import org.junit.Assert;
import org.junit.Test;


public class DeleteOnLockedEntityErrorBuilderTest {

    @Test
    public void test() {
        DeleteOnLockedEntityErrorBuilder builder = new DeleteOnLockedEntityErrorBuilder("entityType",
                "entityId", "lockingUser");
        Assert.assertNotNull(builder);
    }
}