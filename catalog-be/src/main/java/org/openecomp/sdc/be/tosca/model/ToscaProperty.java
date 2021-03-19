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
package org.openecomp.sdc.be.tosca.model;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class ToscaProperty {

    private Object _defaultp_;
    @Getter
    @Setter
    private Object value;
    @Getter
    @Setter
    private String type;
    @Getter
    @Setter
    private String description;
    @Getter
    @Setter
    private Boolean required;
    @Getter
    @Setter
    private ToscaSchemaDefinition entry_schema;
    @Getter
    @Setter
    private List<ToscaPropertyConstraint> constraints;
    @Getter
    @Setter
    private String status;
    @Getter
    @Setter
    private Map<String, String> metadata;

    public ToscaProperty(final ToscaProperty toscaProperty) {
        this.type = toscaProperty.type;
        this._defaultp_ = toscaProperty._defaultp_;
        this.description = toscaProperty.description;
        this.required = toscaProperty.required;
        this.entry_schema = toscaProperty.entry_schema;
        this.status = toscaProperty.status;
        this.constraints = toscaProperty.constraints;
        this.metadata = toscaProperty.metadata;
    }

    public Object getDefaultp() {
        return _defaultp_;
    }

    public void setDefaultp(Object defaultp) {
        this._defaultp_ = defaultp;
    }
}
