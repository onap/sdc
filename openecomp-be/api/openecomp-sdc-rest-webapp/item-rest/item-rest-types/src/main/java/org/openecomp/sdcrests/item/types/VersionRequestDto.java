/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdcrests.item.types;

import lombok.Data;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.versioning.types.VersionCreationMethod;

@Data
public class VersionRequestDto {
    public String getDescription() {
        return description;
    }

    public VersionCreationMethod getCreationMethod() {
        return creationMethod;
    }

    public void setCreationMethod(VersionCreationMethod creationMethod) {
        this.creationMethod = creationMethod;
    }

    private String description;
    private VersionCreationMethod creationMethod;

    public void setDescription(String description) {
        this.description = ValidationUtils.sanitizeInputString(description);
    }
}
