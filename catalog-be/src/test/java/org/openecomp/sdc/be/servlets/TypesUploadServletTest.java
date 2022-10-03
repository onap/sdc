/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.ArtifactTypeImportManager;
import org.openecomp.sdc.be.components.impl.CapabilityTypeImportManager;
import org.openecomp.sdc.be.components.impl.CategoriesImportManager;
import org.openecomp.sdc.be.components.impl.DataTypeImportManager;
import org.openecomp.sdc.be.components.impl.GroupTypeImportManager;
import org.openecomp.sdc.be.components.impl.InterfaceLifecycleTypeImportManager;
import org.openecomp.sdc.be.components.impl.PolicyTypeImportManager;
import org.openecomp.sdc.be.components.impl.RelationshipTypeImportManager;
import org.openecomp.sdc.be.components.impl.model.ToscaTypeImportData;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.RelationshipTypeDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.WebApplicationContext;

@TestInstance(Lifecycle.PER_CLASS)
class TypesUploadServletTest extends JerseyTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;
    @Mock
    private ServletContext servletContext;
    @Mock
    private WebAppContextWrapper webAppContextWrapper;
    @Mock
    private WebApplicationContext webApplicationContext;
    @Mock
    private CapabilityTypeImportManager capabilityTypeImportManager;
    @Mock
    private DataTypeImportManager dataTypeImportManager;
    @Mock
    private RelationshipTypeImportManager relationshipTypeImportManager;
    @Mock
    private PolicyTypeImportManager policyTypeImportManager;
    @Mock
    private InterfaceLifecycleTypeImportManager interfaceLifecycleTypeImportManager;
    @Mock
    private GroupTypeImportManager groupTypeImportManager;
    @Mock
    private CategoriesImportManager categoriesImportManager;
    @Mock
    private ServletUtils servletUtils;
    @Mock
    private UserBusinessLogic userAdmin;
    @Mock
    private ComponentsUtils componentUtils;
    @Mock
    private ResponseFormat responseFormat;
    @Mock
    private ArtifactTypeImportManager artifactTypeImportManager;

    @BeforeAll
    public void setup() {
        ExternalConfiguration.setAppName("catalog-be");
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
        when(webApplicationContext.getBean(CapabilityTypeImportManager.class)).thenReturn(capabilityTypeImportManager);
        when(webApplicationContext.getBean(DataTypeImportManager.class)).thenReturn(dataTypeImportManager);
        when(webApplicationContext.getBean(RelationshipTypeImportManager.class)).thenReturn(relationshipTypeImportManager);
        when(webApplicationContext.getBean(PolicyTypeImportManager.class)).thenReturn(policyTypeImportManager);
        when(webApplicationContext.getBean(InterfaceLifecycleTypeImportManager.class)).thenReturn(interfaceLifecycleTypeImportManager);
        when(webApplicationContext.getBean(GroupTypeImportManager.class)).thenReturn(groupTypeImportManager);
        when(webApplicationContext.getBean(CategoriesImportManager.class)).thenReturn(categoriesImportManager);
        when(webApplicationContext.getBean(ArtifactTypeImportManager.class)).thenReturn(artifactTypeImportManager);
        when(webApplicationContext.getBean(ServletUtils.class)).thenReturn(servletUtils);
        when(servletUtils.getComponentsUtils()).thenReturn(componentUtils);
        when(servletUtils.getUserAdmin()).thenReturn(userAdmin);
        final String userId = "jh0003";
        final User user = new User(userId);
        user.setRole(Role.ADMIN.name());
        when(userAdmin.getUser(userId)).thenReturn(user);
        when(request.getHeader(Constants.USER_ID_HEADER)).thenReturn(userId);
        when(responseFormat.getStatus()).thenReturn(HttpStatus.CREATED_201);
        when(componentUtils.getResponseFormat(ActionStatus.CREATED)).thenReturn(responseFormat);
        when(componentUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
    }

    @BeforeEach
    public void before() throws Exception {
        super.setUp();
    }

    @AfterEach
    void after() throws Exception {
        super.tearDown();
    }

    @Test
    void creatingCapabilityTypeSuccessTest() {
        final ImmutablePair<CapabilityTypeDefinition, Boolean> immutablePair = new ImmutablePair<>(new CapabilityTypeDefinition(), true);
        final Either<List<ImmutablePair<CapabilityTypeDefinition, Boolean>>, ResponseFormat> either = Either.left(Arrays.asList(immutablePair));
        when(capabilityTypeImportManager.createCapabilityTypes(anyString(), Mockito.isNull(), Mockito.anyBoolean())).thenReturn(either);
        final FileDataBodyPart filePart = new FileDataBodyPart("capabilityTypeZip", new File("src/test/resources/types/capabilityTypes.zip"));
        MultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);

        final Response response = target().path("/v1/catalog/uploadType/capability").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

        assertEquals(HttpStatus.CREATED_201, response.getStatus());
    }

    @Test
    void creatingCapabilityType_Either_isRight_FailedTest() {
        final Either<List<ImmutablePair<CapabilityTypeDefinition, Boolean>>, ResponseFormat> either = Either.right(new ResponseFormat(500));
        when(capabilityTypeImportManager.createCapabilityTypes(anyString(), Mockito.isNull(), Mockito.anyBoolean())).thenReturn(either);
        final FileDataBodyPart filePart = new FileDataBodyPart("capabilityTypeZip", new File("src/test/resources/types/capabilityTypes.zip"));
        MultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);

        final Response response = target().path("/v1/catalog/uploadType/capability").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Test
    void creatingCapabilityTypeWithModelSuccessTest() {
        final ImmutablePair<CapabilityTypeDefinition, Boolean> immutablePair = new ImmutablePair<>(new CapabilityTypeDefinition(), true);
        final Either<List<ImmutablePair<CapabilityTypeDefinition, Boolean>>, ResponseFormat> either = Either.left(Arrays.asList(immutablePair));
        when(capabilityTypeImportManager.createCapabilityTypes(anyString(), Mockito.eq("testModel"), Mockito.anyBoolean())).thenReturn(
            either);
        final FileDataBodyPart filePart = new FileDataBodyPart("capabilityTypeZip", new File("src/test/resources/types/capabilityTypes.zip"));
        FormDataMultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);
        multipartEntity.field("model", "testModel");

        final Response response = target().path("/v1/catalog/uploadType/capability").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

        assertEquals(HttpStatus.CREATED_201, response.getStatus());
    }

    @Test
    void creatingDataTypeSuccessTest() {
        final ImmutablePair<DataTypeDefinition, Boolean> immutablePair = new ImmutablePair<>(new DataTypeDefinition(), true);
        final Either<List<ImmutablePair<DataTypeDefinition, Boolean>>, ResponseFormat> either = Either.left(Arrays.asList(immutablePair));
        when(dataTypeImportManager.createDataTypes(anyString(), Mockito.isNull(), Mockito.anyBoolean())).thenReturn(either);
        final FileDataBodyPart filePart = new FileDataBodyPart("dataTypesZip", new File("src/test/resources/types/datatypes.zip"));
        MultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);

        final Response response = target().path("/v1/catalog/uploadType/datatypes").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

        assertEquals(HttpStatus.CREATED_201, response.getStatus());
    }

    @Test
    void creatingDataType_Either_isRight_FailedTest() {
        final Either<List<ImmutablePair<DataTypeDefinition, Boolean>>, ResponseFormat> either = Either.right(new ResponseFormat(500));
        when(dataTypeImportManager.createDataTypes(anyString(), Mockito.isNull(), Mockito.anyBoolean())).thenReturn(either);
        final FileDataBodyPart filePart = new FileDataBodyPart("dataTypesZip", new File("src/test/resources/types/datatypes.zip"));
        MultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);

        final Response response = target().path("/v1/catalog/uploadType/datatypes").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Test
    void creatingDataType_AlreadyExists_FailedTest() {
        final ImmutablePair<DataTypeDefinition, Boolean> immutablePair = new ImmutablePair<>(new DataTypeDefinition(), false);
        final Either<List<ImmutablePair<DataTypeDefinition, Boolean>>, ResponseFormat> either = Either.left(Arrays.asList(immutablePair));
        when(dataTypeImportManager.createDataTypes(anyString(), Mockito.isNull(), Mockito.anyBoolean())).thenReturn(either);
        final FileDataBodyPart filePart = new FileDataBodyPart("dataTypesZip", new File("src/test/resources/types/datatypes.zip"));
        MultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);

        final Response response = target().path("/v1/catalog/uploadType/datatypes").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Test
    void creatingDataTypeWithModelSuccessTest() {
        final ImmutablePair<DataTypeDefinition, Boolean> immutablePair = new ImmutablePair<>(new DataTypeDefinition(), true);
        final Either<List<ImmutablePair<DataTypeDefinition, Boolean>>, ResponseFormat> either = Either.left(Arrays.asList(immutablePair));
        when(dataTypeImportManager.createDataTypes(anyString(), Mockito.eq("testModel"), Mockito.anyBoolean())).thenReturn(either);
        final FileDataBodyPart filePart = new FileDataBodyPart("dataTypesZip", new File("src/test/resources/types/datatypes.zip"));
        FormDataMultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);
        multipartEntity.field("model", "testModel");

        final Response response = target().path("/v1/catalog/uploadType/datatypes").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

        assertEquals(HttpStatus.CREATED_201, response.getStatus());
    }

    @Test
    void creatingRelationshipTypeSuccessTest() {
        final ImmutablePair<RelationshipTypeDefinition, Boolean> immutablePair = new ImmutablePair<>(new RelationshipTypeDefinition(), true);
        final Either<List<ImmutablePair<RelationshipTypeDefinition, Boolean>>, ResponseFormat> either = Either.left(Arrays.asList(immutablePair));
        when(relationshipTypeImportManager.createRelationshipTypes(anyString(), Mockito.isNull(), Mockito.anyBoolean())).thenReturn(either);
        final FileDataBodyPart filePart = new FileDataBodyPart("relationshipTypeZip", new File("src/test/resources/types/relationship.zip"));
        MultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);

        final Response response = target().path("/v1/catalog/uploadType/relationship").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

        assertEquals(HttpStatus.CREATED_201, response.getStatus());
    }

    @Test
    void creatingRelationshipType_AlreadyExists_FailedTest() {
        final ImmutablePair<RelationshipTypeDefinition, Boolean> immutablePair = new ImmutablePair<>(new RelationshipTypeDefinition(), false);
        final Either<List<ImmutablePair<RelationshipTypeDefinition, Boolean>>, ResponseFormat> either = Either.left(Arrays.asList(immutablePair));
        when(relationshipTypeImportManager.createRelationshipTypes(anyString(), Mockito.isNull(), Mockito.anyBoolean())).thenReturn(either);
        final FileDataBodyPart filePart = new FileDataBodyPart("relationshipTypeZip", new File("src/test/resources/types/relationship.zip"));
        MultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);

        final Response response = target().path("/v1/catalog/uploadType/relationship").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Test
    void creatingRelationshipTypeWithModelSuccessTest() {
        final ImmutablePair<RelationshipTypeDefinition, Boolean> immutablePair = new ImmutablePair<>(new RelationshipTypeDefinition(), true);
        final Either<List<ImmutablePair<RelationshipTypeDefinition, Boolean>>, ResponseFormat> either = Either.left(Arrays.asList(immutablePair));
        when(relationshipTypeImportManager.createRelationshipTypes(anyString(), Mockito.eq("testModel"), Mockito.anyBoolean())).thenReturn(
            either);
        final FileDataBodyPart filePart = new FileDataBodyPart("relationshipTypeZip", new File("src/test/resources/types/relationship.zip"));
        FormDataMultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);
        multipartEntity.field("model", "testModel");

        final Response response = target().path("/v1/catalog/uploadType/relationship").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

        assertEquals(HttpStatus.CREATED_201, response.getStatus());
    }

    @Test
    void creatingPolicyTypeSuccessTest() {
        final ImmutablePair<PolicyTypeDefinition, Boolean> immutablePair = new ImmutablePair<>(new PolicyTypeDefinition(), true);
        final Either<List<ImmutablePair<PolicyTypeDefinition, Boolean>>, ResponseFormat> either = Either.left(Arrays.asList(immutablePair));
        when(policyTypeImportManager.createPolicyTypes(Mockito.any(ToscaTypeImportData.class), Mockito.isNull(), Mockito.anyBoolean())).thenReturn(
            either);
        final FileDataBodyPart filePart = new FileDataBodyPart("policyTypesZip", new File("src/test/resources/types/policy.zip"));
        MultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);

        final Response response = target().path("/v1/catalog/uploadType/policytypes").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

        assertEquals(HttpStatus.CREATED_201, response.getStatus());
    }

    @Test
    void creatingPolicyType_AlreadyExists_FailedTest() {
        final ImmutablePair<PolicyTypeDefinition, Boolean> immutablePair = new ImmutablePair<>(new PolicyTypeDefinition(), false);
        final Either<List<ImmutablePair<PolicyTypeDefinition, Boolean>>, ResponseFormat> either = Either.left(Arrays.asList(immutablePair));
        when(policyTypeImportManager.createPolicyTypes(Mockito.any(ToscaTypeImportData.class), Mockito.isNull(), Mockito.anyBoolean())).thenReturn(
            either);
        final FileDataBodyPart filePart = new FileDataBodyPart("policyTypesZip", new File("src/test/resources/types/policy.zip"));
        MultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);

        final Response response = target().path("/v1/catalog/uploadType/policytypes").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Test
    void creatingPolicyTypeWithModelSuccessTest() {
        final ImmutablePair<PolicyTypeDefinition, Boolean> immutablePair = new ImmutablePair<>(new PolicyTypeDefinition(), true);
        final Either<List<ImmutablePair<PolicyTypeDefinition, Boolean>>, ResponseFormat> either = Either.left(Arrays.asList(immutablePair));
        when(policyTypeImportManager.createPolicyTypes(Mockito.any(ToscaTypeImportData.class), Mockito.eq("testModel"),
            Mockito.anyBoolean())).thenReturn(either);
        final FileDataBodyPart filePart = new FileDataBodyPart("policyTypesZip", new File("src/test/resources/types/policy.zip"));
        FormDataMultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);
        multipartEntity.field("model", "testModel");

        final Response response = target().path("/v1/catalog/uploadType/policytypes").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

        assertEquals(HttpStatus.CREATED_201, response.getStatus());
    }

    @Test
    void creatingInterfaceLifecycleTypeSuccessTest() {
        final ImmutablePair<InterfaceDefinition, Boolean> immutablePair = new ImmutablePair<>(new InterfaceDefinition(), true);
        final Either<List<InterfaceDefinition>, ResponseFormat> either = Either.left(emptyList());
        when(interfaceLifecycleTypeImportManager.createLifecycleTypes(anyString(), Mockito.isNull(), Mockito.anyBoolean()))
            .thenReturn(either);
        final FileDataBodyPart filePart = new FileDataBodyPart("interfaceLifecycleTypeZip",
            new File("src/test/resources/types/interfaceLifecycleTypes.zip"));
        MultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);

        final Response response = target().path("/v1/catalog/uploadType/interfaceLifecycle").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

        assertEquals(HttpStatus.CREATED_201, response.getStatus());
    }

    @Test
    void creatingInterfaceLifecycleType_Either_isRight_FailedTest() {
        final Either<List<InterfaceDefinition>, ResponseFormat> either = Either.right(new ResponseFormat(500));
        when(interfaceLifecycleTypeImportManager.createLifecycleTypes(anyString(), Mockito.isNull(), Mockito.anyBoolean()))
            .thenReturn(either);
        final FileDataBodyPart filePart = new FileDataBodyPart("interfaceLifecycleTypeZip",
            new File("src/test/resources/types/interfaceLifecycleTypes.zip"));
        MultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);

        final Response response = target().path("/v1/catalog/uploadType/interfaceLifecycle").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Test
    void creatingInterfaceLifecycleTypeWithModelSuccessTest() {
        final ImmutablePair<InterfaceDefinition, Boolean> immutablePair = new ImmutablePair<>(new InterfaceDefinition(), true);
        final Either<List<InterfaceDefinition>, ResponseFormat> either = Either.left(emptyList());
        when(interfaceLifecycleTypeImportManager.createLifecycleTypes(anyString(), Mockito.eq("testModel"), Mockito.anyBoolean()))
            .thenReturn(either);
        final FileDataBodyPart filePart = new FileDataBodyPart("interfaceLifecycleTypeZip",
            new File("src/test/resources/types/interfaceLifecycleTypes.zip"));
        FormDataMultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);
        multipartEntity.field("model", "testModel");

        final Response response = target().path("/v1/catalog/uploadType/interfaceLifecycle").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

        assertEquals(HttpStatus.CREATED_201, response.getStatus());
    }

    @Test
    void creatingGroupTypesSuccessTest() {
        final ImmutablePair<GroupTypeDefinition, Boolean> immutablePair = new ImmutablePair<>(new GroupTypeDefinition(), true);
        final Either<List<ImmutablePair<GroupTypeDefinition, Boolean>>, ResponseFormat> either = Either.left(Arrays.asList(immutablePair));
        when(groupTypeImportManager.createGroupTypes(Mockito.any(ToscaTypeImportData.class), Mockito.isNull(), Mockito.anyBoolean()))
            .thenReturn(either);
        final FileDataBodyPart filePart = new FileDataBodyPart("groupTypesZip", new File("src/test/resources/types/group.zip"));
        MultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);

        final Response response = target().path("/v1/catalog/uploadType/grouptypes").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

        assertEquals(HttpStatus.CREATED_201, response.getStatus());
    }

    @Test
    void creatingGroupTypes_AlreadyExists_FailedTest() {
        final ImmutablePair<GroupTypeDefinition, Boolean> immutablePair = new ImmutablePair<>(new GroupTypeDefinition(), false);
        final Either<List<ImmutablePair<GroupTypeDefinition, Boolean>>, ResponseFormat> either = Either.left(Arrays.asList(immutablePair));
        when(groupTypeImportManager.createGroupTypes(Mockito.any(ToscaTypeImportData.class), Mockito.isNull(), Mockito.anyBoolean()))
            .thenReturn(either);
        final FileDataBodyPart filePart = new FileDataBodyPart("groupTypesZip", new File("src/test/resources/types/group.zip"));
        MultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);

        final Response response = target().path("/v1/catalog/uploadType/grouptypes").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Test
    void creatingGroupTypesWithModelSuccessTest() {
        final ImmutablePair<GroupTypeDefinition, Boolean> immutablePair = new ImmutablePair<>(new GroupTypeDefinition(), true);
        final Either<List<ImmutablePair<GroupTypeDefinition, Boolean>>, ResponseFormat> either = Either.left(Arrays.asList(immutablePair));
        when(groupTypeImportManager.createGroupTypes(Mockito.any(ToscaTypeImportData.class), Mockito.eq("testModel"), Mockito.anyBoolean()))
            .thenReturn(either);
        final FileDataBodyPart filePart = new FileDataBodyPart("groupTypesZip", new File("src/test/resources/types/group.zip"));
        FormDataMultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);
        multipartEntity.field("model", "testModel");

        final Response response = target().path("/v1/catalog/uploadType/grouptypes").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

        assertEquals(HttpStatus.CREATED_201, response.getStatus());
    }

    @Test
    void creatingCategoriesTypeSuccessTest() {
        final Either<Map<String, List<CategoryDefinition>>, ResponseFormat> either = Either.left(emptyMap());
        when(categoriesImportManager.createCategories(anyString())).thenReturn(either);
        final FileDataBodyPart filePart = new FileDataBodyPart("categoriesZip", new File("src/test/resources/types/categoryTypes.zip"));
        MultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);

        final Response response = target().path("/v1/catalog/uploadType/categories").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

        assertEquals(HttpStatus.CREATED_201, response.getStatus());
    }

    @Test
    void creatingArtifactTypeSuccessTest() {
        when(artifactTypeImportManager.createArtifactTypes(anyString(), anyString(), anyBoolean()))
            .thenReturn(Either.left(emptyList()));
        final FileDataBodyPart filePart = new FileDataBodyPart("artifactsZip", new File("src/test/resources/types/artifactTypes.zip"));
        final FormDataMultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);
        multipartEntity.field("model", "testModel");
        final Response response = target().path("/v1/catalog/uploadType/artifactTypes").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);
        assertEquals(HttpStatus.CREATED_201, response.getStatus());
    }

    @Test
    void creatingArtifactTypeFailTest() {
        when(artifactTypeImportManager.createArtifactTypes(anyString(), anyString(), anyBoolean()))
            .thenReturn(Either.right(new ResponseFormat(500)));
        final FormDataMultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.field("model", "testModel");
        final Response response = target().path("/v1/catalog/uploadType/artifactTypes").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(MultiPartFeature.class);
    }

    @Override
    protected ResourceConfig configure() {
        MockitoAnnotations.openMocks(this);

        forceSet(TestProperties.CONTAINER_PORT, "0");
        final TypesUploadServlet typesUploadServlet = new TypesUploadServlet(null, componentUtils,
            servletUtils, null, capabilityTypeImportManager, interfaceLifecycleTypeImportManager,
            categoriesImportManager, dataTypeImportManager,
            groupTypeImportManager, policyTypeImportManager, relationshipTypeImportManager, artifactTypeImportManager);
        final ResourceConfig resourceConfig = new ResourceConfig().register(typesUploadServlet);

        resourceConfig.register(MultiPartFeature.class);
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                // The below code was cut-pasted to here from setup() because
                // due to it now has
                // to be executed during servlet initialization
                bind(request).to(HttpServletRequest.class);
                when(request.getSession()).thenReturn(session);
                when(session.getServletContext()).thenReturn(servletContext);
                String appConfigDir = "src/test/resources/config/catalog-be";
                ConfigurationSource configurationSource = new FSConfigurationSource(
                    ExternalConfiguration.getChangeListener(), appConfigDir);
                ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
                for (final String mandatoryHeader : configurationManager.getConfiguration().getIdentificationHeaderFields()) {
                    when(request.getHeader(mandatoryHeader)).thenReturn(mandatoryHeader);
                }

                when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR))
                    .thenReturn(configurationManager);
            }
        });
        final ApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
        resourceConfig.property("contextConfig", context);

        return resourceConfig;
    }
}
