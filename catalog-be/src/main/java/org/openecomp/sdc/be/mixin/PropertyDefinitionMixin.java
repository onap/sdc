/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.mixin;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.view.Mixin;
import org.openecomp.sdc.be.view.MixinTarget;

import java.util.List;

@MixinTarget(target = PropertyDefinition.class)
public abstract class PropertyDefinitionMixin extends Mixin {

    @JsonProperty
    abstract List<Annotation> getAnnotations();
    @JsonProperty
    abstract String getDefaultValue();
    @JsonProperty
    abstract String getDescription();
    @JsonProperty
    abstract List<GetInputValueDataDefinition> getGetInputValues();
    @JsonProperty
    abstract String getInputId();
    @JsonProperty
    abstract String getInputPath();
    @JsonProperty
    abstract String getInstanceUniqueId();
    @JsonProperty
    abstract String getLabel();
    @JsonProperty
    abstract String getName();
    @JsonProperty
    abstract String getParentUniqueId();
    @JsonProperty
    abstract String getPropertyId();
    @JsonProperty
    abstract SchemaDefinition getSchema();
    @JsonProperty
    abstract SchemaDefinition getSchemaProperty();
    @JsonProperty
    abstract String getSchemaType();
    @JsonProperty
    abstract String getStatus();
    @JsonProperty
    abstract String getType();
    @JsonProperty
    abstract String getUniqueId();
    @JsonProperty
    abstract String getValue();
    @JsonProperty
    abstract boolean isGetInputProperty();
    @JsonProperty
    abstract List<PropertyConstraint> getConstraints();
}
