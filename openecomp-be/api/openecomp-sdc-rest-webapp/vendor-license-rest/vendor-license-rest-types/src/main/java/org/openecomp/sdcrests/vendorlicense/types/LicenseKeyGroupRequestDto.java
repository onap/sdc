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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyType;
import org.openecomp.sdc.vendorlicense.dao.types.OperationalScope;
import org.openecomp.sdc.vendorlicense.dao.types.ThresholdUnit;

@Data
public class LicenseKeyGroupRequestDto {

    @NotNull
    @Size(max = 120)
    private String name;
    @Size(max = 100)
    private String manufacturerReferenceNumber;
    @Size(max = 1000)
    private String description;
    @NotNull
    private LicenseKeyType type;
    @Valid
    private MultiChoiceOrOtherDto<OperationalScope> operationalScope;
    private String startDate;
    private String expiryDate;
    private Integer thresholdValue;
    private ThresholdUnit thresholdUnits;
    @Size(max = 120)
    private String increments;

    public void setName(final String name) {
        this.name = ValidationUtils.sanitizeInputString(name);
    }

    public void setManufacturerReferenceNumber(final String manufacturerReferenceNumber) {
        this.manufacturerReferenceNumber = ValidationUtils.sanitizeInputString(manufacturerReferenceNumber);
    }

    public void setDescription(final String description) {
        this.description = ValidationUtils.sanitizeInputString(description);
    }

    public void setIncrements(final String increments) {
        this.increments = ValidationUtils.sanitizeInputString(increments);
    }
}
