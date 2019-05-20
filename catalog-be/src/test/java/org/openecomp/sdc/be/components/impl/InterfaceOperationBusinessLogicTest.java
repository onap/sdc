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

package org.openecomp.sdc.be.components.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fj.data.Either;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.components.validation.InterfaceOperationValidation;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.exception.ResponseFormat;
import org.openecomp.sdc.test.utils.InterfaceOperationTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class InterfaceOperationBusinessLogicTest {

    private static final String resourceId = "resourceId";
    private static final String interfaceId = "interfaceId";
    private static final String operationId = "operationId";
    private static final String inputId = "inputId";
    private static final String RESOURCE_NAME = "Resource1";
    private static final String operationId1 = "operationId1";
    private static final String interfaceId1 = "interfaceId1";
    private static final String operationName = "createOperation";

    @InjectMocks
    private InterfaceOperationBusinessLogic interfaceOperationBusinessLogic;
    @Mock
    private UserValidations userValidations;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private IGraphLockOperation graphLockOperation;
    @Mock
    private JanusGraphDao titanDao;
    @Mock
    private InterfaceLifecycleOperation interfaceLifecycleOperation;
    @Mock
    private InterfaceOperationValidation interfaceOperationValidation;
    @Mock
    private InterfaceOperation interfaceOperation;
    @Mock
    private ArtifactCassandraDao artifactCassandraDao;
    @Mock
    protected ArtifactsOperations artifactToscaOperation;
    private User user;
    private Resource resource;

    @Before
    public void setup() {
        resource = new ResourceBuilder().setComponentType(ComponentTypeEnum.RESOURCE).setUniqueId(resourceId)
                           .setName(RESOURCE_NAME).build();
        resource.setInterfaces(InterfaceOperationTestUtils.createMockInterfaceDefinitionMap(interfaceId, operationId,
                operationName));
        resource.setInputs(createInputsForResource());

        user = new User();
        when(userValidations.validateUserExists(eq(user.getUserId()), anyString(), eq(true))).thenReturn(user);
        when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));
        when(graphLockOperation.lockComponent(Mockito.anyString(), eq(NodeTypeEnum.Resource)))
                .thenReturn(StorageOperationStatus.OK);
        when(interfaceOperationValidation
                     .validateInterfaceOperations(any(), any(), any(), anyMap(), anyBoolean()))
                .thenReturn(Either.left(true));
        when(interfaceOperationValidation
                .validateDeleteOperationContainsNoMappedOutput(any(), any(), any()))
                .thenReturn(Either.left(true));
        when(titanDao.commit()).thenReturn(JanusGraphOperationStatus.OK);
    }

    private List<InputDefinition> createInputsForResource() {
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setName(inputId);
        inputDefinition.setInputId(inputId);
        inputDefinition.setUniqueId(inputId);
        inputDefinition.setValue(inputId);
        inputDefinition.setDefaultValue(inputId);
        return Arrays.asList(inputDefinition);
    }

    @Test
    public void createInterfaceOperationTestOnExistingInterface() {
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
                .thenReturn(Either.left(Collections.emptyMap()));
        when(interfaceOperation.updateInterfaces(any(), any())).thenReturn(Either.left(
                Collections.singletonList(InterfaceOperationTestUtils.createMockInterface(interfaceId, operationId, operationName))));
        Either<List<InterfaceDefinition>, ResponseFormat> interfaceOperationEither =
                interfaceOperationBusinessLogic.createInterfaceOperation(resourceId,
                    Collections.singletonList(InterfaceOperationTestUtils.createMockInterface(interfaceId,
                            operationId, operationName)),
                        user, true);
        Assert.assertTrue(interfaceOperationEither.isLeft());
    }

    @Test
    public void createInterfaceOperationTestOnExistingInterfaceInputsFromCapProp() {
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
                .thenReturn(Either.left(Collections.emptyMap()));
        when(interfaceOperation.updateInterfaces(any(), any())).thenReturn(Either.left(
                Collections.singletonList(InterfaceOperationTestUtils.createMockInterface(interfaceId, operationId, operationName))));

        CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setName("cap" + Math.random());
        capabilityDefinition.setType("tosca.capabilities.network.Bindable");
        capabilityDefinition.setOwnerId(resourceId);
        capabilityDefinition.setUniqueId("capUniqueId");

        List<ComponentInstanceProperty> properties = new ArrayList<>();
        ComponentInstanceProperty instanceProperty = new ComponentInstanceProperty();
        instanceProperty.setUniqueId("ComponentInput1_uniqueId");
        instanceProperty.setType("Integer");
        instanceProperty.setName("prop_name");
        instanceProperty.setDescription("prop_description_prop_desc");
        instanceProperty.setOwnerId("capUniqueId");
        instanceProperty.setSchema(new SchemaDefinition());
        properties.add(instanceProperty);
        capabilityDefinition.setProperties(properties);
        Map<String, List<CapabilityDefinition>> capabilityMap = new HashMap<>();
        capabilityMap.put(capabilityDefinition.getType(), Collections.singletonList(capabilityDefinition));

        resource.setCapabilities(capabilityMap);
        when(toscaOperationFacade.getToscaElement(resourceId)).thenReturn(Either.left(resource));

        Either<List<InterfaceDefinition>, ResponseFormat> interfaceOperationEither =
                interfaceOperationBusinessLogic.createInterfaceOperation(resourceId,
                        Collections.singletonList(InterfaceOperationTestUtils.createMockInterface(interfaceId,
                                operationId, operationName)),
                        user, true);
        Assert.assertTrue(interfaceOperationEither.isLeft());
    }


    @Test
    public void createInterfaceOperationWithoutInterfaceTest() {
        resource.getInterfaces().clear();
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
                .thenReturn(Either.left(Collections.emptyMap()));
        when(interfaceOperation.addInterfaces(any(), any())).thenReturn(Either.left(
                Collections.singletonList(InterfaceOperationTestUtils.createMockInterface(interfaceId, operationId, operationName))));
        when(interfaceOperation.updateInterfaces(any(), any())).thenReturn(Either.left(
                Collections.singletonList(InterfaceOperationTestUtils.createMockInterface(interfaceId, operationId, operationName))));
        Either<List<InterfaceDefinition>, ResponseFormat> interfaceOperationEither =
                interfaceOperationBusinessLogic.createInterfaceOperation(resourceId,
                        Collections.singletonList(InterfaceOperationTestUtils.createMockInterface(interfaceId,
                                operationId, operationName)),
                        user, true);
        Assert.assertTrue(interfaceOperationEither.isLeft());
    }

    @Test
    public void createInterfaceOperationWithoutInterfaceTestFail() {
        resource.getInterfaces().clear();
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
                .thenReturn(Either.left(Collections.emptyMap()));
        when(interfaceOperation.addInterfaces(any(), any()))
                .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        Either<List<InterfaceDefinition>, ResponseFormat> interfaceOperationEither =
                interfaceOperationBusinessLogic.createInterfaceOperation(resourceId,
                        Collections.singletonList(InterfaceOperationTestUtils.createMockInterface(interfaceId,
                                operationId, operationName)),
                        user, true);
        Assert.assertTrue(interfaceOperationEither.isRight());
    }

    @Test
    public void shouldFailWhenCreateInterfaceOperationFailedTest() {
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
                .thenReturn(Either.left(Collections.emptyMap()));
        when(interfaceOperation.updateInterfaces(any(), any()))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        Assert.assertTrue(interfaceOperationBusinessLogic.createInterfaceOperation(resourceId,
                Collections.singletonList(InterfaceOperationTestUtils.createMockInterface(interfaceId, operationId, operationName)),
                user, true).isRight());
    }

    @Test
    public void updateInterfaceOperationTestWithArtifactSuccess() {
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
                .thenReturn(Either.left(Collections.emptyMap()));
        when(artifactToscaOperation.removeArifactFromResource(any(), any(), any(), anyBoolean()))
                .thenReturn(Either.left(new ArtifactDefinition()));
        when(artifactToscaOperation.getArtifactById(any(), any())).thenReturn(Either.left(new ArtifactDefinition()));
        when(artifactCassandraDao.deleteArtifact(any(String.class))).thenReturn(CassandraOperationStatus.OK);
        when(interfaceOperation.updateInterfaces(any(), any())).thenReturn(Either.left(
                Collections.singletonList(InterfaceOperationTestUtils.createMockInterface(interfaceId, operationId, operationName))));
        Either<List<InterfaceDefinition>, ResponseFormat> interfaceOperation =
                interfaceOperationBusinessLogic.updateInterfaceOperation(resourceId,
                        Collections.singletonList(InterfaceOperationTestUtils.createMockInterface(interfaceId,
                                operationId, operationName)),
                        user, true);
        Assert.assertTrue(interfaceOperation.isLeft());
    }

    @Test
    public void updateInterfaceOperationTestWithArtifactFailure() {
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
                .thenReturn(Either.left(Collections.emptyMap()));
        when(artifactToscaOperation.getArtifactById(any(), any())).thenReturn(Either.left(new ArtifactDefinition()));
        when(artifactToscaOperation.removeArifactFromResource(any(), any(), any(), anyBoolean()))
                .thenReturn(Either.right(StorageOperationStatus.ARTIFACT_NOT_FOUND));
        Either<List<InterfaceDefinition>, ResponseFormat> interfaceOperation =
                interfaceOperationBusinessLogic.updateInterfaceOperation(resourceId,
                        Collections.singletonList(InterfaceOperationTestUtils.createMockInterface(interfaceId,
                                operationId, operationName)),
                        user, true);
        Assert.assertTrue(interfaceOperation.isRight());
    }

    @Test
    public void updateInterfaceOperationTestWithoutArtifact() {
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
                .thenReturn(Either.left(Collections.emptyMap()));
        when(artifactToscaOperation.getArtifactById(any(), any())).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        when(interfaceOperation.updateInterfaces(any(), any())).thenReturn(Either.left(
                Collections.singletonList(InterfaceOperationTestUtils.createMockInterface(interfaceId, operationId, operationName))));
        Either<List<InterfaceDefinition>, ResponseFormat> interfaceOperation =
                interfaceOperationBusinessLogic.updateInterfaceOperation(resourceId,
                        Collections.singletonList(InterfaceOperationTestUtils.createMockInterface(interfaceId,
                                operationId, operationName)),
                        user, true);
        Assert.assertTrue(interfaceOperation.isLeft());
    }

    @Test
    public void updateInterfaceOperationTestDoesntExist() {
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
                .thenReturn(Either.left(Collections.emptyMap()));
        Either<List<InterfaceDefinition>, ResponseFormat> interfaceOperation =
                interfaceOperationBusinessLogic.updateInterfaceOperation(resourceId,
                        Collections.singletonList(InterfaceOperationTestUtils.createMockInterface(interfaceId,
                                operationId, operationName)),
                        user, true);
        Assert.assertTrue(interfaceOperation.isRight());
    }

    @Test
    public void createInterfaceOperationTestFailOnException() {
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
                .thenReturn(Either.left(Collections.emptyMap()));
        when(interfaceOperation.updateInterfaces(any(), any())).thenThrow(new RuntimeException());
        Either<List<InterfaceDefinition>, ResponseFormat> interfaceOperationEither =
                interfaceOperationBusinessLogic.createInterfaceOperation(resourceId,
                        Collections.singletonList(InterfaceOperationTestUtils.createMockInterface(interfaceId,
                                operationId, operationName)),
                        user, true);
        Assert.assertTrue(interfaceOperationEither.isRight());
    }

    @Test
    public void createInterfaceOperationTestFailOnFetchinGlobalTypes() {
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        Either<List<InterfaceDefinition>, ResponseFormat> interfaceOperationEither =
                interfaceOperationBusinessLogic.createInterfaceOperation(resourceId,
                        Collections.singletonList(InterfaceOperationTestUtils.createMockInterface(interfaceId,
                                operationId, operationName)),
                        user, true);
        Assert.assertTrue(interfaceOperationEither.isRight());
    }

    @Test
    public void createInterfaceOperationTestFailOnValidation() {
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
                .thenReturn(Either.left(Collections.emptyMap()));
        when(interfaceOperationValidation
                     .validateInterfaceOperations(any(), any(), any(), anyMap(), anyBoolean()))
                .thenReturn(Either.right(new ResponseFormat()));
        Either<List<InterfaceDefinition>, ResponseFormat> interfaceOperationEither =
                interfaceOperationBusinessLogic.createInterfaceOperation(resourceId,
                        Collections.singletonList(InterfaceOperationTestUtils.createMockInterface(interfaceId,
                                operationId, operationName)),
                        user, true);
        Assert.assertTrue(interfaceOperationEither.isRight());
    }

    @Test
    public void deleteInterfaceOperationTestInterfaceDoesntExist() {
        Assert.assertTrue(interfaceOperationBusinessLogic.deleteInterfaceOperation(resourceId, interfaceId1,
                Collections.singletonList(operationId), user, true).isRight());
    }

    @Test
    public void deleteInterfaceOperationTestOperationDoesntExist() {
        Assert.assertTrue(interfaceOperationBusinessLogic.deleteInterfaceOperation(resourceId, interfaceId,
                Collections.singletonList(operationId1), user, true).isRight());
    }

    @Test
    public void deleteInterfaceOperationTestSuccess() {
        resource.getInterfaces().get(interfaceId).getOperations()
                .putAll(InterfaceOperationTestUtils.createMockOperationMap(operationId1, operationName));
        when(artifactToscaOperation.getArtifactById(any(), any())).thenReturn(Either.left(new ArtifactDefinition()));
        when(artifactToscaOperation.removeArifactFromResource(any(), any(), any(), anyBoolean()))
                .thenReturn(Either.left(new ArtifactDefinition()));
        when(artifactCassandraDao.deleteArtifact(any(String.class))).thenReturn(CassandraOperationStatus.OK);
        when(interfaceOperation.updateInterfaces(any(), any())).thenReturn(Either.left(Collections.emptyList()));
        Assert.assertTrue(interfaceOperationBusinessLogic.deleteInterfaceOperation(resourceId, interfaceId,
                Collections.singletonList(operationId), user, true).isLeft());
    }

    @Test
    public void shouldFailWhenDeleteInterfaceOperationFailedTest() {
        when(artifactToscaOperation.getArtifactById(any(), any())).thenReturn(Either.left(new ArtifactDefinition()));
        when(artifactToscaOperation.removeArifactFromResource(any(), any(), any(), anyBoolean()))
                .thenReturn(Either.left(new ArtifactDefinition()));
        when(artifactCassandraDao.deleteArtifact(any(String.class))).thenReturn(CassandraOperationStatus.OK);
        when(interfaceOperation.updateInterfaces(any(), any()))
                .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        Assert.assertTrue(interfaceOperationBusinessLogic.deleteInterfaceOperation(resourceId, interfaceId,
                Collections.singletonList(operationId), user, true).isRight());
    }

    @Test
    public void deleteInterfaceOperationTestFailOnArtifactDeletion() {
        when(artifactToscaOperation.getArtifactById(any(), any())).thenReturn(Either.left(new ArtifactDefinition()));
        when(artifactToscaOperation.removeArifactFromResource(any(), any(), any(), anyBoolean()))
                .thenReturn(Either.left(new ArtifactDefinition()));
        when(artifactCassandraDao.deleteArtifact(any(String.class))).thenReturn(CassandraOperationStatus.GENERAL_ERROR);
        Assert.assertTrue(interfaceOperationBusinessLogic.deleteInterfaceOperation(resourceId, interfaceId,
                Collections.singletonList(operationId), user, true).isRight());
    }

    @Test
    public void deleteInterfaceOperationTestFailOnException() {
        when(artifactToscaOperation.getArtifactById(any(), any())).thenReturn(Either.left(new ArtifactDefinition()));
        when(artifactToscaOperation.removeArifactFromResource(any(), any(), any(), anyBoolean()))
                .thenReturn(Either.left(new ArtifactDefinition()));
        when(artifactCassandraDao.deleteArtifact(any(String.class))).thenThrow(new RuntimeException());
        Assert.assertTrue(interfaceOperationBusinessLogic.deleteInterfaceOperation(resourceId, interfaceId,
                Collections.singletonList(operationId), user, true).isRight());
    }

    @Test
    public void deleteInterfaceTestSuccess() {
        when(artifactToscaOperation.getArtifactById(any(), any())).thenReturn(Either.left(new ArtifactDefinition()));
        when(artifactToscaOperation.removeArifactFromResource(any(), any(), any(), anyBoolean()))
                .thenReturn(Either.left(new ArtifactDefinition()));
        when(artifactCassandraDao.deleteArtifact(any(String.class))).thenReturn(CassandraOperationStatus.OK);
        when(interfaceOperation.updateInterfaces(any(), any())).thenReturn(Either.left(Collections.emptyList()));
        when(interfaceOperation.deleteInterface(any(), any())).thenReturn(Either.left(interfaceId));
        Assert.assertTrue(interfaceOperationBusinessLogic.deleteInterfaceOperation(resourceId, interfaceId,
                Collections.singletonList(operationId), user, true).isLeft());
    }

    @Test
    public void deleteInterfaceTestFailure() {
        when(artifactToscaOperation.getArtifactById(any(), any())).thenReturn(Either.left(new ArtifactDefinition()));
        when(artifactToscaOperation.removeArifactFromResource(any(), any(), any(), anyBoolean()))
                .thenReturn(Either.left(new ArtifactDefinition()));
        when(artifactCassandraDao.deleteArtifact(any(String.class))).thenReturn(CassandraOperationStatus.OK);
        when(interfaceOperation.updateInterfaces(any(), any())).thenReturn(Either.left(Collections.emptyList()));
        when(interfaceOperation.deleteInterface(any(), any()))
                .thenReturn(Either.right(StorageOperationStatus.GENERAL_ERROR));
        Assert.assertTrue(interfaceOperationBusinessLogic.deleteInterfaceOperation(resourceId, interfaceId,
                Collections.singletonList(operationId), user, true).isRight());
    }

    @Test
    public void getInterfaceOperationTestInterfaceDoesntExist() {
        Assert.assertTrue(interfaceOperationBusinessLogic.getInterfaceOperation(resourceId, interfaceId1,
                Collections.singletonList(operationId), user, true).isRight());
    }

    @Test
    public void getInterfaceOperationTestOperationDoesntExist() {
        Assert.assertTrue(interfaceOperationBusinessLogic.getInterfaceOperation(resourceId, interfaceId,
                Collections.singletonList(operationId1), user, true).isRight());
    }

    @Test
    public void getInterfaceOperationTest() {
        Assert.assertTrue(interfaceOperationBusinessLogic.getInterfaceOperation(resourceId, interfaceId,
                Collections.singletonList(operationId), user, true).isLeft());
    }

    @Test
    public void getInterfaceOperationTestFailOnException() {
        when(titanDao.commit()).thenThrow(new RuntimeException());
        Assert.assertTrue(interfaceOperationBusinessLogic.getInterfaceOperation(resourceId, interfaceId,
                Collections.singletonList(operationId), user, true).isRight());
    }

    @Test
    public void shouldFailWhenLockComponentFailedTest() {
        when(graphLockOperation.lockComponent(Mockito.anyString(), eq(NodeTypeEnum.Resource)))
                .thenReturn(StorageOperationStatus.NOT_FOUND);
        Assert.assertTrue(interfaceOperationBusinessLogic.deleteInterfaceOperation(resourceId, interfaceId,
                Collections.singletonList(operationId), user, true).isRight());
        Assert.assertTrue(interfaceOperationBusinessLogic.getInterfaceOperation(resourceId, interfaceId,
                Collections.singletonList(operationId), user, true).isRight());
        Assert.assertTrue(interfaceOperationBusinessLogic.createInterfaceOperation(resourceId,
                Collections.singletonList(InterfaceOperationTestUtils.createMockInterface(interfaceId, operationId, operationName)),
                user, true).isRight());
    }

    @Test
    public void shouldFailWhenGetComponentFailedTest() {
        when(toscaOperationFacade.getToscaElement(resourceId))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        Assert.assertTrue(interfaceOperationBusinessLogic.deleteInterfaceOperation(resourceId, interfaceId,
                Collections.singletonList(operationId), user, true).isRight());
        Assert.assertTrue(interfaceOperationBusinessLogic.getInterfaceOperation(resourceId, interfaceId,
                Collections.singletonList(operationId), user, true).isRight());
        Assert.assertTrue(interfaceOperationBusinessLogic.createInterfaceOperation(resourceId,
                Collections.singletonList(InterfaceOperationTestUtils.createMockInterface(interfaceId, operationId,
                        operationName)), user, true).isRight());
    }

    @Test
    public void testGetAllInterfaceLifecycleTypes_TypesNotFound() {
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        Either<Map<String, InterfaceDefinition>, ResponseFormat> response =
                interfaceOperationBusinessLogic.getAllInterfaceLifecycleTypes();
        Assert.assertTrue(response.isRight());
    }

    @Test
    public void testGetAllInterfaceLifecycleTypes_Success() {
        InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setUniqueId(interfaceId);
        interfaceDefinition.setType(interfaceId);
        Map<String, InterfaceDefinition> interfaceDefinitionMap = new HashMap<>();
        interfaceDefinitionMap.put(interfaceDefinition.getUniqueId(), interfaceDefinition);
        when(interfaceLifecycleOperation.getAllInterfaceLifecycleTypes())
                .thenReturn(Either.left(interfaceDefinitionMap));
        Either<Map<String, InterfaceDefinition>, ResponseFormat> response =
                interfaceOperationBusinessLogic.getAllInterfaceLifecycleTypes();
        Assert.assertEquals(1, response.left().value().size());
    }
}