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

import static org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes.FAILED_TO_CREATE_VSP;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;


public class VendorSoftwareProductCreationFailedBuilderTest {

    private static final String VSP_ID = "testVsp1";
    private VendorSoftwareProductCreationFailedBuilder vendorSoftwareProductCreationFailedBuilder;

    @Before
    public void setUp() {
        vendorSoftwareProductCreationFailedBuilder = new VendorSoftwareProductCreationFailedBuilder(VSP_ID);
    }

    @Test
    public void shouldReturnVspNotFoundErrorCode() {
        ErrorCode actual = vendorSoftwareProductCreationFailedBuilder.build();
        Assert.assertEquals(ErrorCategory.APPLICATION, actual.category());
        Assert.assertEquals(FAILED_TO_CREATE_VSP, actual.id());
    }
}
