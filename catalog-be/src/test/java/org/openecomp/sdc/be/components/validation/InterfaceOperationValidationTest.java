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

import com.google.common.collect.Sets;
import fj.data.Either;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationOutputDefinition;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.exception.ResponseFormat;
import org.openecomp.sdc.test.utils.InterfaceOperationTestUtils;

public class InterfaceOperationValidationTest {

    private Resource resource = setUpResourceMock();
    ResponseFormatManager responseFormatManagerMock;

    InterfaceOperationValidationUtilTest interfaceOperationValidationUtilTest = new InterfaceOperationValidationUtilTest();
    ListDataDefinition<OperationInputDefinition> operationInputDefinitionList = new ListDataDefinition<>();
    ListDataDefinition<OperationOutputDefinition> operationOutputDefinitionList = new ListDataDefinition<>();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        responseFormatManagerMock = Mockito.mock(ResponseFormatManager.class);
        when(responseFormatManagerMock.getResponseFormat(any())).thenReturn(new ResponseFormat());
        when(responseFormatManagerMock.getResponseFormat(any(), any())).thenReturn(new ResponseFormat());
        when(responseFormatManagerMock.getResponseFormat(any(), any(), any())).thenReturn(new ResponseFormat());
    }

    @Test
    public void testValidInterfaceOperation() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                        operationOutputDefinitionList,"upgrade");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, resource, false);
        Assert.assertTrue(booleanResponseFormatEither.isLeft());
    }

    @Test
    public void testInterfaceOperationDescriptionLength() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2 -  The Spring Initializer provides a project generator to make you " +
                        "productive with the certain technology stack from the beginning. You can create a skeleton project" +
                        "with web, data access (relational and NoSQL datastores), cloud, or messaging support",
                new ArtifactDefinition(), operationInputDefinitionList, operationOutputDefinitionList,"update");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, resource, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }



    @Test
    public void testInterfaceOperationForEmptyType() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList, "");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, resource, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }

    @Test
    public void testInterfaceOperationForEmptyInputParam() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,"input2");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, resource, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }

    @Test
    public void testInterfaceOperationForNonUniqueType() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,"CREATE");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, resource, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }

    @Test
    public void testInterfaceOperationTypeLength() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,
                "interface operation2 -  The Spring Initializer provides a project generator to make you " +
                        "productive with the certain technology stack from the beginning. You can create a skeleton project" +
                        "with web, data access (relational and NoSQL datastores), cloud, or messaging support");
        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, resource, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }


    @Test
    public void testInterfaceOperationUniqueInputParamNameInvalid() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label2"));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label2"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,"create");

        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, resource, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }

    @Test
    public void testInterfaceOperationUniqueInputParamNameValid() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label2"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,"update");


        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, resource, false);
        Assert.assertTrue(booleanResponseFormatEither.isLeft());
    }

    @Test
    public void testInterfaceOperationeInputParamNameEmpty() {
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("  "));
        operationInputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationInputDefinition("label1"));
        operationOutputDefinitionList.add(InterfaceOperationTestUtils.createMockOperationOutputDefinition("label1"));
        Collection<Operation> operations = createInterfaceOperationData("op2",
                "interface operation2",new ArtifactDefinition(), operationInputDefinitionList,
                operationOutputDefinitionList,"update");


        Either<Boolean, ResponseFormat> booleanResponseFormatEither = interfaceOperationValidationUtilTest
                .validateInterfaceOperations(operations, resource, false);
        Assert.assertTrue(booleanResponseFormatEither.isRight());
    }

    private Set<Operation> createInterfaceOperationData( String uniqueID, String description, ArtifactDefinition artifactDefinition,
                                                         ListDataDefinition<OperationInputDefinition> inputs,
                                                         ListDataDefinition<OperationOutputDefinition> outputs, String name) {
        return Sets.newHashSet(InterfaceOperationTestUtils.createInterfaceOperation(uniqueID, description, artifactDefinition, inputs, outputs, name));
    }

    private Resource setUpResourceMock(){
        Resource resource = new Resource();
        resource.setInterfaces(createMockInterfaceDefinition());
        return  resource;
    }

    private  Map<String, InterfaceDefinition> createMockInterfaceDefinition() {
        Map<String, Operation> operationMap = InterfaceOperationTestUtils.createMockOperationMap();
        Map<String, InterfaceDefinition> interfaceDefinitionMap = new HashMap<>();
        interfaceDefinitionMap.put("int1", InterfaceOperationTestUtils.createInterface("int1", "Interface 1",
                "lifecycle", "tosca", operationMap));

        return interfaceDefinitionMap;
    }

    private class InterfaceOperationValidationUtilTest extends InterfaceOperationValidation {

        protected ResponseFormatManager getResponseFormatManager() {
            return responseFormatManagerMock;
        }
    }
}
