/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.servlets;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import fj.data.Either;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.components.impl.GroupTypeBusinessLogic;
import org.openecomp.sdc.be.components.utils.GroupTypeBuilder;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.datatypes.elements.GroupTypeDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.DerivedFromOperation;
import org.openecomp.sdc.be.model.operations.impl.CapabilityOperation;
import org.openecomp.sdc.be.model.operations.impl.CapabilityTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.GroupTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.OperationUtils;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.resources.data.GroupTypeData;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

class GroupTypesEndpointTest extends JerseySpringBaseTest {

    private static final String USER_ID = "a001";
    private static final GroupTypeDefinition EMPTY_GROUP_TYPE = new GroupTypeDefinition();
    private static final String COMPONENT_TYPE = "VF";
    private static final HashSet<String> EXCLUDED_TYPES = Sets.newHashSet("Root", "Heat");

    private static ComponentsUtils componentsUtils;
    private static JanusGraphGenericDao janusGraphGenericDao;
    private static CapabilityTypeOperation capabilityTypeOperation;
    private static DerivedFromOperation derivedFromOperation;
    private static JanusGraphDao janusGraphDao;
    private static PropertyOperation propertyOperation;
    private static CapabilityOperation capabilityOperation;
    private static UserValidations userValidations;
    private static OperationUtils operationUtils;
    private static User user;

    static ConfigurationSource configurationSource = new FSConfigurationSource(
        ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be");
    static ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

    @org.springframework.context.annotation.Configuration
    @Import(BaseTestConfig.class)
    static class GroupTypesTestConfig {

        @Bean
        GroupTypesEndpoint groupTypesEndpoint() {
            UserBusinessLogic userBusinessLogic = mock(UserBusinessLogic.class);
            ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
            return new GroupTypesEndpoint(componentsUtils, groupTypeBusinessLogic());
        }

        @Bean
        GroupTypeBusinessLogic groupTypeBusinessLogic() {
            return new GroupTypeBusinessLogic(groupTypeOperation(), janusGraphDao, userValidations);
        }

        @Bean
        GroupTypeOperation groupTypeOperation() {
            return new GroupTypeOperation(janusGraphGenericDao, propertyOperation, capabilityTypeOperation,
                capabilityOperation, derivedFromOperation, operationUtils);
        }
    }

    @BeforeAll
    public static void initClass() {
        componentsUtils = mock(ComponentsUtils.class);
        propertyOperation = mock(PropertyOperation.class);
        capabilityTypeOperation = mock(CapabilityTypeOperation.class);
        janusGraphDao = mock(JanusGraphDao.class);
        janusGraphGenericDao = mock(JanusGraphGenericDao.class);
        userValidations = mock(UserValidations.class);
        operationUtils = mock(OperationUtils.class);
        user = mock(User.class);
    }

    @BeforeEach
    public void before() throws Exception {
        super.setUp();
        when(userValidations.validateUserExists(USER_ID)).thenReturn(user);
        when(
            janusGraphGenericDao.getByCriteriaWithPredicate(eq(NodeTypeEnum.GroupType), any(), eq(GroupTypeData.class), any()))
            .thenReturn(Either.left(buildGroupTypeDataList()));
    }

    @AfterEach
    void after() throws Exception {
        super.tearDown();
    }

    @Override
    protected void configureClient(ClientConfig config) {
        final JacksonJsonProvider jacksonJsonProvider = new JacksonJaxbJsonProvider()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        config.register(jacksonJsonProvider);
    }

    @Override
    protected ResourceConfig configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return super.configure(GroupTypesTestConfig.class)
            .register(GroupTypesEndpoint.class);
    }

    @Test
    void verifyExclusionMapIsCaseInsensitive() {
        Map<String, Set<String>> excludedGroupTypesMapping = configurationManager.getConfiguration()
            .getExcludedGroupTypesMapping();
        assertThat(excludedGroupTypesMapping.get(COMPONENT_TYPE))
            .hasSameElementsAs(excludedGroupTypesMapping.get(COMPONENT_TYPE.toLowerCase()));
    }

    @Test
    void getGroupTypes_validUser_Success() {
        List<GroupTypeDefinition> testConfigGroupTypes = buildGroupTypesList();
        List<GroupTypeDefinition> fetchedGroupTypes = buildGetGroupTypesCall(USER_ID, COMPONENT_TYPE)
            .get(new GenericType<>() {
            });
        verifyGroupTypesList(testConfigGroupTypes, fetchedGroupTypes);
    }

    @Test
    void getGroupTypes_whenNoInternalComponentType_passEmptyAsExcludedTypes() {
        List<GroupTypeDefinition> testConfigGroupTypes = buildGroupTypesList();
        List<GroupTypeDefinition> fetchedGroupTypes = buildGetGroupTypesCallNoInternalComponent(USER_ID)
            .get(new GenericType<>() {
            });
        verifyGroupTypesList(testConfigGroupTypes, fetchedGroupTypes);
    }

    private void verifyGroupTypesList(List<GroupTypeDefinition> groupTypes,
                                      List<GroupTypeDefinition> fetchedGroupTypes) {
        String[] expectedReturnFields = {"version", "type", "uniqueId", "name", "icon"};
        assertThat(fetchedGroupTypes)
            .usingElementComparatorOnFields(expectedReturnFields)
            .isEqualTo(groupTypes);
        verifyOnlySpecificFieldsInResponse(fetchedGroupTypes, expectedReturnFields);
    }

    private void verifyOnlySpecificFieldsInResponse(List<GroupTypeDefinition> fetchedGroupTypes, String... fields) {
        assertThat(fetchedGroupTypes)
            .usingElementComparatorIgnoringFields(fields)
            .containsOnly(EMPTY_GROUP_TYPE);
    }

    private Invocation.Builder buildGetGroupTypesCall(String userId, String componentType) {
        return target("/v1/catalog/groupTypes")
            .queryParam("internalComponentType", componentType)
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
            .setType("org.openecomp.groups.VfModule")
            .setUniqueId("id1")
            .setVersion("1.0")
            .setName("vf module")
            .setIcon("vf module icon")
            .build();
        GroupTypeDefinition type2 = GroupTypeBuilder.create()
            .setDerivedFrom("root")
            .setType("org.openecomp.groups.NetworkCollection")
            .setUniqueId("id2")
            .setVersion("1.0")
            .setName("network collection")
            .setIcon("network collection icon")
            .build();
        return asList(type1, type2);
    }

    private List<GroupTypeData> buildGroupTypeDataList() {
        GroupTypeDataDefinition d1 = new GroupTypeDataDefinition();
        d1.setType("org.openecomp.groups.VfModule");
        d1.setDerivedFrom("root");
        d1.setUniqueId("id1");
        d1.setVersion("1.0");
        d1.setName("vf module");
        d1.setIcon("vf module icon");
        GroupTypeData gt1 = new GroupTypeData(d1);
        GroupTypeDataDefinition d2 = new GroupTypeDataDefinition();
        d2.setType("org.openecomp.groups.NetworkCollection");
        d2.setDerivedFrom("root");
        d2.setUniqueId("id2");
        d2.setVersion("1.0");
        d2.setName("network collection");
        d2.setIcon("network collection icon");
        GroupTypeData gt2 = new GroupTypeData(d2);
        return asList(gt1, gt2);
    }

}
