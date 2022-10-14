/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.sdc.vendorsoftwareproduct.dao.errors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openecomp.sdc.common.errors.Messages.VSP_VERSION_NOT_FOUND;
import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.VSP_NOT_FOUND;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;

class VendorSoftwareProductNotFoundErrorBuilderTest {

    @Test
    void shouldReturnVspNotFoundErrorCode() {
        final var vendorSoftwareProductNotFoundErrorBuilder = new VendorSoftwareProductNotFoundErrorBuilder("testVsp1");
        ErrorCode actual = vendorSoftwareProductNotFoundErrorBuilder.build();
        assertEquals(ErrorCategory.APPLICATION, actual.category());
        assertEquals(VSP_NOT_FOUND, actual.id());
    }

    @Test
    void vspIdAndVspVersionIdConstructorTest() {
        var vspId = "vspId";
        var vspVersionId = "vspVersionId";
        final var errorBuilder = new VendorSoftwareProductNotFoundErrorBuilder(vspId, vspVersionId);
        final ErrorCode actualErrorCode = errorBuilder.build();
        assertEquals(ErrorCategory.APPLICATION, actualErrorCode.category());
        assertEquals(VSP_NOT_FOUND, actualErrorCode.id());
        final String expectedMsg = VSP_VERSION_NOT_FOUND.formatMessage(vspId, vspVersionId);
        assertEquals(expectedMsg, actualErrorCode.message());
    }
}
