package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Arrays;

public class VersionableSubEntityNotFoundErrorBuilderTest {
    @Test
    public void test() {
        VersionableSubEntityNotFoundErrorBuilder builder = new VersionableSubEntityNotFoundErrorBuilder("entityType",
                "entityId", "containingEntityType", "ContainingEntityId", new Version());
        Assert.assertNotNull(builder);
    }

    @Test
    public void testWithListOfIds() {
        VersionableSubEntityNotFoundErrorBuilder builder = new VersionableSubEntityNotFoundErrorBuilder("entityType",
                Arrays.asList("entityId"), "containingEntityType", "ContainingEntityId", new Version());
        Assert.assertNotNull(builder);
    }
}