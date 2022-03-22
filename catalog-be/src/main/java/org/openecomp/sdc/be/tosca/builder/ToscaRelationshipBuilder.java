/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.tosca.builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.model.CapabilityRequirementRelationship;
import org.openecomp.sdc.be.tosca.model.ToscaInterfaceDefinition;
import org.openecomp.sdc.be.tosca.model.ToscaOperationAssignment;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyAssignment;
import org.openecomp.sdc.be.tosca.model.ToscaRelationship;
import org.openecomp.sdc.be.tosca.utils.InterfaceTypesNameUtil;
import org.openecomp.sdc.be.ui.model.OperationUi;

public class ToscaRelationshipBuilder {

    public ToscaRelationship from(final CapabilityRequirementRelationship capabilityRequirementRelationship) {
        final ToscaRelationship toscaRelationship = new ToscaRelationship();
        final List<OperationUi> operations = capabilityRequirementRelationship.getOperations();
        toscaRelationship.setType(capabilityRequirementRelationship.getRelation().getRelationship().getType());
        final Map<String, List<OperationUi>> operationsByInterfaceType = operations.stream()
            .collect(Collectors.groupingBy(OperationUi::getInterfaceType));
        final Map<String, ToscaInterfaceDefinition> interfaceMap = new HashMap<>();
        for (final Entry<String, List<OperationUi>> interfaceTypeEntry : operationsByInterfaceType.entrySet()) {
            final ToscaInterfaceDefinition toscaInterfaceDefinition = new ToscaInterfaceDefinition();
            final String interfaceType = interfaceTypeEntry.getKey();
            final Map<String, Object> operationDefinitionMap = new HashMap<>();
            for (final OperationUi operationUi : interfaceTypeEntry.getValue()) {
                final ToscaOperationAssignment toscaOperationAssignment = new ToscaOperationAssignment();
                toscaOperationAssignment.setImplementation(operationUi.getImplementation());
                if (CollectionUtils.isNotEmpty(operationUi.getInputs())) {
                    final Map<String, ToscaPropertyAssignment> inputMap = new HashMap<>();
                    operationUi.getInputs().forEach(propertyAssignmentUi -> {
                        final ToscaPropertyAssignment toscaProperty = new ToscaPropertyAssignment();
                        toscaProperty.setValue(propertyAssignmentUi.getValue());
                        inputMap.put(propertyAssignmentUi.getName(), toscaProperty);
                    });
                    toscaOperationAssignment.setInputs(inputMap);
                }
                operationDefinitionMap.put(operationUi.getOperationType(), toscaOperationAssignment);
            }
            toscaInterfaceDefinition.setOperations(operationDefinitionMap);
            interfaceMap.put(interfaceType, toscaInterfaceDefinition);
        }
        toscaRelationship.setInterfaces(interfaceMap);
        return toscaRelationship;
    }
}
