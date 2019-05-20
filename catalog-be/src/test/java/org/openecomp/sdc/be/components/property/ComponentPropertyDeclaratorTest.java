/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Fujitsu Limited. All rights reserved.
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

package org.openecomp.sdc.be.components.property;

import fj.data.Either;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ComponentPropertyDeclaratorTest extends PropertyDeclaratorTestBase {

    @InjectMocks
    private ComponentPropertyDeclarator testInstance;
    @Mock
    private PropertyBusinessLogic propertyBusinessLogic;
    @Mock
    private PropertyOperation propertyOperation;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Test
    public void unDeclarePropertiesAsListInputsTest_whenPropertyUsedByOperation() {
        InputDefinition input = new InputDefinition();
        input.setUniqueId(INPUT_ID);
        input.setName(INPUT_ID);
        input.setValue("value");

        PropertyDefinition propertyDefinition = new PropertyDefinition(input);

        when(propertyBusinessLogic.isPropertyUsedByOperation(eq(resource), eq(propertyDefinition))).thenReturn(true);
        StorageOperationStatus status = testInstance.unDeclarePropertiesAsListInputs(resource, input);
        Assert.assertEquals(status, StorageOperationStatus.DECLARED_INPUT_USED_BY_OPERATION);
    }

    @Test
    public void unDeclarePropertiesAsListInputsTest_whenNotPresentPropertyToUpdateCandidate() {
        InputDefinition input = new InputDefinition();
        input.setUniqueId(INPUT_ID);
        input.setName(INPUT_ID);
        input.setValue("value");

        PropertyDefinition propertyDefinition = new PropertyDefinition();
        resource.setProperties(Collections.singletonList(propertyDefinition));

        when(propertyBusinessLogic.isPropertyUsedByOperation(eq(resource), any(PropertyDefinition.class))).thenReturn(false);
        StorageOperationStatus status = testInstance.unDeclarePropertiesAsListInputs(resource, input);
        Assert.assertEquals(status, StorageOperationStatus.OK);
    }

    @Test
    public void unDeclarePropertiesAsListInputsTest_whenPropertiesEmpty() {
        InputDefinition input = new InputDefinition();
        input.setUniqueId(INPUT_ID);
        input.setName(INPUT_ID);
        input.setValue("value");

        resource.setProperties(new ArrayList<>());

        when(propertyBusinessLogic.isPropertyUsedByOperation(eq(resource), any(PropertyDefinition.class))).thenReturn(false);
        StorageOperationStatus status = testInstance.unDeclarePropertiesAsListInputs(resource, input);
        Assert.assertEquals(status, StorageOperationStatus.OK);
    }

    @Test
    public void unDeclarePropertiesAsListInputsTest_whenPropertiesToUpdateIsEmpty() {
        InputDefinition input = new InputDefinition();
        input.setUniqueId(INPUT_ID);
        input.setName(INPUT_ID);
        input.setValue("value");

        PropertyDefinition propertyDefinition = new PropertyDefinition(input);
        resource.setProperties(Collections.singletonList(propertyDefinition));

        when(propertyBusinessLogic.isPropertyUsedByOperation(eq(resource), eq(propertyDefinition))).thenReturn(false);
        StorageOperationStatus status = testInstance.unDeclarePropertiesAsListInputs(resource, input);
        Assert.assertEquals(status, StorageOperationStatus.OK);
    }

    @Test
    public void unDeclarePropertiesAsListInputsTest_singleProperty() {
        InputDefinition input = new InputDefinition();
        input.setUniqueId(INPUT_ID);
        input.setName(INPUT_ID);
        input.setValue("value");
        input.setDefaultValue("default value");

        PropertyDefinition propertyDefinition = new PropertyDefinition(input);
        List<GetInputValueDataDefinition> getInputValueList = new ArrayList<>();
        getInputValueList.add(buildGetInputValue(INPUT_ID));
        getInputValueList.add(buildGetInputValue("otherInputId"));
        propertyDefinition.setGetInputValues(getInputValueList);
        propertyDefinition.setUniqueId("propertyId");
        propertyDefinition.setDefaultValue("default value");
        propertyDefinition.setValue(generateGetInputValueAsListInput(INPUT_ID, "innerPropName"));
        resource.setProperties(Collections.singletonList(propertyDefinition));

        when(propertyBusinessLogic.isPropertyUsedByOperation(eq(resource), any())).thenReturn(false);
        when(propertyOperation.findDefaultValueFromSecondPosition(eq(Collections.emptyList()), eq(propertyDefinition.getUniqueId()), eq(propertyDefinition.getDefaultValue()))).thenReturn(Either.left(propertyDefinition.getDefaultValue()));
        when(toscaOperationFacade.updatePropertyOfComponent(eq(resource), any())).thenReturn(Either.left(propertyDefinition));
        StorageOperationStatus status = testInstance.unDeclarePropertiesAsListInputs(resource, input);
        Assert.assertEquals(status, StorageOperationStatus.OK);
    }

    @Test
    public void unDeclarePropertiesAsListInputsTest_UnDeclareInputFail() {
        InputDefinition input = new InputDefinition();
        input.setUniqueId(INPUT_ID);
        input.setName(INPUT_ID);
        input.setValue("value");
        input.setDefaultValue("default value");

        PropertyDefinition propertyDefinition = new PropertyDefinition(input);
        List<GetInputValueDataDefinition> getInputValueList = new ArrayList<>();
        getInputValueList.add(buildGetInputValue(INPUT_ID));
        getInputValueList.add(buildGetInputValue("otherInputId"));
        propertyDefinition.setGetInputValues(getInputValueList);
        propertyDefinition.setUniqueId("propertyId");
        propertyDefinition.setDefaultValue("default value");
        propertyDefinition.setValue(generateGetInputValueAsListInput(INPUT_ID, "innerPropName"));
        resource.setProperties(Collections.singletonList(propertyDefinition));

        when(propertyBusinessLogic.isPropertyUsedByOperation(eq(resource), any())).thenReturn(false);
        when(propertyOperation.findDefaultValueFromSecondPosition(eq(Collections.emptyList()), eq(propertyDefinition.getUniqueId()), eq(propertyDefinition.getDefaultValue()))).thenReturn(Either.left(propertyDefinition.getDefaultValue()));
        when(toscaOperationFacade.updatePropertyOfComponent(eq(resource), any())).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        StorageOperationStatus status = testInstance.unDeclarePropertiesAsListInputs(resource, input);
        Assert.assertEquals(status, StorageOperationStatus.NOT_FOUND);
    }

    private GetInputValueDataDefinition buildGetInputValue(String InputId) {
        GetInputValueDataDefinition getInputValue = new GetInputValueDataDefinition();
        getInputValue.setInputId(InputId);
        getInputValue.setInputName(InputId);

        return getInputValue;
    }

}
