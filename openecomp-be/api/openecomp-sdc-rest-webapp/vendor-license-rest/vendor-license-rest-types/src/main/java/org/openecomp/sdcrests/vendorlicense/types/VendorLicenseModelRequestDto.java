/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdcrests.vendorlicense.types;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import org.openecomp.sdc.common.util.ValidationUtils;

@Data
@Schema(description = "VendorLicenseModelRequest")
public class VendorLicenseModelRequestDto {

    @NotNull
    @Size(max = 25)
    private String vendorName;
    @NotNull
    @Size(max = 1000)
    private String description;
    @NotNull
    private String iconRef;

    @Size(max = 25)
    private String tenant;

    public void setVendorName(final String vendorName) {
        this.vendorName = ValidationUtils.sanitizeInputString(vendorName);
    }

    public void setDescription(final String description) {
        this.description = ValidationUtils.sanitizeInputString(description);
    }

    public void setTenant(final String tenant) {
        if(tenant != null){
            this.tenant = ValidationUtils.sanitizeInputString(tenant);
        }
        else this.tenant=tenant;
    }
}
