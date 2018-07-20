package org.openecomp.sdc.be.servlets;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import fj.data.Either;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.sdc.be.DummyConfigurationManager;
import org.openecomp.sdc.be.components.impl.GroupTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.components.utils.GroupTypeBuilder;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.impl.GroupTypeOperation;
import org.openecomp.sdc.common.api.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GroupTypesEndpointTest extends JerseySpringBaseTest {

    private static final String USER_ID = "a001";
    private static final String INVALID_USER_ID = "a002";
    private static final GroupTypeDefinition EMPTY_GROUP_TYPE = new GroupTypeDefinition();
    private static final String COMPONENT_TYPE = "VF";
    private static final HashSet<String> EXCLUDED_TYPES = Sets.newHashSet("Root", "Heat");

    private static ComponentsUtils componentsUtils;
    private static GroupTypeOperation groupTypeOperation;
    private static TitanDao titanDao;
    private static UserValidations userValidations;
    private static User user;

    @Configuration
    @Import(BaseTestConfig.class)
    static class GroupTypesTestConfig {

        @Bean
        GroupTypesEndpoint groupTypesEndpoint() {
            return new GroupTypesEndpoint(groupTypeBusinessLogic());
        }

        @Bean
        GroupTypeBusinessLogic groupTypeBusinessLogic() {
            return new GroupTypeBusinessLogic(groupTypeOperation, titanDao, userValidations);
        }
    }

    @BeforeClass
    public static void initClass() {
        componentsUtils = mock(ComponentsUtils.class);
        groupTypeOperation = mock(GroupTypeOperation.class);
        titanDao = mock(TitanDao.class);
        userValidations = mock(UserValidations.class);
        user = mock(User.class);
    }

    @Before
    public void init() {
        new DummyConfigurationManager();
        when(ConfigurationManager.getConfigurationManager().getConfiguration().getExcludedGroupTypesMapping()).thenReturn(buildExcludeGroupTypesMap());
        ResponseFormatManager responseFormatManager = ResponseFormatManager.getInstance();
        when(userValidations.validateUserExists(eq(USER_ID), anyString(), anyBoolean())).thenReturn(Either.left(user));
        // TODO: handle for invalid user test
//        when(userValidations.validateUserExists(eq(INVALID_USER_ID), anyString(), anyBoolean())).thenReturn(Either.right(???)));
    }

    @Override
    protected void configureClient(ClientConfig config) {
        final JacksonJsonProvider jacksonJsonProvider = new JacksonJaxbJsonProvider().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        config.register(jacksonJsonProvider);
    }

    @Override
    protected ResourceConfig configure() {
        return super.configure(GroupTypesTestConfig.class)
                .register(GroupTypesEndpoint.class);
    }

    @Test
    public void getGroupTypes_validUser_Success() {
        List<GroupTypeDefinition> testConfigGroupTypes = buildGroupTypesList();
        when(groupTypeOperation.getAllGroupTypes(EXCLUDED_TYPES)).thenReturn(buildGroupTypesList());
        List<GroupTypeDefinition> fetchedGroupTypes = buildGetGroupTypesCall(USER_ID).get(new GenericType<List<GroupTypeDefinition>>(){});
        verifyGroupTypesList(testConfigGroupTypes, fetchedGroupTypes);
    }

    @Test
    public void getGroupTypes_whenNoInteranlComponentType_passEmptyAsExcludedTypes() {
        List<GroupTypeDefinition> testConfigGroupTypes = buildGroupTypesList();
        when(groupTypeOperation.getAllGroupTypes(null)).thenReturn(buildGroupTypesList());
        List<GroupTypeDefinition> fetchedGroupTypes = buildGetGroupTypesCallNoInternalComponent(USER_ID).get(new GenericType<List<GroupTypeDefinition>>(){});
        verifyGroupTypesList(testConfigGroupTypes, fetchedGroupTypes);
    }

    private void verifyGroupTypesList(List<GroupTypeDefinition> groupTypes, List<GroupTypeDefinition> fetchedGroupTypes) {
        assertThat(fetchedGroupTypes)
                .usingElementComparatorOnFields("version", "type", "uniqueId")
                .isEqualTo(groupTypes);
        verifyOnlySpecificFieldsInResponse(fetchedGroupTypes, "version", "type", "uniqueId");
    }

    private void verifyOnlySpecificFieldsInResponse(List<GroupTypeDefinition> fetchedGroupTypes, String ... fields) {
        assertThat(fetchedGroupTypes)
                .usingElementComparatorIgnoringFields(fields)
                .containsOnly(EMPTY_GROUP_TYPE);
    }

    private Invocation.Builder buildGetGroupTypesCall(String userId) {
        return target("/v1/catalog/groupTypes")
                .queryParam("internalComponentType", COMPONENT_TYPE)
                .request(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, userId);
    }

    private Invocation.Builder buildGetGroupTypesCallNoInternalComponent(String userId) {
        return target("/v1/catalog/groupTypes")
                .request(MediaType.APPLICATION_JSON)
                .header(Constants.USER_ID_HEADER, userId);
    }

    private Map<String, Set<String>> buildExcludeGroupTypesMap() {
        return new ImmutableMap.Builder<String, Set<String>>()
                .put("CR", Sets.newHashSet("VFModule", "Root", "Heat"))
                .put(COMPONENT_TYPE, EXCLUDED_TYPES)
                .build();
    }


    private List<GroupTypeDefinition> buildGroupTypesList() {
        GroupTypeDefinition type1 = GroupTypeBuilder.create()
                                                    .setDerivedFrom("root")
                                                    .setType("VFModule")
                                                    .setUniqueId("id1")
                                                    .setVersion("1.0")
                                                    .build();
        GroupTypeDefinition type2 = GroupTypeBuilder.create().setDerivedFrom("root").setType("Heat").setUniqueId("id2").build();
        return asList(type1, type2);
    }

    private GroupTypeDefinition[] listOfEmptyGroupTypes(int size) {
        return Stream.generate(GroupTypeDefinition::new).limit(size).toArray(GroupTypeDefinition[]::new);
    }


}