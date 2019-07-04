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
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;

public class NicInternalNetworkErrorBuilderTest {

    @Test
    public void testGetNetworkDescriptionInternalNetworkErrorBuilder() {
        //when
        ErrorCode errorCode = NicInternalNetworkErrorBuilder.getNetworkDescriptionInternalNetworkErrorBuilder();

        //then
        assertEquals(VendorSoftwareProductErrorCodes.NETWORK_DESCRIPTION_NOT_ALLOWED_FOR_INTERNAL_NETWORK,
                errorCode.id());
        assertEquals(ErrorCategory.APPLICATION, errorCode.category());
        assertEquals("Invalid request, Network Description not allowed for Internal Networks", errorCode.message());
    }
}
