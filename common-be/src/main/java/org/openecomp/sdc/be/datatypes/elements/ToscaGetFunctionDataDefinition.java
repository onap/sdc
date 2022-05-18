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

package org.openecomp.sdc.be.datatypes.elements;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.openecomp.sdc.be.datatypes.enums.PropertySource;
import org.openecomp.sdc.be.datatypes.tosca.ToscaGetFunctionType;

@Data
public class ToscaGetFunctionDataDefinition {

    private String propertyUniqueId;
    private String propertyName;
    private PropertySource propertySource;
    private String sourceUniqueId;
    private String sourceName;
    private ToscaGetFunctionType functionType;
    private List<String> propertyPathFromSource = new ArrayList<>();

    public ToscaGetFunctionDataDefinition() {
        //necessary for JSON conversions
    }

    public boolean isSubProperty() {
        return propertyPathFromSource != null && propertyPathFromSource.size() > 1;
    }
}
