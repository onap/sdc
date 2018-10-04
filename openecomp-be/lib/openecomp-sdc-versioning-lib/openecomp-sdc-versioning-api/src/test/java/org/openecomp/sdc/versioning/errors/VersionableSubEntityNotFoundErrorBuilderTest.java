package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Arrays;

public class VersionableSubEntityNotFoundErrorBuilderTest {
    @Test
    public void test() {
        VersionableSubEntityNotFoundErrorBuilder builder = new VersionableSubEntityNotFoundErrorBuilder("entityType",
                "entityId", "containingEntityType", "ContainingEntityId", new Version("0.0"));
        ErrorCode build = builder.build();
        Assert.assertEquals(VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND, build.id());
        Assert.assertEquals(ErrorCategory.APPLICATION, build.category());
        Assert.assertEquals(String.format(VersionableSubEntityNotFoundErrorBuilder.SUB_ENTITY_NOT_FOUND_MSG,
                "entityType", "entityId", "containingEntityType", "ContainingEntityId", "0.0"), build.message());
    }

    @Test
    public void testWithListOfIds() {
        VersionableSubEntityNotFoundErrorBuilder builder = new VersionableSubEntityNotFoundErrorBuilder("entityType",
                Arrays.asList("entityId"), "containingEntityType", "ContainingEntityId", new Version("0.0"));
        ErrorCode build = builder.build();
        Assert.assertEquals(VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND, build.id());
        Assert.assertEquals(ErrorCategory.APPLICATION, build.category() );
        Assert.assertEquals(String.format(VersionableSubEntityNotFoundErrorBuilder.SUB_ENTITIES_NOT_FOUND_MSG,
                "entityType", "entityId", "containingEntityType", "ContainingEntityId", "0.0"), build.message());
    }
}