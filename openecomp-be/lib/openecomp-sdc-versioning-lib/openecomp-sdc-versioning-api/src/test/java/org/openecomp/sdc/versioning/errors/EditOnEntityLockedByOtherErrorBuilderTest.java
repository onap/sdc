package org.openecomp.sdc.versioning.errors;

import org.junit.Assert;
import org.junit.Test;


public class EditOnEntityLockedByOtherErrorBuilderTest {
    @Test
    public void test() {
        EditOnEntityLockedByOtherErrorBuilder builder = new EditOnEntityLockedByOtherErrorBuilder("entityType",
                "entityId", "lockingUser");
        Assert.assertNotNull(builder);
    }
}