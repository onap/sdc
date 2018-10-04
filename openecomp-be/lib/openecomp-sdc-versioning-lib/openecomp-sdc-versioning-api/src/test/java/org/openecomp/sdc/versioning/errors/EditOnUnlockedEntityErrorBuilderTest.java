package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class EditOnUnlockedEntityErrorBuilderTest {
    @Test
    public void test() {
        EditOnUnlockedEntityErrorBuilder builder = new EditOnUnlockedEntityErrorBuilder("entityType",
                "entityId");

        ErrorCode build = builder.build();
        Assert.assertEquals(VersioningErrorCodes.EDIT_ON_UNLOCKED_ENTITY, build.id());
        Assert.assertEquals(ErrorCategory.APPLICATION, build.category());
        Assert.assertEquals(String.format("Can not edit versionable entity %s with id %s since it is not checked out.",
                "entityType", "entityId"), build.message());
    }
}