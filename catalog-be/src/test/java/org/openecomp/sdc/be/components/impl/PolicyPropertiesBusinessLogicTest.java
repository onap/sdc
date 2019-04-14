package org.openecomp.sdc.be.components.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.util.List;
import javax.ws.rs.core.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.utils.PolicyDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;

@RunWith(MockitoJUnitRunner.class)
public class PolicyPropertiesBusinessLogicTest {

    private static final String POLICY_ID = "policy1";
    private static final String RESOURCE_ID = "resourceId";
    private static final String USER_ID = "userId";
    public static final String NO_PROPS_POLICY = "policy2";
    @InjectMocks
    private PolicyBusinessLogic testInstance;

    @Mock
    private UserValidations userValidations;

    @Mock
    private TitanDao titanDao;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Mock
    private ComponentsUtils componentsUtils;

    private final ComponentTypeEnum COMPONENT_TYPE = ComponentTypeEnum.RESOURCE;

    private ComponentParametersView componentFilter;
    private Resource resource;
    private PropertyDefinition prop1, prop2;

    @Before
    public void setUp() throws Exception {
        testInstance.setUserValidations(userValidations);
        testInstance.setTitanGenericDao(titanDao);
        testInstance.setToscaOperationFacade(toscaOperationFacade);
        testInstance.setComponentsUtils(componentsUtils);

        componentFilter = new ComponentParametersView(true);
        componentFilter.setIgnorePolicies(false);
        componentFilter.setIgnoreUsers(false);

        prop1 = new PropertyDataDefinitionBuilder().setUniqueId("prop1").build();
        prop2 = new PropertyDataDefinitionBuilder().setUniqueId("prop1").build();

        PolicyDefinition policy1 = PolicyDefinitionBuilder.create()
                .setUniqueId(POLICY_ID)
                .setProperties(prop1, prop2)
                .build();

        PolicyDefinition policy2 = PolicyDefinitionBuilder.create()
                .setUniqueId(NO_PROPS_POLICY)
                .build();
        resource = new ResourceBuilder()
                .setUniqueId(RESOURCE_ID)
                .setComponentType(COMPONENT_TYPE)
                .setLifeCycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT)
                .setLastUpdaterUserId(USER_ID)
                .addPolicy(policy1)
                .addPolicy(policy2)
                .build();
    }

    @After
    public void tearDown() {
        verify(titanDao).commit();
    }

    @Test
    public void getPolicyProperties_userIdIsNull() {
        String userId = null;
        ComponentException forbiddenException = new ComponentException(ActionStatus.AUTH_FAILED);
        when(userValidations.validateUserExists(eq(userId), anyString(), eq(false))).thenThrow(forbiddenException);
        try{
            testInstance.getPolicyProperties(ComponentTypeEnum.RESOURCE, RESOURCE_ID, POLICY_ID, null);
        } catch(ComponentException e){
            assertThat(e.getActionStatus()).isEqualTo(ActionStatus.AUTH_FAILED);
        }
    }

    @Test
    public void getPolicyProperties_componentNotFound() {
        when(userValidations.validateUserExists(eq(USER_ID), anyString(), eq(false))).thenReturn(new User());
        ArgumentCaptor<ComponentParametersView> filterCaptor = ArgumentCaptor.forClass(ComponentParametersView.class);
        when(toscaOperationFacade.getToscaElement(eq(RESOURCE_ID), filterCaptor.capture())).thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        when(componentsUtils.convertFromStorageResponse(StorageOperationStatus.NOT_FOUND, ComponentTypeEnum.RESOURCE)).thenCallRealMethod();
        ResponseFormat notFoundResponse = new ResponseFormat(Response.Status.NOT_FOUND.getStatusCode());
        when(componentsUtils.getResponseFormat(eq(ActionStatus.RESOURCE_NOT_FOUND), anyString())).thenReturn(notFoundResponse);
        Either<List<PropertyDataDefinition>, ResponseFormat> policyProperties = testInstance.getPolicyProperties(ComponentTypeEnum.RESOURCE, RESOURCE_ID, POLICY_ID, USER_ID);
        assertThat(policyProperties.right().value()).isSameAs(notFoundResponse);
    }

    @Test
    public void getPolicyProperties_policyNotExist() {
        doPolicyValidations();
        ResponseFormat notFoundResponse = new ResponseFormat(Response.Status.NOT_FOUND.getStatusCode());
        when(componentsUtils.getResponseFormat(ActionStatus.POLICY_NOT_FOUND_ON_CONTAINER, "nonExistingPolicy", RESOURCE_ID)).thenReturn(notFoundResponse);
        Either<List<PropertyDataDefinition>, ResponseFormat> policyProperties = testInstance.getPolicyProperties(ComponentTypeEnum.RESOURCE, RESOURCE_ID, "nonExistingPolicy", USER_ID);
        assertThat(policyProperties.right().value()).isEqualTo(notFoundResponse);
    }

    @Test
    public void getPolicyProperties_noPropertiesOnPolicy() {
        doPolicyValidations();
        Either<List<PropertyDataDefinition>, ResponseFormat> policyProperties = testInstance.getPolicyProperties(ComponentTypeEnum.RESOURCE, RESOURCE_ID, NO_PROPS_POLICY, USER_ID);
        assertThat(policyProperties.left().value()).isNull();
    }

    @Test
    public void getPolicyProperties() {
        doPolicyValidations();
        Either<List<PropertyDataDefinition>, ResponseFormat> policyProperties = testInstance.getPolicyProperties(ComponentTypeEnum.RESOURCE, RESOURCE_ID, POLICY_ID, USER_ID);
        assertThat(policyProperties.left().value())
                .usingElementComparatorOnFields("uniqueId")
                .containsExactly(prop1, prop2);
    }

    private void doPolicyValidations() {
        when(userValidations.validateUserExists(eq(USER_ID), anyString(), eq(false))).thenReturn(new User());
        ArgumentCaptor<ComponentParametersView> filterCaptor = ArgumentCaptor.forClass(ComponentParametersView.class);
        when(toscaOperationFacade.getToscaElement(eq(RESOURCE_ID), filterCaptor.capture())).thenReturn(Either.left(resource));
    }
}
