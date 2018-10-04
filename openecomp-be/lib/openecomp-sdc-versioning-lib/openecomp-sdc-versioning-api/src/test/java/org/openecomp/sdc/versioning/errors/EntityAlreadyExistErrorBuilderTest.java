package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;

public class EntityAlreadyExistErrorBuilderTest {
    @Test
    public void test() {
        EntityAlreadyExistErrorBuilder builder = new EntityAlreadyExistErrorBuilder("entityType",
                "entityId");
        Assert.assertNotNull(builder);
    }
}