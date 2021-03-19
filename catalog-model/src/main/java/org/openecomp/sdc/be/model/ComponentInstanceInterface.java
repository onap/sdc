/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
package org.openecomp.sdc.be.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InterfaceInstanceDataDefinition;

@Getter
@Setter
@NoArgsConstructor
public class ComponentInstanceInterface extends InterfaceDefinition {

    private String interfaceId;
    private InterfaceInstanceDataDefinition interfaceInstanceDataDefinition;

    public ComponentInstanceInterface(String interfaceId, InterfaceInstanceDataDefinition interfaceInstanceDataDefinition) {
        this.interfaceId = interfaceId;
        this.interfaceInstanceDataDefinition = interfaceInstanceDataDefinition;
    }

    public ComponentInstanceInterface(String interfaceId, InterfaceDataDefinition interfaceDataDefinition) {
        super(interfaceDataDefinition);
        this.interfaceId = interfaceId;
    }
}
