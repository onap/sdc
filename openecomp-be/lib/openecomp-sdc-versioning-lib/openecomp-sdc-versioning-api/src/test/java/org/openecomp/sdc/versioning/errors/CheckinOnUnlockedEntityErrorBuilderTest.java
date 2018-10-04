package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;

public class CheckinOnUnlockedEntityErrorBuilderTest {

    @Test
    public void test() {
        CheckinOnUnlockedEntityErrorBuilder builder = new CheckinOnUnlockedEntityErrorBuilder("entityType",
                "entityId");
        Assert.assertNotNull(builder);
    }
}