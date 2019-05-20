package org.openecomp.sdc.be.servlets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import fj.data.Either;
import org.assertj.core.api.AssertionsForClassTypes;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.be.components.impl.GroupBusinessLogicNew;
import org.openecomp.sdc.be.components.validation.AccessValidations;
import org.openecomp.sdc.be.components.validation.ComponentValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupProperty;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.GroupsOperation;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.GroupOperation;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.*;
import static org.openecomp.sdc.be.model.operations.api.StorageOperationStatus.NOT_FOUND;

public class GroupEndpointTest extends JerseySpringBaseTest {

    private static final String VALID_USER = "ab001";
    private static final String INVALID_USER = "ab002";
    private static final String VALID_COMPONENT_ID = "12345";
    private static final String INVALID_COMPONENT_ID = "9999";
    private static final String VALID_GROUP_ID = "1";
    private static final String INVALID_GROUP_ID = "2";
    public static final String A = "a";
    private static final String VL1 = "VL1";
    private static final String OLD_VALUE = "old value";
    private static AccessValidations accessValidations;
    private static ComponentValidations componentValidations;
    private static GroupsOperation groupsOperation;
    private static GroupOperation groupOperation;
    private Resource cr;
    private GroupDefinition g1;
    private ComponentInstance ci;
    private GroupProperty gp1;

    @Configuration
    @Import(BaseTestConfig.class)
    static class GroupEndpointTestConfig {

        @Bean
        GroupEndpoint groupEndpoint() {
            return new GroupEndpoint(groupBusinessLogic());
        }

        @Bean
        GroupBusinessLogicNew groupBusinessLogic() {
            return new GroupBusinessLogicNew(accessValidations, componentValidations, groupsOperation, groupOperation);
        }
    }

    @BeforeClass
    public static void initClass() {
        ExternalConfiguration.setAppName("catalog-be");
        String appConfigDir = "src/test/resources/config/catalog-be";
        ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
        //ComponentsUtils needs configuration singleton to be set
        componentValidations = mock(ComponentValidations.class);
        accessValidations = mock(AccessValidations.class);
        groupsOperation = mock(GroupsOperation.class);
        groupOperation = mock(GroupOperation.class);

    }

    @Override
    protected void configureClient(ClientConfig config) {
        final JacksonJsonProvider jacksonJsonProvider = new JacksonJaxbJsonProvider()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        config.register(jacksonJsonProvider);
    }

    @Override
    protected ResourceConfig configure() {
        return super.configure(GroupEndpointTestConfig.class)
                .register(GroupEndpoint.class)
                .property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_SERVER, "WARNING");
    }

    @Before
    public void init() {
        cr = new Resource();
        cr.setSystemName("CR1");
        g1 = new GroupDefinition();
        g1.setUniqueId(VALID_GROUP_ID);
        gp1 = new GroupProperty();
        gp1.setUniqueId("1");
        gp1.setName("p1");
        gp1.setValue(OLD_VALUE);
        g1.setProperties(Arrays.asList(gp1));
        cr.setGroups(Arrays.asList(g1));
        ci = new ComponentInstance();
        ci.setUniqueId(A);
        ci.setName(VL1);
        happyScenarioSetup();
        unhappyScenarioSetup();
    }

    private void unhappyScenarioSetup() {
        when(accessValidations.validateUserCanWorkOnComponent(eq(INVALID_COMPONENT_ID), eq(ComponentTypeEnum.RESOURCE), eq(VALID_USER), anyString())).thenThrow(new StorageException(NOT_FOUND, INVALID_COMPONENT_ID));
        when(componentValidations.getComponentInstance(cr, A)).thenReturn(Optional.of(ci));
    }


    private void happyScenarioSetup() {
        when(accessValidations.validateUserCanWorkOnComponent(eq(VALID_COMPONENT_ID), any(ComponentTypeEnum.class), eq(VALID_USER), anyString())).thenReturn(cr);
        when(accessValidations.validateUserCanRetrieveComponentData(eq(VALID_COMPONENT_ID), eq("resources"), eq(VALID_USER), anyString()))
                .thenReturn(cr);
        when(componentValidations.getComponentInstance(cr, A)).thenReturn(Optional.of(ci));
        doNothing().when(groupsOperation).updateGroupOnComponent(eq(VALID_COMPONENT_ID), isA(GroupDefinition.class));
        when(groupOperation.validateAndUpdatePropertyValue(isA(GroupProperty.class))).thenReturn(StorageOperationStatus.OK);
        when(groupsOperation.updateGroupPropertiesOnComponent(eq(VALID_COMPONENT_ID), isA(GroupDefinition.class), anyList())).thenAnswer(new Answer<Either>() {
            @Override
            public Either answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                return Either.left(Arrays.asList(args[2]));
            }
        });
    }

    @Test
    public void updateGroupMembers_success() {
        List<String> ids = Arrays.asList(A);
        List<String> updatedIds = buildUpdateGroupMembersCall(VALID_COMPONENT_ID, VALID_GROUP_ID, VALID_USER)
                .post(Entity.entity(ids, MediaType.APPLICATION_JSON), new GenericType<List<String>>() {
                });
        assertThat(updatedIds.size()).isEqualTo(ids.size());
        assertThat(updatedIds).containsExactlyInAnyOrder(ids.toArray(new String[ids.size()]));
    }

    @Test
    public void updateGroupMembersWith2IdenticalMembers_success() {
        List<String> ids = Arrays.asList(A, A);
        List<String> updatedIds = buildUpdateGroupMembersCall(VALID_COMPONENT_ID, VALID_GROUP_ID, VALID_USER)
                .post(Entity.entity(ids, MediaType.APPLICATION_JSON), new GenericType<List<String>>() {
                });
        assertThat(updatedIds.size()).isEqualTo(1);
        assertThat(updatedIds).containsExactlyInAnyOrder(String.valueOf(A));
    }

    @Test
    public void updateGroupMembersWithEmptyList_success() {
        List<String> ids = new ArrayList<>();
        List<String> updatedIds = buildUpdateGroupMembersCall(VALID_COMPONENT_ID, VALID_GROUP_ID, VALID_USER)
                .post(Entity.entity(ids, MediaType.APPLICATION_JSON), new GenericType<List<String>>() {
                });
        assertThat(updatedIds.size()).isEqualTo(0);
    }

    @Test
    public void updateGroupMember_InvalidComponentId_failure() {
        List<String> ids = new ArrayList<>();
        Response response = buildUpdateGroupMembersCall(INVALID_COMPONENT_ID, VALID_GROUP_ID, VALID_USER)
                .post(Entity.entity(ids, MediaType.APPLICATION_JSON), Response.class);
        AssertionsForClassTypes.assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void updateGroupProperty_success() {
        GroupProperty property = new GroupProperty();
        property.setValue("value1");
        property.setName("prop");
        String propertyStr = "[{\"uniqueId\":\"1\",\"type\":null,\"required\":false,\"definition\":false,\"defaultValue\":null,\"description\":null,\"schema\":null,\"password\":false,\"name\":\"p1\",\"value\":\"new value\",\"label\":null,\"hidden\":false,\"immutable\":false,\"inputPath\":null,\"status\":null,\"inputId\":null,\"instanceUniqueId\":null,\"propertyId\":null,\"parentUniqueId\":null,\"getInputValues\":null,\"constraints\":null,\"valueUniqueUid\":null,\"ownerId\":null}]";
        List<GroupProperty> properties = Arrays.asList(property);
        //TODO define GroupPropertyDTO (after finding other usage in UI code) and improve test coverage
//        List<GroupProperty> updatedProperties = buildUpdateGroupPropertiesCall(VALID_COMPONENT_ID, VALID_GROUP_ID, VALID_USER)
//                .put(Entity.entity(propertyStr, MediaType.APPLICATION_JSON), new GenericType<List<GroupProperty>>() {
//                });
//        assertThat(updatedProperties.size()).isEqualTo(1);
        Response response = buildUpdateGroupPropertiesCall(VALID_COMPONENT_ID, VALID_GROUP_ID, VALID_USER)
                .put(Entity.entity(propertyStr, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void getGroupProperties_success() {
        List<PropertyDataDefinition> properties = buildUpdateGroupPropertiesCall(VALID_COMPONENT_ID, VALID_GROUP_ID, VALID_USER)
                .get(new GenericType<List<PropertyDataDefinition>>(){});
        assertThat(properties.size()).isEqualTo(1);
        assertThat(properties.get(0).getValue()).isEqualTo(OLD_VALUE);
    }

    private Invocation.Builder buildUpdateGroupMembersCall(String componentId, String groupId, String userId) {
        return target("/v1/catalog/resources/" + componentId + "/groups/" + groupId + "/members")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, userId);
    }

    private Invocation.Builder buildUpdateGroupPropertiesCall(String componentId, String groupId, String userId) {
        return target("/v1/catalog/resources/" + componentId + "/groups/" + groupId + "/properties")
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, userId);
    }
	

}
