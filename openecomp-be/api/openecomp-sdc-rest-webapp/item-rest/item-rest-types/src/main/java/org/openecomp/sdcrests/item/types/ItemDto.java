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

import java.util.Map;
import lombok.Data;
import org.openecomp.sdc.common.util.ValidationUtils;

@Data
public class ItemDto {
    private String id;
    private String type;
    private String name;
    private String description;
    private String owner;
    private String status;
    private String tenant;
    private Map<String, Object> properties;

    public void setId(final String id) {
        this.id = ValidationUtils.sanitizeInputString(id);
    }

    public void setType(final String type) {
        this.type = ValidationUtils.sanitizeInputString(type);
    }

    public void setName(final String name) {
        this.name = ValidationUtils.sanitizeInputString(name);
    }

    public void setDescription(final String description) {
        this.description = ValidationUtils.sanitizeInputString(description);
    }

    public void setOwner(final String owner) {
        this.owner = ValidationUtils.sanitizeInputString(owner);
    }

    public void setStatus(final String status) {
        this.status = ValidationUtils.sanitizeInputString(status);
    }

    public void setTenant(final String tenant) { this.tenant = ValidationUtils.sanitizeInputString(tenant); }

}

