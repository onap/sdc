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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import org.openecomp.sdc.common.util.ValidationUtils;

@Data
public class FeatureGroupDescriptorDto {

    @NotNull
    @Size(max = 120)
    private String name;
    @Size(max = 1000)
    private String description;
    @NotNull
    private String partNumber;

    public void setName(final String name) {
        this.name = ValidationUtils.sanitizeInputString(name);
    }

    public void setDescription(String description) {
        this.description = ValidationUtils.sanitizeInputString(description);
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = ValidationUtils.sanitizeInputString(partNumber);
    }
}
