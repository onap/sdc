/*
 * Copyright © 2016-2019 European Support Limited
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

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.property.PropertyDeclarationOrchestrator;
import org.openecomp.sdc.be.components.utils.ComponentInstanceBuilder;
import org.openecomp.sdc.be.components.utils.GroupDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.PromoteVersionEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstInputsMap;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstancePropInput;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsonjanusgraph.datamodel.ToscaElementTypeEnum;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.PolicyTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;

@RunWith(MockitoJUnitRunner.class)
public class PolicyBusinessLogicTest {

    private final static String COMPONENT_ID = "componentId";
    private final static String NON_EXIST_COMPONENT_ID = "nonExistComponentId";
    private final static String COMPONENT_NAME = "componentName";
    private final static String POLICY_TYPE_NAME = "policyTypeName";
    private final static String POLICY_ID = "policyId";
    private final static String INVALID_POLICY_ID = "invalidPolicyId";
    private final static String POLICY_NAME = "policyName";
    private final static String OTHER_POLICY_NAME = "otherPolicyName";
    private final static String USER_ID = "jh0003";
    private final static String UNIQUE_ID_EXSISTS = "uniqueIdExists";
    private final static String UNIQUE_ID_DOESNT_EXSISTS = "uniqueIdDoesntExists";
    private final static String CREATE_POLICY = "create Policy";
    private final static String PROPERTY_NAME = "propDefinition";
    private final static User user = buildUser();
    private final static PolicyDefinition policy = buildPolicy(POLICY_NAME);
    private final static Resource resource = buildResource();
    private final static PolicyDefinition otherPolicy = buildPolicy(OTHER_POLICY_NAME);
    private final static PolicyTypeDefinition policyType = buildPolicyType();
    private static Either<Component, StorageOperationStatus> componentSuccessEither;
    private static Either<PolicyTypeDefinition, StorageOperationStatus> getPolicyTypeSuccessEither;
    private static Either<PolicyDefinition, StorageOperationStatus> policySuccessEither;
    private static ResponseFormat notFoundResponse;
    private static ResponseFormat invalidContentResponse;
    private static ResponseFormat nameExistsResponse;
    private final ConfigurationManager configurationManager = new ConfigurationManager(
        new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
    @Mock
    private PropertyDeclarationOrchestrator propertyDeclarationOrchestrator;
    @InjectMocks
    private PolicyBusinessLogic businessLogic;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Mock
    private IGraphLockOperation graphLockOperation;
    @Mock
    private PolicyTypeOperation policyTypeOperation;
    @Mock
    private UserValidations userValidations;
    @Mock
    private JanusGraphDao janusGraphDao;
    @Mock
    private ApplicationDataTypeCache applicationDataTypeCache;
    @Mock
    private PropertyOperation propertyOperation;

    @BeforeClass
    public static void setup() {
        createResponses();
    }

    private static void createResponses() {
        componentSuccessEither = Either.left(resource);
        getPolicyTypeSuccessEither = Either.left(policyType);
        policySuccessEither = Either.left(policy);
        notFoundResponse = new ResponseFormat();
        notFoundResponse.setStatus(404);
        invalidContentResponse = new ResponseFormat();
        invalidContentResponse.setStatus(400);
        nameExistsResponse = new ResponseFormat();
        nameExistsResponse.setStatus(409);
    }

    private static PolicyTypeDefinition buildPolicyType() {
        PolicyTypeDefinition policyType = new PolicyTypeDefinition();
        policyType.setType(POLICY_TYPE_NAME);
        return policyType;
    }

    private static PolicyDefinition buildPolicy(String policyName) {
        PolicyDefinition policy = new PolicyDefinition();
        policy.setUniqueId(POLICY_ID);
        policy.setPolicyTypeName(POLICY_TYPE_NAME);
        policy.setComponentName(COMPONENT_NAME);
        policy.setName(policyName);
        return policy;
    }

    private static Resource buildResource() {
        Resource resource = new Resource();
        resource.setUniqueId(COMPONENT_ID);
        resource.setName(COMPONENT_NAME);
        resource.setCreatorUserId(USER_ID);
        resource.setLastUpdaterUserId(USER_ID);
        resource.setState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        resource.setIsDeleted(false);
        resource.setResourceType(ResourceTypeEnum.VF);
        resource.setToscaType(ToscaElementTypeEnum.TOPOLOGY_TEMPLATE.getValue());
        resource.setComponentType(ComponentTypeEnum.RESOURCE);
        Map<String, PolicyDefinition> policies = new HashMap<>();
        policies.put(POLICY_ID, policy);
        resource.setPolicies(policies);
        return resource;
    }

    private static User buildUser() {
        User user = new User();
        user.setUserId(USER_ID);
        return user;
    }

    @Before
    public void initBl() {
        MockitoAnnotations.openMocks(this);
        businessLogic.setComponentsUtils(componentsUtils);
        businessLogic.setToscaOperationFacade(toscaOperationFacade);
        businessLogic.setJanusGraphDao(janusGraphDao);
        businessLogic.setUserValidations(userValidations);
        businessLogic.setGraphLockOperation(graphLockOperation);
        businessLogic.setPolicyTypeOperation(policyTypeOperation);
        businessLogic.setApplicationDataTypeCache(applicationDataTypeCache);
        businessLogic.setPropertyOperation(propertyOperation);
        businessLogic.setPropertyDeclarationOrchestrator(propertyDeclarationOrchestrator);
    }

    @Test
    public void createPolicySuccessTest() {
        stubValidateAndLockSuccess(CREATE_POLICY);
        when(policyTypeOperation.getLatestPolicyTypeByType(eq(POLICY_TYPE_NAME), any())).thenReturn(getPolicyTypeSuccessEither);
        when(toscaOperationFacade.associatePolicyToComponent(eq(COMPONENT_ID), any(PolicyDefinition.class), eq(0))).thenReturn(policySuccessEither);
        stubUnlockAndCommit();
        PolicyDefinition response = businessLogic.createPolicy(ComponentTypeEnum.RESOURCE, COMPONENT_ID, POLICY_TYPE_NAME, USER_ID, true);
        assertThat(response.isEmpty()).isFalse();
    }

    @Test
    public void createPolicyFromCsarDefinitionTest() {
        String prop1 = "Prop_1";
        String prop2 = "Prop_2";
        Map<String, PolicyDefinition> policies = new HashMap<>();
        PolicyDefinition policy = buildPolicy(POLICY_NAME);
        Map<PolicyTargetType, List<String>> targets = getTargets();
        PropertyDataDefinition[] properties = getProperties(prop1, prop2);
        policy.setTargets(targets);
        policy.setProperties(Arrays.asList(properties));
        policies.put(POLICY_NAME, policy);

        List<ComponentInstance> instanceList = new ArrayList<>();
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setUniqueId(UNIQUE_ID_EXSISTS);
        componentInstance.setName(UNIQUE_ID_EXSISTS);
        instanceList.add(componentInstance);

        Resource newResource = buildResource();
        newResource.setPolicies(policies);
        newResource.setComponentInstances(instanceList);

        when(policyTypeOperation.getLatestPolicyTypeByType(eq(POLICY_TYPE_NAME), any())).thenReturn(getPolicyTypeSuccessEither);
        when(toscaOperationFacade.associatePolicyToComponent(eq(COMPONENT_ID), any(PolicyDefinition.class), eq(0))).thenReturn(Either.left(policy));
        when(toscaOperationFacade.getToscaFullElement(eq(COMPONENT_ID))).thenReturn(Either.left(newResource));
        when(toscaOperationFacade.updatePolicyOfComponent(eq(COMPONENT_ID), any(PolicyDefinition.class), any(PromoteVersionEnum.class))).thenReturn(
            Either.left(policy));
        when(propertyOperation.validateAndUpdatePropertyValue(eq(null), eq(prop1), anyBoolean(), eq(null), anyMap())).thenReturn(Either.left(prop1));
        when(propertyOperation.validateAndUpdatePropertyValue(eq(null), eq(prop2), anyBoolean(), eq(null), anyMap())).thenReturn(Either.left(prop2));

        Map<String, PolicyDefinition> createdPolicy = businessLogic.createPolicies(newResource, policies);

        assertThat(createdPolicy.isEmpty()).isFalse();
        PolicyDefinition newPolicy = createdPolicy.get(POLICY_NAME);
        assertNotNull(newPolicy);
        assertNotNull(newPolicy.getTargets());
        assertNotNull(newPolicy.getProperties());
        assertEquals(2, newPolicy.getProperties().size());
        assertEquals(1, newPolicy.getTargets().size());
    }

    @Test
    public void createPolicyUserFailureTest() {
        ByActionStatusComponentException userNotFoundException = new ByActionStatusComponentException(ActionStatus.USER_NOT_FOUND);
        when(userValidations.validateUserExists(eq(USER_ID))).thenThrow(userNotFoundException);
        stubRollback();
        try {
            businessLogic.createPolicy(ComponentTypeEnum.RESOURCE, COMPONENT_ID, POLICY_TYPE_NAME, USER_ID, true);
        } catch (ByActionStatusComponentException e) {
            assertEquals(e.getActionStatus(), userNotFoundException.getActionStatus());
        }
    }

    private void assertNotFound(Either<PolicyDefinition, ResponseFormat> response) {
        assertThat(response.isRight() && response.right().value().getStatus().equals(404)).isTrue();
    }

    @Test(expected = ComponentException.class)
    public void createPolicyComponentFailureTest() {
        when(userValidations.validateUserExists(eq(USER_ID))).thenReturn(user);
        Either<Component, StorageOperationStatus> componentNotFoundResponse = Either.right(StorageOperationStatus.NOT_FOUND);
        when(componentsUtils.convertFromStorageResponse(eq(StorageOperationStatus.NOT_FOUND), eq(ComponentTypeEnum.RESOURCE))).thenReturn(
            ActionStatus.RESOURCE_NOT_FOUND);
        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(componentNotFoundResponse);
        businessLogic.createPolicy(ComponentTypeEnum.RESOURCE, COMPONENT_ID, POLICY_TYPE_NAME, USER_ID, true);
    }

    @Test(expected = ComponentException.class)
    public void createPolicyPolicyTypeFailureTest() {
        stubValidateAndLockSuccess(CREATE_POLICY);
        Either<PolicyTypeDefinition, StorageOperationStatus> getPolicyTypeFailed = Either.right(StorageOperationStatus.NOT_FOUND);
        when(policyTypeOperation.getLatestPolicyTypeByType(eq(POLICY_TYPE_NAME), any())).thenReturn(getPolicyTypeFailed);
        when(componentsUtils.convertFromStorageResponse(eq(getPolicyTypeFailed.right().value()))).thenReturn(ActionStatus.RESOURCE_NOT_FOUND);
        stubUnlockAndRollback();
        businessLogic.createPolicy(ComponentTypeEnum.RESOURCE, COMPONENT_ID, POLICY_TYPE_NAME, USER_ID, true);
    }

    @Test(expected = ComponentException.class)
    public void createPolicyComponentTypeFailureTest() {
        stubValidateAndLockSuccess(CREATE_POLICY);
        when(policyTypeOperation.getLatestPolicyTypeByType(eq(POLICY_TYPE_NAME), any())).thenReturn(getPolicyTypeSuccessEither);
        Either<PolicyDefinition, StorageOperationStatus> addPolicyRes = Either.right(StorageOperationStatus.BAD_REQUEST);
        when(toscaOperationFacade.associatePolicyToComponent(eq(COMPONENT_ID), any(PolicyDefinition.class), eq(0))).thenReturn(addPolicyRes);
        when(componentsUtils.convertFromStorageResponse(eq(addPolicyRes.right().value()))).thenReturn(ActionStatus.INVALID_CONTENT);

        stubUnlockAndRollback();
        businessLogic.createPolicy(ComponentTypeEnum.RESOURCE, COMPONENT_ID, POLICY_TYPE_NAME, USER_ID, true);
    }

    @Test
    public void updatePolicySuccessTest() {
        stubValidateAndLockSuccess(CREATE_POLICY);
        when(toscaOperationFacade.updatePolicyOfComponent(eq(COMPONENT_ID), any(PolicyDefinition.class), any(PromoteVersionEnum.class))).thenReturn(
            policySuccessEither);
        stubUnlockAndCommit();
        PolicyDefinition response = businessLogic.updatePolicy(ComponentTypeEnum.RESOURCE, COMPONENT_ID, otherPolicy, USER_ID, true);
        assertThat(response.isEmpty()).isFalse();
    }

    @Test(expected = ComponentException.class)
    public void updatePolicyNameFailureTest() {
        stubValidateAndLockSuccess(CREATE_POLICY);
        stubUnlockAndRollback();
        businessLogic.updatePolicy(ComponentTypeEnum.RESOURCE, COMPONENT_ID, policy, USER_ID, true);
    }

    @Test
    public void getPolicySuccessTest() {
        stubValidationSuccess(CREATE_POLICY);
        stubCommit();
        PolicyDefinition response = businessLogic.getPolicy(ComponentTypeEnum.RESOURCE, COMPONENT_ID, POLICY_ID, USER_ID);
        assertThat(response.isEmpty()).isFalse();
    }

    @Test(expected = ComponentException.class)
    public void getPolicyFailureTest() {
        stubValidationSuccess(CREATE_POLICY);
        stubRollback();
        businessLogic.getPolicy(ComponentTypeEnum.RESOURCE, COMPONENT_ID, INVALID_POLICY_ID, USER_ID);
    }

    @Test
    public void deletePolicySuccessTest() {
        stubValidateAndLockSuccess(CREATE_POLICY);
        stubCommit();
        when(toscaOperationFacade.removePolicyFromComponent(eq(COMPONENT_ID), eq(POLICY_ID))).thenReturn(StorageOperationStatus.OK);
        PolicyDefinition response = businessLogic.deletePolicy(ComponentTypeEnum.RESOURCE, COMPONENT_ID, POLICY_ID, USER_ID, true);
        assertThat(response.isEmpty()).isFalse();
    }

    @Test(expected = ComponentException.class)
    public void deletePolicyFailureTest() {
        stubValidateAndLockSuccess(CREATE_POLICY);
        stubCommit();
        businessLogic.deletePolicy(ComponentTypeEnum.RESOURCE, COMPONENT_ID, INVALID_POLICY_ID, USER_ID, true);
    }

    @Test
    public void updatePolicyPropertiesSuccessTest() {
        stubValidateAndLockSuccess(CREATE_POLICY);
        String prop1 = "Name";
        String prop2 = "Type";
        when(propertyOperation.validateAndUpdatePropertyValue(eq(null), eq(prop1), anyBoolean(), eq(null), anyMap())).thenReturn(Either.left(prop1));
        when(propertyOperation.validateAndUpdatePropertyValue(eq(null), eq(prop2), anyBoolean(), eq(null), anyMap())).thenReturn(Either.left(prop2));
        when(toscaOperationFacade.updatePolicyOfComponent(eq(COMPONENT_ID), any(PolicyDefinition.class), any(PromoteVersionEnum.class))).thenReturn(
            policySuccessEither);
        stubUnlockAndCommit();
        PropertyDataDefinition[] properties = getProperties(prop1, prop2);
        policy.setProperties(Arrays.asList(properties));
        List<PropertyDataDefinition> response = businessLogic.updatePolicyProperties(ComponentTypeEnum.RESOURCE, COMPONENT_ID, POLICY_ID, properties,
            USER_ID, true);
        List<PropertyDataDefinition> updatedProperties = response;
        assertThat(updatedProperties.size()).isEqualTo(2);
    }

    @Test
    public void updatePolicyTargetsSuccessTest() {
        stubValidateAndLockSuccess(CREATE_POLICY);
        stubGetToscaFullElementSuccess();
        stubUpdatePolicyOfComponentSuccess();
        stubGetToscaElementSuccess();
        PolicyDefinition policyResult = businessLogic.updatePolicyTargets(ComponentTypeEnum.RESOURCE, COMPONENT_ID, POLICY_ID, getTargets(), USER_ID);
        Map<PolicyTargetType, List<String>> targets = getTargets();
        assertThat(policyResult.getTargets().values()).usingFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(targets.get(PolicyTargetType.GROUPS), targets.get(PolicyTargetType.COMPONENT_INSTANCES));

    }

    @Test(expected = ComponentException.class)
    public void updatePolicyTargetsTargetIDFailureTest() {
        stubValidateAndLockSuccess(CREATE_POLICY);
        stubGetToscaFullElementSuccess();
        stubGetToscaElementSuccess();
        stubUpdatePolicyOfComponentSuccess();
        stubRollback();
        businessLogic.updatePolicyTargets(ComponentTypeEnum.RESOURCE, COMPONENT_ID, POLICY_ID, getTargetListFakeId(), USER_ID);

    }

    @Test(expected = ComponentException.class)
    public void updatePolicyTargetsTypeFailureTest() {
        stubValidateAndLockSuccess(CREATE_POLICY);
        stubGetToscaFullElementSuccess();
        stubGetToscaElementSuccess();
        stubUpdatePolicyOfComponentSuccess();
        stubRollback();
        businessLogic.updatePolicyTargets(ComponentTypeEnum.RESOURCE, COMPONENT_ID, POLICY_ID, getTargetListFakeType(), USER_ID);

    }

    private void stubUpdatePolicyOfComponentSuccess() {
        when(toscaOperationFacade.updatePolicyOfComponent(eq(COMPONENT_ID), eq(policy), any(PromoteVersionEnum.class))).thenReturn(
            policySuccessEither);
    }

    @Test(expected = ComponentException.class)
    public void updatePolicyPropertiesFailureTest() {
        stubValidateAndLockSuccess(CREATE_POLICY);
        stubUnlockAndRollback();
        policy.setProperties(null);
        businessLogic.updatePolicyProperties(ComponentTypeEnum.RESOURCE, COMPONENT_ID, POLICY_ID, getProperties("Name", "Type"), USER_ID, true);
    }

    @Test
    public void testDeclarePropertiesAsPoliciesSuccess() {
        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), Mockito.any(ComponentParametersView.class))).thenReturn(Either.left(resource));
        when(graphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);
        when(graphLockOperation.unlockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);

        when(propertyDeclarationOrchestrator.declarePropertiesToPolicies(any(), any())).thenReturn(Either.left(getDeclaredPolicies()));

        Either<List<PolicyDefinition>, ResponseFormat> declaredPoliciesEither = businessLogic
            .declareProperties(USER_ID,
                resource.getUniqueId(),
                ComponentTypeEnum.RESOURCE,
                getInputForPropertyToPolicyDeclaration());

        assertThat(declaredPoliciesEither.isLeft()).isTrue();

        List<PolicyDefinition> declaredPolicies = declaredPoliciesEither.left().value();
        assertThat(CollectionUtils.isNotEmpty(declaredPolicies)).isTrue();
        assertEquals(1, declaredPolicies.size());
    }

    @Test
    public void testDeclarePropertiesAsPoliciesFailure() {
        when(toscaOperationFacade.getToscaElement(eq(NON_EXIST_COMPONENT_ID), Mockito.any(ComponentParametersView.class))).thenReturn(
            Either.right(StorageOperationStatus.NOT_FOUND));
        when(componentsUtils.convertFromStorageResponse(eq(StorageOperationStatus.NOT_FOUND), eq(ComponentTypeEnum.RESOURCE))).thenReturn(
            ActionStatus.RESOURCE_NOT_FOUND);
        try {
            businessLogic
                .declareProperties(USER_ID,
                    NON_EXIST_COMPONENT_ID,
                    ComponentTypeEnum.RESOURCE,
                    getInputForPropertyToPolicyDeclaration());
        } catch (ComponentException e) {
            assertEquals(ActionStatus.RESOURCE_NOT_FOUND, e.getActionStatus());
            return;
        }
        fail();
    }

    private ComponentInstInputsMap getInputForPropertyToPolicyDeclaration() {
        PropertyDefinition propertyDefinition = getPropertyDefinitionForDeclaration();

        ComponentInstancePropInput componentInstancePropInput = new ComponentInstancePropInput();
        componentInstancePropInput.setInput(propertyDefinition);
        componentInstancePropInput.setPropertiesName(PROPERTY_NAME);

        Map<String, List<ComponentInstancePropInput>> componentPropertiesToPolicies = new HashMap<>();
        componentPropertiesToPolicies.put(resource.getUniqueId(), Collections.singletonList(componentInstancePropInput));

        ComponentInstInputsMap componentInstInputsMap = new ComponentInstInputsMap();
        componentInstInputsMap.setComponentInstancePropertiesToPolicies(componentPropertiesToPolicies);
        return componentInstInputsMap;
    }

    private List<PolicyDefinition> getDeclaredPolicies() {
        return Collections.singletonList(new PolicyDefinition(getPropertyDefinitionForDeclaration()));
    }

    private PropertyDefinition getPropertyDefinitionForDeclaration() {
        PropertyDefinition propertyDefinition = new PropertyDefinition();
        propertyDefinition.setUniqueId(PROPERTY_NAME);
        propertyDefinition.setName(PROPERTY_NAME);
        return propertyDefinition;
    }

    private PropertyDataDefinition[] getProperties(String prop1, String prop2) {
        PropertyDataDefinition property1 = new PropertyDataDefinition();
        property1.setName(prop1);
        property1.setValue(prop1);
        PropertyDataDefinition property2 = new PropertyDataDefinition();
        property2.setName(prop2);
        property2.setValue(prop2);
        return new PropertyDataDefinition[]{property1, property2};
    }

    private void stubUnlockAndRollback() {
        when(graphLockOperation.unlockComponent(eq(COMPONENT_ID), any(NodeTypeEnum.class))).thenReturn(StorageOperationStatus.OK);
        stubRollback();
    }

    private void stubCommit() {
        when(janusGraphDao.commit()).thenReturn(JanusGraphOperationStatus.OK);
    }

    private void stubRollback() {
        when(janusGraphDao.rollback()).thenReturn(JanusGraphOperationStatus.OK);
    }

    private void stubUnlockAndCommit() {
        when(graphLockOperation.unlockComponent(eq(COMPONENT_ID), any(NodeTypeEnum.class))).thenReturn(StorageOperationStatus.OK);
        stubCommit();
    }

    private void stubValidateAndLockSuccess(String methodName) {
        stubValidationSuccess(methodName);
        when(graphLockOperation.lockComponent(eq(COMPONENT_ID), any(NodeTypeEnum.class))).thenReturn(StorageOperationStatus.OK);
    }

    private void stubValidationSuccess(String methodName) {
        when(userValidations.validateUserExists(eq(USER_ID))).thenReturn(user);
        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID), any(ComponentParametersView.class))).thenReturn(componentSuccessEither);
    }

    private void stubGetToscaFullElementSuccess() {
        when(toscaOperationFacade.getToscaFullElement(eq(COMPONENT_ID))).thenReturn(buildElementEither());
    }

    private void stubGetToscaElementSuccess() {
        when(toscaOperationFacade.getToscaElement(eq(COMPONENT_ID))).thenReturn(componentSuccessEither);
    }

    private Either<Component, StorageOperationStatus> buildElementEither() {
        ResourceBuilder builder = new ResourceBuilder();
        GroupDefinition groupDefinition = GroupDefinitionBuilder.create().setUniqueId(UNIQUE_ID_EXSISTS).build();
        ComponentInstanceBuilder componentInstanceBuilder = new ComponentInstanceBuilder();
        ComponentInstance componentInstance = componentInstanceBuilder.setUniqueId(UNIQUE_ID_EXSISTS).build();
        return Either.left(builder.addGroup(groupDefinition).addComponentInstance(componentInstance).build());
    }

    private Map<PolicyTargetType, List<String>> getTargets() {
        Map<PolicyTargetType, List<String>> targets = new HashMap<>();
        targets.put(PolicyTargetType.COMPONENT_INSTANCES, Collections.singletonList(UNIQUE_ID_EXSISTS));
        targets.put(PolicyTargetType.GROUPS, Collections.singletonList(UNIQUE_ID_EXSISTS));
        return targets;
    }

    private Map<PolicyTargetType, List<String>> getTargetListFakeType() {
        Map<PolicyTargetType, List<String>> targets = new HashMap<>();
        targets.put(PolicyTargetType.TYPE_DOES_NOT_EXIST, Collections.singletonList(UNIQUE_ID_EXSISTS));
        return targets;
    }

    private Map<PolicyTargetType, List<String>> getTargetListFakeId() {
        Map<PolicyTargetType, List<String>> targets = new HashMap<>();
        targets.put(PolicyTargetType.COMPONENT_INSTANCES, Collections.singletonList(UNIQUE_ID_DOESNT_EXSISTS));
        targets.put(PolicyTargetType.GROUPS, Collections.singletonList(UNIQUE_ID_DOESNT_EXSISTS));
        return targets;
    }

}
