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
package org.onap.sdc.tosca.datatypes.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class AttributeDefinition implements Cloneable {

    private String type;
    private String description;
    @JsonProperty("default")
    private Object _default;
    private String status;
    private EntrySchema entry_schema;

    public AttributeDefinition() {
        status = Status.SUPPORTED.getName();
    }

    public AttributeDefinition(final String type, final String description, final Object _default, final String status,
                               final EntrySchema entry_schema) {
        this.setType(type);
        this.setDescription(description);
        this.set_default(_default);
        this.setStatus(StringUtils.isEmpty(status) ? Status.SUPPORTED.getName() : status);
        this.setEntry_schema(entry_schema);
    }

    @Override
    public AttributeDefinition clone() {
        AttributeDefinition attributeDefinition = new AttributeDefinition();
        attributeDefinition.setType(this.getType());
        attributeDefinition.setDescription(this.getDescription());
        attributeDefinition.set_default(this.get_default());
        attributeDefinition.setStatus(this.getStatus());
        attributeDefinition.setEntry_schema(Objects.isNull(this.getEntry_schema()) ? null : this.getEntry_schema().clone());
        return attributeDefinition;
    }
}
