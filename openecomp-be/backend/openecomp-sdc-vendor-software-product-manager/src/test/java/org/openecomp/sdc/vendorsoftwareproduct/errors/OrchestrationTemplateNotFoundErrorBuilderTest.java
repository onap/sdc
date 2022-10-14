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

public class OrchestrationTemplateNotFoundErrorBuilderTest {

    @Test
    public void testBuild() {
        //given
        OrchestrationTemplateNotFoundErrorBuilder orchestrationTemplateNotFoundErrorBuilder =
            new OrchestrationTemplateNotFoundErrorBuilder("1");

        //when
        ErrorCode errorCode = orchestrationTemplateNotFoundErrorBuilder.build();

        //then
        assertEquals(VendorSoftwareProductErrorCodes.ORCHESTRATION_NOT_FOUND, errorCode.id());
        assertEquals(ErrorCategory.APPLICATION, errorCode.category());
        assertEquals("Failed to get orchestration template for VSP with id 1", errorCode.message());
    }
}
