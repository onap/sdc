package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;

public class CheckinOnEntityLockedByOtherErrorBuilderTest {

    @Test
    public void test() {
        CheckinOnEntityLockedByOtherErrorBuilder builder = new CheckinOnEntityLockedByOtherErrorBuilder("entityType",
                "entityId", "lockingUser");
        Assert.assertNotNull(builder);

    }

}