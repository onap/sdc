package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;

public class EntityNotExistErrorBuilderTest {
    @Test
    public void test() {
        EntityNotExistErrorBuilder builder = new EntityNotExistErrorBuilder("entityType",
                "entityId");
        Assert.assertNotNull(builder);
    }
}