package org.openecomp.sdc.versioning.errors;

import org.junit.Assert;
import org.junit.Test;

public class EntityAlreadyFinalizedErrorBuilderTest {
    @Test
    public void test() {

        EntityAlreadyFinalizedErrorBuilder builder = new EntityAlreadyFinalizedErrorBuilder("entityType",
                "entityId");
        Assert.assertNotNull(builder);
    }
}