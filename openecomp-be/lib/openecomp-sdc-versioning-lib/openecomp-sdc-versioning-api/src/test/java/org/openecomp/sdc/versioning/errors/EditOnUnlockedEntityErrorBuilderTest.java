package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;

public class EditOnUnlockedEntityErrorBuilderTest {
    @Test
    public void test() {
        EditOnUnlockedEntityErrorBuilder builder = new EditOnUnlockedEntityErrorBuilder("entityType",
                "entityId");
        Assert.assertNotNull(builder);
    }
}