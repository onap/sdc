package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.common.errors.BaseErrorBuilder;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;


public class CheckinOnEntityLockedByOtherErrorBuilderTest {

    @Test
    public void test() {
        CheckinOnEntityLockedByOtherErrorBuilder builder = new CheckinOnEntityLockedByOtherErrorBuilder("entityType",
                "entityId", "lockingUser");
        ErrorCode build = builder.build();

        Assert.assertEquals(VersioningErrorCodes.CHECKIN_ON_ENTITY_LOCKED_BY_OTHER_USER, build.id());
        Assert.assertEquals(ErrorCategory.APPLICATION, build.category());
        Assert.assertEquals(String.format("Can not check in versionable entity %s with id %s since it is checked out by other "
                        + "user: %s.", "entityType", "entityId", "lockingUser") ,  build.message());
    }

}
