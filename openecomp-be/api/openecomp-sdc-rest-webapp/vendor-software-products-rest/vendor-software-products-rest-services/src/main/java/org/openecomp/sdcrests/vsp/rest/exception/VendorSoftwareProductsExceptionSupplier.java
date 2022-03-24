/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdcrests.vsp.rest.exception;

import java.util.function.Supplier;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.dao.errors.VendorSoftwareProductNotFoundErrorBuilder;

public class VendorSoftwareProductsExceptionSupplier {

    public static Supplier<CoreException> vspNotFound(final String vspId) {
        final VendorSoftwareProductNotFoundErrorBuilder errorBuilder = new VendorSoftwareProductNotFoundErrorBuilder(vspId);
        return () -> new CoreException((errorBuilder.build()));
    }

}
