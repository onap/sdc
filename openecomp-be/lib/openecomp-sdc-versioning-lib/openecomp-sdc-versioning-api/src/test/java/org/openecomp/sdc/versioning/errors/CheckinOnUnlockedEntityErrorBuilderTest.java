package org.openecomp.sdc.versioning.errors;


import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class CheckinOnUnlockedEntityErrorBuilderTest {

    @Test
    public void test() {
        CheckinOnUnlockedEntityErrorBuilder builder = new CheckinOnUnlockedEntityErrorBuilder("entityType",
                "entityId");

        ErrorCode build = builder.build();
        Assert.assertEquals(VersioningErrorCodes.CHECKIN_ON_UNLOCKED_ENTITY, build.id());
        Assert.assertEquals(ErrorCategory.APPLICATION, build.category());
        Assert.assertEquals(String.format(CheckinOnUnlockedEntityErrorBuilder.CHECKIN_ON_UNLOCKED_ENTITY_MSG,
                "entityType", "entityId"), build.message());
    }
}