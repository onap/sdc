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

import java.util.Date;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.openecomp.sdc.versioning.dao.types.VersionState;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;

@Data
@NoArgsConstructor
public class VersionDto {

    private String id;
    private String name;
    private String description;
    private String baseId;
    private VersionStatus status;
    private VersionState state;
    private Date creationTime;
    private Date modificationTime;
    private Map<String, Object> additionalInfo;

    public void setId(final String id) {
        this.id = ValidationUtils.sanitizeInputString(id);
    }

    public void setName(final String name) {
        this.name = ValidationUtils.sanitizeInputString(name);
    }

    public void setDescription(final String description) {
        this.description = ValidationUtils.sanitizeInputString(description);
    }

    public void setBaseId(final String baseId) {
        this.baseId = ValidationUtils.sanitizeInputString(baseId);
    }
}
