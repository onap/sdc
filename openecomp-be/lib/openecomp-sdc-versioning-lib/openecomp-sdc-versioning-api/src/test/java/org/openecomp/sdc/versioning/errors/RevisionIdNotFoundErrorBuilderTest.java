package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class RevisionIdNotFoundErrorBuilderTest {
    @Test
    public void test() {
        RevisionIdNotFoundErrorBuilder builder = new RevisionIdNotFoundErrorBuilder();
        ErrorCode build = builder.build();
        Assert.assertEquals(VersioningErrorCodes.MANDATORY_FIELD_REVISION_ID_MISSING, build.id());
        Assert.assertEquals(ErrorCategory.APPLICATION, build.category());
        Assert.assertEquals("Mandatory field revision id missing", build.message());
    }
}