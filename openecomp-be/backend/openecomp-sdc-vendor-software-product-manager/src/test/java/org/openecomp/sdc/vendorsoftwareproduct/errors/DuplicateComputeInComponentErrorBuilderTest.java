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

public class DuplicateComputeInComponentErrorBuilderTest {

    @Test
    public void testGetComputeHeatReadOnlyErrorBuilder() {
        //when
        ErrorCode errorCode = DuplicateComputeInComponentErrorBuilder.getComputeHeatReadOnlyErrorBuilder("");

        //then
        assertEquals(VendorSoftwareProductErrorCodes.UPDATE_COMPUTE_NOT_ALLOWED, errorCode.id());
        assertEquals(ErrorCategory.APPLICATION, errorCode.category());
        assertEquals("Update of attribute  not allowed for VSP onboarded via HEAT.", errorCode.message());

    }

    @Test
    public void testBuild() {
        // given
        DuplicateComputeInComponentErrorBuilder duplicateComputeInComponentErrorBuilder =
            new DuplicateComputeInComponentErrorBuilder();

        // when
        ErrorCode errorCode = duplicateComputeInComponentErrorBuilder.build();

        // then
        assertEquals(ErrorCategory.APPLICATION, errorCode.category());
    }
}
