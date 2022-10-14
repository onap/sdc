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


import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.versioning.dao.types.Version;

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
        Assert.assertEquals(ErrorCategory.APPLICATION, build.category());
        Assert.assertEquals(String.format(VersionableSubEntityNotFoundErrorBuilder.SUB_ENTITIES_NOT_FOUND_MSG,
            "entityType", "entityId", "containingEntityType", "ContainingEntityId", "0.0"), build.message());
    }
}
