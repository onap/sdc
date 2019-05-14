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


import fj.data.Either;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.types.ServiceConsumptionData;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openecomp.sdc.be.components.property.CapabilityTestUtils.createCapabilityDefinition;
import static org.openecomp.sdc.be.components.property.CapabilityTestUtils.createProperties;

public class ConsumptionUtilsTest {

    @Test
    public void testHandleConsumptionInputMappedToCapabilityProperty() {

        Operation operation = new Operation();
        operation.setUniqueId("uniqueId");
        OperationInputDefinition operationInputDefinition = new OperationInputDefinition();
        operationInputDefinition.setUniqueId("uniqueId");
        operationInputDefinition.setInputId("uniqueId");
        operationInputDefinition.setType("Integer");

        List<OperationInputDefinition> operationInputDefinitions = new ArrayList<>();
        operationInputDefinitions.add(operationInputDefinition);
        ListDataDefinition<OperationInputDefinition> listDataDefinition = new ListDataDefinition<>(operationInputDefinitions);
        operation.setInputs(listDataDefinition);
        CapabilityDefinition capabilityDefinition = createCapabilityDefinition();
        ServiceConsumptionData serviceConsumptionData = new ServiceConsumptionData();
        serviceConsumptionData.setInputId("uniqueId");
        serviceConsumptionData.setValue(capabilityDefinition.getName() + "_prop_name" );

        List<ComponentInstanceProperty> capPropList = new ArrayList<>();
        ComponentInstanceProperty instanceProperty = createProperties();
        capPropList.add(instanceProperty);
        capabilityDefinition.setProperties(capPropList);

        capabilityDefinition.setPath(Collections.singletonList("path"));
        Map<String, List<CapabilityDefinition>> capabilityMap = new HashMap<>();
        capabilityMap.put(capabilityDefinition.getType(), Collections.singletonList(capabilityDefinition));

        Either<Operation, ResponseFormat> operationResponseFormatEither = ConsumptionUtils
                .handleConsumptionInputMappedToCapabilityProperty(operation, operationInputDefinition,
                        serviceConsumptionData, capabilityMap, "componentName");

        Assert.assertTrue(operationResponseFormatEither.isLeft());
    }
}