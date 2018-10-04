package org.openecomp.sdc.versioning.errors;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class EntityAlreadyFinalizedErrorBuilderTest {
    @Test
    public void test() {

        EntityAlreadyFinalizedErrorBuilder builder = new EntityAlreadyFinalizedErrorBuilder("entityType",
                "entityId");
        ErrorCode build = builder.build();
        Assert.assertEquals(VersioningErrorCodes.SUBMIT_FINALIZED_ENTITY_NOT_ALLOWED, build.id());
        Assert.assertEquals(ErrorCategory.APPLICATION, build.category());
        Assert.assertEquals(String.format("Versionable entity %s with id %s can not be submitted since it is already final.",
                "entityType", "entityId"), build.message());
    }
}