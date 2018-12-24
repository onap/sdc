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
package org.openecomp.sdc.be.components.utils;

import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;

public class InterfaceOperationUtils {

    private InterfaceOperationUtils(){}

    public static final Optional<InterfaceDefinition> getInterfaceDefinitionFromComponentByInterfaceType(Component component, String interfaceType) {
        if (MapUtils.isEmpty(component.getInterfaces())) {
            return Optional.empty();
        }
        return component.getInterfaces().values().stream().filter(interfaceDefinition -> interfaceDefinition.getType() != null && interfaceDefinition.getType().equals(interfaceType)).findAny();
    }

    public static final Optional<InterfaceDefinition> getInterfaceDefinitionFromComponentByInterfaceId(Component component, String interfaceId) {
        if (MapUtils.isEmpty(component.getInterfaces())) {
            return Optional.empty();
        }
        return component.getInterfaces().values().stream().filter(interfaceDefinition -> interfaceDefinition.getUniqueId() != null && interfaceDefinition.getUniqueId().equals(interfaceId)).findAny();
    }

    public static final Optional<Map.Entry<String, Operation>> getOperationFromInterfaceDefinition(InterfaceDefinition interfaceDefinition, String operationId) {
        if (MapUtils.isEmpty(interfaceDefinition.getOperationsMap())) {
            return Optional.empty();
        }
        Optional<Map.Entry<String, Operation>> operationMap = interfaceDefinition.getOperationsMap().entrySet().stream().filter(entry -> entry.getValue().getUniqueId().equals(operationId)).findAny();
        if (operationMap.isPresent()) {
            return operationMap;
        }
        return Optional.empty();
    }

}
