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

import org.openecomp.sdc.be.model.HeatParameterDefinition;

public class HeatParameterBuilder {

    private HeatParameterDefinition heatParameterDefinition;

    public HeatParameterBuilder() {
        heatParameterDefinition = new HeatParameterDefinition();
    }

    public HeatParameterBuilder setName(String name) {
        heatParameterDefinition.setName(name);
        return this;
    }

    public HeatParameterBuilder setType(String type) {
        heatParameterDefinition.setType(type);
        return this;
    }

    public HeatParameterBuilder setCurrentValue(String value) {
        heatParameterDefinition.setCurrentValue(value);
        return this;
    }

    public HeatParameterBuilder setDefaultValue(String value) {
        heatParameterDefinition.setDefaultValue(value);
        return this;
    }

    public HeatParameterDefinition build() {
        return heatParameterDefinition;
    }
}
