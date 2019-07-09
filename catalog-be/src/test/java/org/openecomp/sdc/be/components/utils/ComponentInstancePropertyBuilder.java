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

package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.model.ComponentInstanceProperty;

public class ComponentInstancePropertyBuilder extends PropertyDataDefinitionAbstractBuilder<ComponentInstanceProperty, ComponentInstancePropertyBuilder> {

    @Override
    protected PropertyDataDefinitionAbstractBuilder<ComponentInstanceProperty, ComponentInstancePropertyBuilder> self() {
        return this;
    }

    @Override
    ComponentInstanceProperty propertyDefinition() {
        return new ComponentInstanceProperty();
    }

    @Override
    public ComponentInstanceProperty build() {
        return propertyDefinition;
    }

}
