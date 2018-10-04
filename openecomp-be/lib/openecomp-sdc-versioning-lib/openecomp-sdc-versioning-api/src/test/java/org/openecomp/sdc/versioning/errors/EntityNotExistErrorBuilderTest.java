package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class EntityNotExistErrorBuilderTest {
    @Test
    public void test() {
        EntityNotExistErrorBuilder builder = new EntityNotExistErrorBuilder("entityType",
                "entityId");
        ErrorCode build = builder.build();
        Assert.assertEquals(VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST, build.id());
        Assert.assertEquals(ErrorCategory.APPLICATION, build.category());
        Assert.assertEquals(String.format(EntityNotExistErrorBuilder.VERSIONABLE_ENTITY_NOT_EXIST_MSG,
                "entityType", "entityId"), build.message());
    }
}