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

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.DeserializationFeature;
import fj.data.Either;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.jetty.http.HttpStatus;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.CommonImportManager;
import org.openecomp.sdc.be.components.validation.AccessValidations;
import org.openecomp.sdc.be.dao.janusgraph.HealingJanusGraphGenericDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.AnnotationTypeDefinition;
import org.openecomp.sdc.be.model.operations.StorageException;
import org.openecomp.sdc.be.model.operations.impl.AnnotationTypeOperations;
import org.openecomp.sdc.be.model.operations.impl.CommonTypeOperations;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;
import org.openecomp.sdc.be.model.operations.impl.OperationUtils;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.be.resources.data.AnnotationTypeData;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

class TypesUploadEndpointTest extends JerseySpringBaseTest {

    static final String userId = "jh0003";

    private static AccessValidations accessValidations;
    private static HealingJanusGraphGenericDao janusGraphGenericDao;
    private static PropertyOperation propertyOperation;
    private static ComponentsUtils componentUtils;
    private static OperationUtils operationUtils;
    private static ModelOperation modelOperation;

    @BeforeAll
    public static void initClass() {
        janusGraphGenericDao = mock(HealingJanusGraphGenericDao.class);
        accessValidations = mock(AccessValidations.class);
        propertyOperation = mock(PropertyOperation.class);
        modelOperation = mock(ModelOperation.class);
        componentUtils = Mockito.mock(ComponentsUtils.class);
        operationUtils = Mockito.mock(OperationUtils.class);
    }

    @BeforeEach
    public void before() throws Exception {
        super.setUp();
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
        config.register(MultiPartFeature.class);
    }

    @Override
    protected ResourceConfig configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return super.configure(TypesUploadEndpointTest.TypesUploadTestConfig.class)
            .register(TypesUploadEndpoint.class)
            .property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_SERVER, "WARNING");
    }

    @Test
    void creatingAnnotationTypeSuccessTest() {
        doNothing().when(accessValidations).validateUserExists(eq(userId), anyString());
        when(janusGraphGenericDao.createNode(isA(AnnotationTypeData.class), eq(AnnotationTypeData.class)))
            .thenReturn(Either.left(new AnnotationTypeData()));
        when(janusGraphGenericDao.getNode(anyString(), eq("org.openecomp.annotations.source.1.0.annotationtype"),
            eq(AnnotationTypeData.class))).thenReturn(Either.left(new AnnotationTypeData()));
        when(
            janusGraphGenericDao.getByCriteria(eq(NodeTypeEnum.AnnotationType), anyMap(), eq(AnnotationTypeData.class)))
            .thenReturn(Either.right(
                JanusGraphOperationStatus.NOT_FOUND));
        when(propertyOperation.addPropertiesToElementType(anyString(), eq(NodeTypeEnum.AnnotationType), anyList()))
            .thenReturn(Either.left(emptyMap()));
        when(propertyOperation.fillPropertiesList(anyString(), eq(NodeTypeEnum.AnnotationType), any())).thenReturn(
            JanusGraphOperationStatus.OK);
        when(propertyOperation.getJanusGraphGenericDao()).thenReturn(janusGraphGenericDao);
        when(janusGraphGenericDao.commit()).thenReturn(JanusGraphOperationStatus.OK);
        when(janusGraphGenericDao.rollback()).thenReturn(JanusGraphOperationStatus.OK);
        FileDataBodyPart filePart = new FileDataBodyPart("annotationTypesZip",
            new File("src/test/resources/types/annotationTypes.zip"));
        MultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);

        Response response = target().path("/v1/catalog/uploadType/annotationtypes")
            .request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, userId)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED_201);
        assertThat(Boolean.valueOf(getTypeActionResult(response))).isTrue();
    }

    private String getTypeActionResult(Response response) {
        String typeResult = "";
        String body = response.readEntity(String.class);
        int indexColon = body.lastIndexOf(':');
        if (indexColon > 0) {
            int indexCurly = body.indexOf('}', indexColon);
            if (indexCurly > 0) {
                typeResult = body.substring(indexColon + 1, indexCurly);
            }
        }
        return typeResult;
    }

    @Test
    void creatingAnnotationTypeFailureTest() {
        doNothing().when(accessValidations).validateUserExists(eq(userId), anyString());
        when(janusGraphGenericDao.createNode(isA(AnnotationTypeData.class), eq(AnnotationTypeData.class)))
            .thenReturn(Either.left(new AnnotationTypeData()));
        when(janusGraphGenericDao.getNode(anyString(), eq("org.openecomp.annotations.source.1.0.annotationtype"),
            eq(AnnotationTypeData.class))).thenReturn(Either.left(new AnnotationTypeData()));
        when(
            janusGraphGenericDao.getByCriteria(eq(NodeTypeEnum.AnnotationType), anyMap(), eq(AnnotationTypeData.class)))
            .thenReturn(Either.right(
                JanusGraphOperationStatus.NOT_FOUND));
        when(propertyOperation.addPropertiesToElementType(anyString(), eq(NodeTypeEnum.AnnotationType), anyList()))
            .thenThrow(new StorageException(
                JanusGraphOperationStatus.MATCH_NOT_FOUND));
        when(propertyOperation.fillPropertiesList(anyString(), eq(NodeTypeEnum.AnnotationType), any())).thenReturn(
            JanusGraphOperationStatus.OK);
        when(propertyOperation.getJanusGraphGenericDao()).thenReturn(janusGraphGenericDao);
        when(janusGraphGenericDao.commit()).thenReturn(JanusGraphOperationStatus.OK);
        when(janusGraphGenericDao.rollback()).thenReturn(JanusGraphOperationStatus.OK);
        FileDataBodyPart filePart = new FileDataBodyPart("annotationTypesZip",
            new File("src/test/resources/types/annotationTypes.zip"));
        MultiPart multipartEntity = new FormDataMultiPart();
        multipartEntity.bodyPart(filePart);

        Response response = target().path("/v1/catalog/uploadType/annotationtypes")
            .request(MediaType.APPLICATION_JSON)
            .header(Constants.USER_ID_HEADER, userId)
            .post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        assertThat(Boolean.valueOf(getTypeActionResult(response))).isFalse();
    }

    @Test
    void singleTypeSucceeded_statusIsCreated() {
        List<ImmutablePair<AnnotationTypeDefinition, Boolean>> typeActionResults = new ArrayList<>();
        AnnotationTypeDefinition dummyDefition = new AnnotationTypeDefinition();
        typeActionResults.add(new ImmutablePair(dummyDefition, true));
        assertThat(TypesUploadEndpoint.getHttpStatus(typeActionResults).value()).isEqualTo(HttpStatus.CREATED_201);
    }

    @Test
    void singleTypeExists_statusIsConflict() {
        List<ImmutablePair<AnnotationTypeDefinition, Boolean>> typeActionResults = new ArrayList<>();
        AnnotationTypeDefinition dummyDefition = new AnnotationTypeDefinition();
        typeActionResults.add(new ImmutablePair(dummyDefition, null));
        assertThat(TypesUploadEndpoint.getHttpStatus(typeActionResults).value()).isEqualTo(HttpStatus.CONFLICT_409);
    }

    @Test
    void mixedSuccessAndExists_statusIsCreated() {
        List<ImmutablePair<AnnotationTypeDefinition, Boolean>> typeActionResults = new ArrayList<>();
        AnnotationTypeDefinition dummyDefition = new AnnotationTypeDefinition();
        typeActionResults.add(new ImmutablePair(dummyDefition, true));
        typeActionResults.add(new ImmutablePair(dummyDefition, null));
        assertThat(TypesUploadEndpoint.getHttpStatus(typeActionResults).value()).isEqualTo(HttpStatus.CREATED_201);
    }

    @Test
    void mixedSuccessAndFailure_statusIsBadRequest() {
        List<ImmutablePair<AnnotationTypeDefinition, Boolean>> typeActionResults = new ArrayList<>();
        AnnotationTypeDefinition dummyDefition = new AnnotationTypeDefinition();
        typeActionResults.add(new ImmutablePair(dummyDefition, true));
        typeActionResults.add(new ImmutablePair(dummyDefition, false));
        typeActionResults.add(new ImmutablePair(dummyDefition, null));
        assertThat(TypesUploadEndpoint.getHttpStatus(typeActionResults).value()).isEqualTo(HttpStatus.BAD_REQUEST_400);
    }

    @org.springframework.context.annotation.Configuration
    @Import(BaseTestConfig.class)
    static class TypesUploadTestConfig {

        @Bean
        TypesUploadEndpoint typesUploadEndpoint() {
            UserBusinessLogic userBusinessLogic = mock(UserBusinessLogic.class);
            ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
            return new TypesUploadEndpoint(componentsUtils, commonImportManager(),
                annotationTypeOperations(), accessValidations);
        }

        @Bean
        CommonImportManager commonImportManager() {
            return new CommonImportManager(componentUtils, propertyOperation, modelOperation);
        }

        @Bean
        AnnotationTypeOperations annotationTypeOperations() {
            return new AnnotationTypeOperations(commonTypeOperations());
        }

        @Bean
        CommonTypeOperations commonTypeOperations() {
            return new CommonTypeOperations(janusGraphGenericDao, propertyOperation, operationUtils);
        }
    }
}
