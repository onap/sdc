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

import org.assertj.core.api.Condition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class Conditions {

    public static Condition<List<PropertyDataDefinition>> hasPropertiesWithNames(String ... expectedPropsName) {
        return new Condition<List<PropertyDataDefinition>>(){
            public boolean matches(List<PropertyDataDefinition> props) {
                List<String> propsNames = props.stream().map(PropertyDataDefinition::getName).collect(Collectors.toList());
                return propsNames.containsAll(asList(expectedPropsName));
            }
        };
    }

}
