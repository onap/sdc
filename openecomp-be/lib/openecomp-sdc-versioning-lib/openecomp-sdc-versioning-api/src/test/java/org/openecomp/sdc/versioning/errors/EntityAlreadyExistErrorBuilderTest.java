package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class EntityAlreadyExistErrorBuilderTest {
    @Test
    public void test() {
        EntityAlreadyExistErrorBuilder builder = new EntityAlreadyExistErrorBuilder("entityType",
                "entityId");
        ErrorCode build = builder.build();
        Assert.assertEquals(VersioningErrorCodes.VERSIONABLE_ENTITY_ALREADY_EXIST, build.id());
        Assert.assertEquals(ErrorCategory.APPLICATION, build.category());
        Assert.assertEquals(String.format(EntityAlreadyExistErrorBuilder.VERSIONABLE_ENTITY_ALREADY_EXIST_MSG,
                "entityType", "entityId"), build.message());
    }
}