/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Samsung. All rights reserved.
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

package org.openecomp.sdc.vendorsoftwareproduct.errors;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.versioning.dao.types.Version;

public class PackageInvalidErrorBuilderTest {

    @Test
    public void testBuild() {
        //given
        PackageInvalidErrorBuilder packageInvalidErrorBuilder =
            new PackageInvalidErrorBuilder("1", Version.valueOf("1.0"));

        //when
        ErrorCode errorCode = packageInvalidErrorBuilder.build();

        //then
        assertEquals(VendorSoftwareProductErrorCodes.PACKAGE_INVALID, errorCode.id());
        assertEquals(ErrorCategory.APPLICATION, errorCode.category());
        assertEquals(
            "Package for vendor software product with Id 1 and version 1.0 is invalid (does not contain translated data).",
            errorCode.message());
    }
}
