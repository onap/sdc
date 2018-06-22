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

package org.openecomp.sdc.be.components.validation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.InterfaceOperationTestUtils;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationOutputDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;

import com.google.common.collect.Sets;

import fj.data.Either;

public class InterfaceOperationValidationTest implements InterfaceOperationTestUtils {

    private Resource  resource = (Resource) getToscaFullElement().left().value();

    ResponseFormatManager mock;
    @Mock
    ToscaOperationFacade toscaOperationFacade;

    @InjectMocks
    InterfaceOperationValidationUtilTest interfaceOperationValidationUtilTest = new InterfaceOperationValidationUtilTest();
    private static final String RESOURCE_ID = "resource1";
    ListDataDefinition<OperationInputDefinition> operationInputDefinitionList = new ListDataDefinition<>();
    ListDataDefinition<OperationOutputDefinition> operationOutputDefinitionList = new ListDataDefinition<>();
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mock = Mockito.mock(ResponseFormatManager.class);
        when(toscaOperationFacade.getToscaElement(any(), any(ComponentParametersView.class))).thenReturn(Either.left(resource));
        when(mock.getResponseFormat(any())).thenReturn(new ResponseFormat());
        when(mock.getResponseFormat(any(), any())).thenReturn(new ResponseFormat());
        when(mock.getResponseFormat(any(), any(), any())).thenReturn(new ResponseFormat());
    }


    @Test
    public void testValidInterfaceOperation() {
        operationInputDefinitionList.add(createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                        operationOutputDefinitionList,"upgrade");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, RESOURCE_ID, false);
        Assert.assertTrue(booleanResponseFormatEither.isLeft());
    }

    @Test
    public void testInterfaceOperationDescriptionLength() {
        operationInputDefinitionList.add(createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2 -  The Spring Initializer provides a project generator to make you " +
                        "productive with the certain technology stack from the beginning. You can create a skeleton project" +
                        "with web, data access (relational and NoSQL datastores), cloud, or messaging support",
                new ArtifactDefinition(), operationInputDefinitionList, operationOutputDefinitionList,"update");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, RESOURCE_ID, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }



    @Test
    public void testInterfaceOperationForEmptyType() {
        operationInputDefinitionList.add(createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList, "");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, RESOURCE_ID, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }

    @Test
    public void testInterfaceOperationForEmptyInputParam() {
        operationInputDefinitionList.add(createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,"input2");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, RESOURCE_ID, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }

    @Test
    public void testInterfaceOperationForNonUniqueType() {
        operationInputDefinitionList.add(createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,"CREATE");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, RESOURCE_ID, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }

    @Test
    public void testInterfaceOperationTypeLength() {
        operationInputDefinitionList.add(createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,
                "interface operation2 -  The Spring Initializer provides a project generator to make you " +
                        "productive with the certain technology stack from the beginning. You can create a skeleton project" +
                        "with web, data access (relational and NoSQL datastores), cloud, or messaging support");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, RESOURCE_ID, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }


    @Test
    public void testInterfaceOperationUniqueInputParamNameInvalid() {
        operationInputDefinitionList.add(createMockOperationInputDefinition("label1"));
        operationInputDefinitionList.add(createMockOperationInputDefinition("label1"));
        operationInputDefinitionList.add(createMockOperationInputDefinition("label2"));
        operationInputDefinitionList.add(createMockOperationInputDefinition("label2"));
        operationOutputDefinitionList.add(createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,"create");

        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, RESOURCE_ID, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }

    @Test
    public void testInterfaceOperationUniqueInputParamNameValid() {
        operationInputDefinitionList.add(createMockOperationInputDefinition("label1"));
        operationInputDefinitionList.add(createMockOperationInputDefinition("label2"));
        operationOutputDefinitionList.add(createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,"update");


        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, RESOURCE_ID, false);
        Assert.assertTrue(booleanResponseFormatEither.isLeft());
    }

    @Test
    public void testInterfaceOperationeInputParamNameEmpty() {
        operationInputDefinitionList.add(createMockOperationInputDefinition("  "));
        operationInputDefinitionList.add(createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,"update");


        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, RESOURCE_ID, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }

    private Set<Operation> createInterfaceOperationData( String uniqueID, String description, ArtifactDefinition artifactDefinition,
                                                         ListDataDefinition<OperationInputDefinition> inputs,
                                                         ListDataDefinition<OperationOutputDefinition> outputs, String name) {
        return Sets.newHashSet(createInterfaceOperation(uniqueID, description, artifactDefinition, inputs, outputs, name));
    }

    private  <T extends Component> Either<T, StorageOperationStatus> getToscaFullElement() {

        return Either.left((T) setUpResourceMock());
    }

    private Resource setUpResourceMock(){
        Resource resource = new Resource();
        resource.setInterfaces(createMockInterfaceDefinition());

        return  resource;
    }

    private  Map<String, InterfaceDefinition> createMockInterfaceDefinition() {
        Map<String, Operation> operationMap = createMockOperationMap();
        Map<String, InterfaceDefinition> interfaceDefinitionMap = new HashMap<>();
        interfaceDefinitionMap.put("int1", createInterface("int1", "Interface 1",
                "lifecycle", "tosca", operationMap));

        return interfaceDefinitionMap;
    }

    private class InterfaceOperationValidationUtilTest extends InterfaceOperationValidation {

        protected ResponseFormatManager getResponseFormatManager() {
            return mock;
        }
    }
}
