/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.versioning.errors;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;


public class DeleteOnLockedEntityErrorBuilderTest {

    @Test
    public void test() {
        DeleteOnLockedEntityErrorBuilder builder = new DeleteOnLockedEntityErrorBuilder("entityType",
            "entityId", "lockingUser");

        ErrorCode build = builder.build();
        Assert.assertEquals(VersioningErrorCodes.DELETE_ON_LOCKED_ENTITY, build.id());
        Assert.assertEquals(ErrorCategory.APPLICATION, build.category());
        Assert.assertEquals(String.format(DeleteOnLockedEntityErrorBuilder.DELETE_ON_LOCKED_ENTITY_MSG,
            "entityType", "entityId", "lockingUser"), build.message());
    }
}
