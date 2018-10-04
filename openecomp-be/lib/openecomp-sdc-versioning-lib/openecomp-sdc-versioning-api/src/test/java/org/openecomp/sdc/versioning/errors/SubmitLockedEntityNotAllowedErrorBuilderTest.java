package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;

public class SubmitLockedEntityNotAllowedErrorBuilderTest {
    @Test
    public void test() {
        SubmitLockedEntityNotAllowedErrorBuilder builder = new SubmitLockedEntityNotAllowedErrorBuilder("entityType",
                "entityId", "lockingUser");
        Assert.assertNotNull(builder);
    }
}