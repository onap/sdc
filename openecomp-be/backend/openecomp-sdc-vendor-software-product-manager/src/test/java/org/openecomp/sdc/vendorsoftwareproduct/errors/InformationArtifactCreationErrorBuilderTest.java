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

public class InformationArtifactCreationErrorBuilderTest {


    @Test
    public void testBuild() {
        //given
        InformationArtifactCreationErrorBuilder informationArtifactCreationErrorBuilder =
            new InformationArtifactCreationErrorBuilder("1");

        //when
        ErrorCode errorCode = informationArtifactCreationErrorBuilder.build();

        //then
        assertEquals(VendorSoftwareProductErrorCodes.HEAT_PACKAGE_FILE_CREATION, errorCode.id());
        assertEquals(ErrorCategory.SYSTEM, errorCode.category());
        assertEquals("Error while trying to information artifact file for vendor software product with Id 1.",
            errorCode.message());
    }
}
