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

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.versioning.dao.types.Version;

class VendorSoftwareProductInvalidErrorBuilderTest {

    @Test
    void testVendorSoftwareProductMissingServiceModelErrorBuilder() {
        //when
        ErrorCode errorCode = VendorSoftwareProductInvalidErrorBuilder
            .vendorSoftwareProductMissingServiceModelErrorBuilder("1",
                Version.valueOf("1.1"));

        //then
        assertEquals(VendorSoftwareProductErrorCodes.VSP_INVALID, errorCode.id());
        assertEquals(ErrorCategory.APPLICATION, errorCode.category());
        assertEquals("Vendor software product with Id 1 and version null is invalid - does not contain service model.",
            errorCode.message());
    }

    @Test
    void testVspMissingDeploymentFlavorErrorBuilder() {
        //when
        ErrorCode errorCode = VendorSoftwareProductInvalidErrorBuilder.vspMissingDeploymentFlavorErrorBuilder();

        //then
        assertEquals(VendorSoftwareProductErrorCodes.VSP_INVALID, errorCode.id());
        assertEquals(ErrorCategory.APPLICATION, errorCode.category());
        assertEquals(
            "VSP has to have a minimum of one Deployment Flavor defined for being able to be instantiated.Please add a "
                + "Deployment Flavor and re-submit the VSP.", errorCode.message());
    }
}
