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
package org.openecomp.sdc.be.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class ComponentInstancePropInput extends ComponentInstanceProperty {

    private String propertiesName;
    private PropertyDefinition input;

    public ComponentInstancePropInput() {
        super();
    }

    public ComponentInstancePropInput(ComponentInstanceProperty p) {
        super(p);
    }

    public ComponentInstancePropInput(PropertyDefinition p) {
        super(p);
    }

    public String[] getParsedPropNames() {
        String[] tokens = null;
        if (propertiesName != null && !propertiesName.isEmpty()) {
            tokens = propertiesName.split("#");
        }
        return tokens;
    }
}
