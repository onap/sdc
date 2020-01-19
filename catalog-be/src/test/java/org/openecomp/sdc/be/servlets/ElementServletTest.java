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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.be.servlets;

import fj.data.Either;
import org.apache.commons.text.StrSubstitutor;
import org.apache.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ElementBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.components.impl.exceptions.ByResponseFormatComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.components.scheduledtasks.ComponentsCleanBusinessLogic;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.config.SpringConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.ArtifactType;
import org.openecomp.sdc.be.model.PropertyScope;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Tag;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.catalog.CatalogComponent;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.GroupingDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.ui.model.UiCategories;
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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class ElementServletTest extends JerseyTest {
	public static final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
	public static final HttpSession session = Mockito.mock(HttpSession.class);
	public static final ResourceImportManager resourceImportManager = Mockito.mock(ResourceImportManager.class);
	public static final ResourceBusinessLogic resourceBusinessLogic = Mockito.mock(ResourceBusinessLogic.class);
	public static final BeGenericServlet beGenericServlet =  Mockito.mock(BeGenericServlet.class);
	public static final Resource resource = Mockito.mock(Resource.class);
	public static final UserBusinessLogic userBusinessLogic = Mockito.mock(UserBusinessLogic.class);
	public static final ComponentInstanceBusinessLogic componentInstanceBusinessLogic = Mockito.mock(ComponentInstanceBusinessLogic.class);
	public static final ArtifactsBusinessLogic artifactsBusinessLogic = Mockito.mock(ArtifactsBusinessLogic.class);

	private static final ServletContext servletContext = Mockito.mock(ServletContext.class);
	public static final WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
	private static final WebApplicationContext webApplicationContext = Mockito.mock(WebApplicationContext.class);
	private static final ServletUtils servletUtils = Mockito.mock(ServletUtils.class);
	private static final UserBusinessLogic userAdmin = Mockito.mock(UserBusinessLogic.class);
	private static final ComponentsUtils componentUtils = Mockito.mock(ComponentsUtils.class);
	private static final ComponentsCleanBusinessLogic componentsCleanBusinessLogic = Mockito.mock(ComponentsCleanBusinessLogic.class);
	private static final ElementBusinessLogic elementBusinessLogic = Mockito.mock(ElementBusinessLogic.class);

	private static final ResponseFormat okResponseFormat = new ResponseFormat(HttpStatus.SC_OK);
	private static final ResponseFormat conflictResponseFormat = new ResponseFormat(HttpStatus.SC_CONFLICT);
	private static final ResponseFormat generalErrorResponseFormat = new ResponseFormat(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	private static final ResponseFormat createdResponseFormat = new ResponseFormat(HttpStatus.SC_CREATED);
	private static final ResponseFormat noContentResponseFormat = new ResponseFormat(HttpStatus.SC_NO_CONTENT);
	private static final ResponseFormat unauthorizedResponseFormat = Mockito.mock(ResponseFormat.class);
	private static final ResponseFormat notFoundResponseFormat = Mockito.mock(ResponseFormat.class);
	private static final ResponseFormat badRequestResponseFormat = Mockito.mock(ResponseFormat.class);
	private static final String EMPTY_JSON = "{}";
	private static final String COMPONENT_TYPE = "componentType";
	private static final String CATEGORY_UNIQUE_ID = "categoryUniqueId";
	private static final String CATEGORY_ID = "categoryId";
	private static final String SUB_CATEGORY_UNIQUE_ID = "subCategoryUniqueId";
	private static final String SUB_CATEGORY_ID = "subCategoryId";
	private static final String GROUPING_UNIQUE_ID = "groupingUniqueId";

	/* Users */
	private static User designerUser = new User("designer", "designer", "designer", "designer@email.com", Role.DESIGNER.name(), System
			.currentTimeMillis());

	private static ConfigurationManager configurationManager;

	@BeforeClass
	public static void setup() {

		//Needed for User Authorization
		when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
		when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
		when(webApplicationContext.getBean(ServletUtils.class)).thenReturn(servletUtils);
		when(servletUtils.getUserAdmin()).thenReturn(userAdmin);
		when(servletUtils.getComponentsUtils()).thenReturn(componentUtils);
		when(componentUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION)).thenReturn(unauthorizedResponseFormat);
		when(unauthorizedResponseFormat.getStatus()).thenReturn(HttpStatus.SC_UNAUTHORIZED);

		when(componentUtils.getResponseFormat(ActionStatus.OK)) .thenReturn(okResponseFormat);
		when(componentUtils.getResponseFormat(ActionStatus.CREATED)).thenReturn(createdResponseFormat);
		when(componentUtils.getResponseFormat(ActionStatus.NO_CONTENT)).thenReturn(noContentResponseFormat);
		when(componentUtils.getResponseFormat(ActionStatus.INVALID_CONTENT)).thenReturn(badRequestResponseFormat);
		when(componentUtils.getResponseFormat(ActionStatus.GENERAL_ERROR)) .thenReturn(generalErrorResponseFormat);
		when(componentUtils.getResponseFormat(any(ComponentException.class)))
				.thenReturn(generalErrorResponseFormat);

		ByResponseFormatComponentException ce = Mockito.mock(ByResponseFormatComponentException.class);
		when(ce.getResponseFormat()).thenReturn(unauthorizedResponseFormat);

		//Needed for error configuration
		when(notFoundResponseFormat.getStatus()).thenReturn(HttpStatus.SC_NOT_FOUND);
		when(badRequestResponseFormat.getStatus()).thenReturn(HttpStatus.SC_BAD_REQUEST);
		when(componentUtils.getResponseFormat(eq(ActionStatus.RESOURCE_NOT_FOUND), any())).thenReturn(notFoundResponseFormat);
		when(componentUtils.getResponseFormat(eq(ActionStatus.COMPONENT_VERSION_NOT_FOUND), any())).thenReturn(notFoundResponseFormat);
		when(componentUtils.getResponseFormat(eq(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND), any())).thenReturn(notFoundResponseFormat);
		when(componentUtils.getResponseFormat(eq(ActionStatus.EXT_REF_NOT_FOUND), any())).thenReturn(notFoundResponseFormat);
		when(componentUtils.getResponseFormat(eq(ActionStatus.MISSING_X_ECOMP_INSTANCE_ID), any())).thenReturn(badRequestResponseFormat);
		when(request.getSession()).thenReturn(session);
		when(session.getServletContext()).thenReturn(servletContext);
		when(beGenericServlet.getElementBL(any())).thenReturn(elementBusinessLogic);
		when(webApplicationContext.getBean(ElementBusinessLogic.class)).thenReturn(elementBusinessLogic);
		when(webApplicationContext.getBean(ComponentsUtils.class)).thenReturn(componentUtils);
		when(beGenericServlet.getComponentsUtils()).thenReturn(componentUtils);

		Either<User, ActionStatus> designerEither = Either.left(designerUser);

		when(userAdmin.getUser(designerUser.getUserId(), false)).thenReturn(designerUser);

		String appConfigDir = "src/test/resources/config";
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
		configurationManager = new ConfigurationManager(configurationSource);

		org.openecomp.sdc.be.config.Configuration configuration = new org.openecomp.sdc.be.config.Configuration();
		configuration.setJanusGraphInMemoryGraph(true);
		Configuration.HeatDeploymentArtifactTimeout testHeatDeploymentArtifactTimeout = new Configuration.HeatDeploymentArtifactTimeout();
		testHeatDeploymentArtifactTimeout.setDefaultMinutes(1);
		configuration.setHeatArtifactDeploymentTimeout(testHeatDeploymentArtifactTimeout);

		configurationManager.setConfiguration(configuration);
		ExternalConfiguration.setAppName("catalog-be");


	}

	@Before
	public void resetSomeMocks() {
		reset(elementBusinessLogic);
	}

	@Test
	public void getComponentCategoriesNoCategoryFoundTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);

		String formatEndpoint = "/v1/categories/{componentType}";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

		ResponseFormat notFoundResponseFormat = new ResponseFormat(HttpStatus.SC_NOT_FOUND);
		Either<List<CategoryDefinition>, ResponseFormat> getAllCategoriesEither = Either.right(notFoundResponseFormat);

		when(elementBusinessLogic.getAllCategories(componentType, designerUser.getUserId()))
				.thenReturn(getAllCategoriesEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.get();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_NOT_FOUND);
	}

	@Test
	public void getComponentCategoriesExceptionDuringProcessingTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);

		String formatEndpoint = "/v1/categories/{componentType}";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

		when(elementBusinessLogic.getAllCategories(componentType, designerUser.getUserId()))
				.thenThrow(new RuntimeException("Test exception: getComponentCategories"));

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.get();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void getComponentCategoriesTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);

		String formatEndpoint = "/v1/categories/{componentType}";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

		Either<List<CategoryDefinition>, ResponseFormat> getAllCategoriesEither = Either.left(new ArrayList<>());

		when(elementBusinessLogic.getAllCategories(componentType, designerUser.getUserId()))
				.thenReturn(getAllCategoriesEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.get();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
	}

	@Test
	public void getAllCategoriesNoCategoryFoundTest() {
		String path = "/v1/categories";

		ResponseFormat notFoundResponseFormat = new ResponseFormat(HttpStatus.SC_NOT_FOUND);
		Either<UiCategories, ResponseFormat> getAllCategoriesEither = Either.right(notFoundResponseFormat);

		when(elementBusinessLogic.getAllCategories(designerUser.getUserId()))
				.thenReturn(getAllCategoriesEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.get();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_NOT_FOUND);
	}

	@Test
	public void getAllCategoriesExceptionDuringProcessingTest() {
		String path = "/v1/setup/ui";
		when(elementBusinessLogic.getAllCategories(designerUser.getUserId()))
				.thenThrow(new RuntimeException("Test exception: getAllCategories"));

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.get();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void getAllCategoriesTest() {
		String path = "/v1/setup/ui";
		Either<UiCategories, ResponseFormat> getAllCategoriesEither = Either.left(new UiCategories());
		Either<List<ArtifactType>, ActionStatus> otherEither = Either.left(new ArrayList<>());
		when(elementBusinessLogic.getDefaultHeatTimeout()).thenReturn(Either.left(configurationManager.getConfiguration().getHeatArtifactDeploymentTimeout()));
		when(elementBusinessLogic.getAllDeploymentArtifactTypes()).thenReturn(Either.left(new HashMap<String, Object>()));
		when(elementBusinessLogic.getResourceTypesMap()).thenReturn(Either.left(new HashMap<String, String>()));
		when(elementBusinessLogic.getAllArtifactTypes(designerUser.getUserId()))
				.thenReturn(otherEither);



		when(elementBusinessLogic.getAllCategories(designerUser.getUserId()))
				.thenReturn(getAllCategoriesEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.get();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
	}

	@Test
	public void createComponentCategoryCreationFailedTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);

		String formatEndpoint = "/v1/category/{componentType}";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

		Either<CategoryDefinition, ResponseFormat> createComponentCategoryEither = Either.right(conflictResponseFormat);

		when(elementBusinessLogic.createCategory(any(), eq(componentType), eq(designerUser.getUserId())))
				.thenReturn(createComponentCategoryEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.post(Entity.json(EMPTY_JSON));

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CONFLICT);
	}

	@Test
	public void createComponentCategoryExceptionDuringCreationTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);

		String formatEndpoint = "/v1/category/{componentType}";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

		when(elementBusinessLogic.createCategory(any(), eq(componentType), eq(designerUser.getUserId())))
				.thenThrow(new RuntimeException("Test exception: createComponentCategory"));

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.post(Entity.json(EMPTY_JSON));

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void createComponentCategoryTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);

		String formatEndpoint = "/v1/category/{componentType}";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

		Either<CategoryDefinition, ResponseFormat> createComponentCategoryEither = Either.left(new CategoryDefinition());

		when(elementBusinessLogic.createCategory(any(), eq(componentType), eq(designerUser.getUserId())))
				.thenReturn(createComponentCategoryEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.post(Entity.json(EMPTY_JSON));

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CREATED);
	}

	@Test
	public void deleteComponentCategoryNoCategoryFoundTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		String categoryUniqueId = "categoryUniqueId";
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);
		parametersMap.put(CATEGORY_UNIQUE_ID, categoryUniqueId);

		String formatEndpoint = "/v1/category/{componentType}/{categoryUniqueId}";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

		ResponseFormat notFoundResponseFormat = new ResponseFormat(HttpStatus.SC_NOT_FOUND);
		Either<CategoryDefinition, ResponseFormat> deleteComponentCategoryEither = Either.right(notFoundResponseFormat);

		when(elementBusinessLogic.deleteCategory(categoryUniqueId, componentType, designerUser.getUserId()))
				.thenReturn(deleteComponentCategoryEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.delete();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_NOT_FOUND);
	}

	@Test
	public void deleteComponentCategoryExceptionDuringProcessingTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		String categoryUniqueId = "categoryUniqueId";
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);
		parametersMap.put(CATEGORY_UNIQUE_ID, categoryUniqueId);

		String formatEndpoint = "/v1/category/{componentType}/{categoryUniqueId}";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

		when(elementBusinessLogic.deleteCategory(categoryUniqueId, componentType, designerUser.getUserId()))
				.thenThrow(new RuntimeException("Test exception: deleteComponentCategory"));

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.delete();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void deleteComponentCategoryTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		String categoryUniqueId = "categoryUniqueId";
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);
		parametersMap.put(CATEGORY_UNIQUE_ID, categoryUniqueId);

		String formatEndpoint = "/v1/category/{componentType}/{categoryUniqueId}";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

		Either<CategoryDefinition, ResponseFormat> deleteComponentCategoryEither = Either.left(new CategoryDefinition());
		when(elementBusinessLogic.deleteCategory(categoryUniqueId, componentType, designerUser.getUserId()))
				.thenReturn(deleteComponentCategoryEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.delete();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
	}

	@Test
	public void createComponentSubCategoryCreationFailedTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		String categoryId = "categoryId";
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);
		parametersMap.put(CATEGORY_ID, categoryId);

		String formatEndpoint = "/v1/category/{componentType}/{categoryId}/subCategory";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

		Either<SubCategoryDefinition, ResponseFormat> createComponentSubCategoryEither = Either.right(conflictResponseFormat);

		when(elementBusinessLogic.createSubCategory(any(), eq(componentType), eq(categoryId), eq(designerUser.getUserId())))
				.thenReturn(createComponentSubCategoryEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.post(Entity.json(EMPTY_JSON));

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CONFLICT);
	}

	@Test
	public void createComponentSubCategoryExceptionDuringCreationTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		String categoryId = "categoryId";
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);
		parametersMap.put(CATEGORY_ID, categoryId);

		String formatEndpoint = "/v1/category/{componentType}/{categoryId}/subCategory";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

		when(elementBusinessLogic.createSubCategory(any(), eq(componentType), eq(categoryId), eq(designerUser.getUserId())))
				.thenThrow(new RuntimeException("Test exception: createComponentSubCategory"));

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.post(Entity.json(EMPTY_JSON));

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void createComponentSubCategoryTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		String categoryId = "categoryId";
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);
		parametersMap.put(CATEGORY_ID, categoryId);

		String formatEndpoint = "/v1/category/{componentType}/{categoryId}/subCategory";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

		Either<SubCategoryDefinition, ResponseFormat> createComponentSubCategoryEither = Either.left(new SubCategoryDefinition());

		when(elementBusinessLogic.createSubCategory(any(), eq(componentType), eq(categoryId), eq(designerUser.getUserId())))
				.thenReturn(createComponentSubCategoryEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.post(Entity.json(EMPTY_JSON));

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CREATED);
	}

	@Test
	public void deleteComponentSubCategoryCreationFailedTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		String categoryUniqueId = "categoryUniqueId";
		String subCategoryUniqueId = "subCategoryUniqueId";
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);
		parametersMap.put(CATEGORY_UNIQUE_ID, categoryUniqueId);
		parametersMap.put(SUB_CATEGORY_UNIQUE_ID, subCategoryUniqueId);

		String formatEndpoint = "/v1/category/{componentType}/{categoryUniqueId}/subCategory/{subCategoryUniqueId}";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

		Either<SubCategoryDefinition, ResponseFormat> deleteComponentSubCategoryEither = Either.right(conflictResponseFormat);

		when(elementBusinessLogic.deleteSubCategory(eq(subCategoryUniqueId), eq(componentType), eq(designerUser.getUserId())))
				.thenReturn(deleteComponentSubCategoryEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.delete();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CONFLICT);
	}

	@Test
	public void deleteComponentSubCategoryExceptionDuringCreationTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		String categoryUniqueId = "categoryUniqueId";
		String subCategoryUniqueId = "subCategoryUniqueId";
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);
		parametersMap.put(CATEGORY_UNIQUE_ID, categoryUniqueId);
		parametersMap.put(SUB_CATEGORY_UNIQUE_ID, subCategoryUniqueId);

		String formatEndpoint = "/v1/category/{componentType}/{categoryUniqueId}/subCategory/{subCategoryUniqueId}";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

		when(elementBusinessLogic.deleteSubCategory(eq(subCategoryUniqueId), eq(componentType), eq(designerUser.getUserId())))
				.thenThrow(new RuntimeException("Test exception: deleteComponentSubCategory"));

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.delete();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void deleteComponentSubCategoryTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		String categoryUniqueId = "categoryUniqueId";
		String subCategoryUniqueId = "subCategoryUniqueId";
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);
		parametersMap.put(CATEGORY_UNIQUE_ID, categoryUniqueId);
		parametersMap.put(SUB_CATEGORY_UNIQUE_ID, subCategoryUniqueId);

		String formatEndpoint = "/v1/category/{componentType}/{categoryUniqueId}/subCategory/{subCategoryUniqueId}";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

		Either<SubCategoryDefinition, ResponseFormat> deleteComponentSubCategoryEither = Either.left(new SubCategoryDefinition());

		when(elementBusinessLogic.deleteSubCategory(eq(subCategoryUniqueId), eq(componentType), eq(designerUser.getUserId())))
				.thenReturn(deleteComponentSubCategoryEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.delete();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
	}

	@Test
	public void createComponentGroupingCreationFailedTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		String categoryId = "categoryUniqueId";
		String subCategoryId = "subCategoryId";
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);
		parametersMap.put(CATEGORY_ID, categoryId);
		parametersMap.put(SUB_CATEGORY_ID, subCategoryId);

		String formatEndpoint = "/v1/category/{componentType}/{categoryId}/subCategory/{subCategoryId}/grouping";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

		Either<GroupingDefinition, ResponseFormat> createComponentGroupingEither = Either.right(conflictResponseFormat);

		when(elementBusinessLogic.createGrouping(any(), eq(componentType), eq(categoryId), eq(subCategoryId), eq(designerUser.getUserId())))
				.thenReturn(createComponentGroupingEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.post(Entity.json(EMPTY_JSON));

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CONFLICT);
	}

	@Test
	public void createComponentGroupingExceptionDuringCreationTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		String categoryId = "categoryUniqueId";
		String subCategoryId = "subCategoryId";
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);
		parametersMap.put(CATEGORY_ID, categoryId);
		parametersMap.put(SUB_CATEGORY_ID, subCategoryId);

		String formatEndpoint = "/v1/category/{componentType}/{categoryId}/subCategory/{subCategoryId}/grouping";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");


		when(elementBusinessLogic.createGrouping(any(), eq(componentType), eq(categoryId), eq(subCategoryId), eq(designerUser.getUserId())))
				.thenThrow(new RuntimeException("Test exception: createComponentGrouping"));

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.post(Entity.json(EMPTY_JSON));

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void createComponentGroupingTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		String categoryId = "categoryUniqueId";
		String subCategoryId = "subCategoryId";
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);
		parametersMap.put(CATEGORY_ID, categoryId);
		parametersMap.put(SUB_CATEGORY_ID, subCategoryId);

		String formatEndpoint = "/v1/category/{componentType}/{categoryId}/subCategory/{subCategoryId}/grouping";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

		Either<GroupingDefinition, ResponseFormat> createComponentGroupingEither = Either.left(new GroupingDefinition());

		when(elementBusinessLogic.createGrouping(any(), eq(componentType), eq(categoryId), eq(subCategoryId), eq(designerUser.getUserId())))
				.thenReturn(createComponentGroupingEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.post(Entity.json(EMPTY_JSON));

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CREATED);
	}

	@Test
	public void deleteComponentGroupingCreationFailedTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		String categoryUniqueId = "categoryUniqueId";
		String subCategoryUniqueId = "subCategoryUniqueId";
		String groupingUniqueId = "groupingUniqueId";
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);
		parametersMap.put(CATEGORY_UNIQUE_ID, categoryUniqueId);
		parametersMap.put(SUB_CATEGORY_UNIQUE_ID, subCategoryUniqueId);
		parametersMap.put(GROUPING_UNIQUE_ID, groupingUniqueId);

		String formatEndpoint = "/v1/category/{componentType}/{categoryUniqueId}/subCategory/{subCategoryUniqueId}/grouping/{groupingUniqueId}";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

		Either<GroupingDefinition, ResponseFormat> deleteComponentGroupingEither = Either.right(conflictResponseFormat);

		when(elementBusinessLogic.deleteGrouping(eq(groupingUniqueId), eq(componentType), eq(designerUser.getUserId())))
				.thenReturn(deleteComponentGroupingEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.delete();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_CONFLICT);
	}

	@Test
	public void deleteComponentGroupingExceptionDuringCreationTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		String categoryUniqueId = "categoryUniqueId";
		String subCategoryUniqueId = "subCategoryUniqueId";
		String groupingUniqueId = "groupingUniqueId";
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);
		parametersMap.put(CATEGORY_UNIQUE_ID, categoryUniqueId);
		parametersMap.put(SUB_CATEGORY_UNIQUE_ID, subCategoryUniqueId);
		parametersMap.put(GROUPING_UNIQUE_ID, groupingUniqueId);

		String formatEndpoint = "/v1/category/{componentType}/{categoryUniqueId}/subCategory/{subCategoryUniqueId}/grouping/{groupingUniqueId}";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

		when(elementBusinessLogic.deleteGrouping(eq(groupingUniqueId), eq(componentType), eq(designerUser.getUserId())))
				.thenThrow(new RuntimeException("Test exception: deleteComponentGrouping"));

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.delete();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void deleteComponentGroupingTest() {
		String componentType = ComponentTypeEnum.SERVICE_PARAM_NAME;
		String categoryUniqueId = "categoryUniqueId";
		String subCategoryUniqueId = "subCategoryUniqueId";
		String groupingUniqueId = "groupingUniqueId";
		Map<String,String> parametersMap = new HashMap<>();
		parametersMap.put(COMPONENT_TYPE, componentType);
		parametersMap.put(CATEGORY_UNIQUE_ID, categoryUniqueId);
		parametersMap.put(SUB_CATEGORY_UNIQUE_ID, subCategoryUniqueId);
		parametersMap.put(GROUPING_UNIQUE_ID, groupingUniqueId);

		String formatEndpoint = "/v1/category/{componentType}/{categoryUniqueId}/subCategory/{subCategoryUniqueId}/grouping/{groupingUniqueId}";
		String path = StrSubstitutor.replace(formatEndpoint, parametersMap, "{","}");

		Either<GroupingDefinition, ResponseFormat> deleteComponentGroupingEither = Either.left(new GroupingDefinition());

		when(elementBusinessLogic.deleteGrouping(eq(groupingUniqueId), eq(componentType), eq(designerUser.getUserId())))
				.thenReturn(deleteComponentGroupingEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.delete();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
	}

	@Test
	public void tagsNoTagsFoundTest() {
		String path = "/v1/tags";
		Either<List<Tag>, ActionStatus> tagsEither = Either.right(ActionStatus.NO_CONTENT);

		when(elementBusinessLogic.getAllTags(designerUser.getUserId()))
				.thenReturn(tagsEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.get();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
	}

	@Test
	public void tagsExceptionDuringProcessingTest() {
		String path = "/v1/tags";
		when(elementBusinessLogic.getAllTags(designerUser.getUserId()))
				.thenThrow(new RuntimeException("Test exception: tags"));

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.get();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void tagsTest() {
		String path = "/v1/tags";
		Either<List<Tag>, ActionStatus> tagsEither = Either.left(new ArrayList<>());
		when(elementBusinessLogic.getAllTags(designerUser.getUserId()))
				.thenReturn(tagsEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.get();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
	}

	@Test
	public void propertyScopesNoPropertyScopesFoundTest() {
		String path = "/v1/propertyScopes";
		Either<List<PropertyScope>, ActionStatus> propertyScopesEither = Either.right(ActionStatus.NO_CONTENT);

		when(elementBusinessLogic.getAllPropertyScopes(designerUser.getUserId()))
				.thenReturn(propertyScopesEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.get();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
	}

	@Test
	public void propertyScopesExceptionDuringProcessingTest() {
		String path = "/v1/propertyScopes";
		when(elementBusinessLogic.getAllPropertyScopes(designerUser.getUserId()))
				.thenThrow(new RuntimeException("Test exception: propertyScopes"));

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.get();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void propertyScopesTest() {
		String path = "/v1/propertyScopes";
		Either<List<PropertyScope>, ActionStatus> propertyScopesEither = Either.left(new ArrayList<>());
		when(elementBusinessLogic.getAllPropertyScopes(designerUser.getUserId()))
				.thenReturn(propertyScopesEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.get();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
	}

	@Test
	public void artifactTypesNoartifactTypesFoundTest() {
		String path = "/v1/artifactTypes";
		Either<List<ArtifactType>, ActionStatus> artifactTypesEither = Either.right(ActionStatus.NO_CONTENT);

		when(elementBusinessLogic.getAllArtifactTypes(designerUser.getUserId()))
				.thenReturn(artifactTypesEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.get();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_NO_CONTENT);
	}

	@Test
	public void artifactTypesExceptionDuringProcessingTest() {
		String path = "/v1/artifactTypes";
		when(elementBusinessLogic.getAllArtifactTypes(designerUser.getUserId()))
				.thenThrow(new RuntimeException("Test exception: artifactTypes"));

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.get();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void artifactTypesTest() {
		String path = "/v1/artifactTypes";
		Either<List<ArtifactType>, ActionStatus> artifactTypesEither = Either.left(new ArrayList<>());
		when(elementBusinessLogic.getAllArtifactTypes(designerUser.getUserId()))
				.thenReturn(artifactTypesEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.get();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
	}

	@Test
	public void configurationNoConfigurationFoundTest() {
		String path = "/v1/setup/ui";

		Either<List<ArtifactType>, ActionStatus> otherEither = Either.left(new ArrayList<>());
		Configuration.HeatDeploymentArtifactTimeout heatDeploymentArtifactTimeout = new Configuration.HeatDeploymentArtifactTimeout();
		heatDeploymentArtifactTimeout.setDefaultMinutes(1);
		Either<Configuration.HeatDeploymentArtifactTimeout, ActionStatus> defaultHeatTimeoutEither = Either.left(heatDeploymentArtifactTimeout);
		Either<Map<String, Object>, ActionStatus> deploymentEither = Either.left(new HashMap<>());
		Either<Map<String, String>, ActionStatus> resourceTypesMapEither = Either.left(new HashMap<>());

		when(elementBusinessLogic.getAllArtifactTypes(designerUser.getUserId()))
				.thenReturn(otherEither);
		when(elementBusinessLogic.getDefaultHeatTimeout())
				.thenReturn(defaultHeatTimeoutEither);
		when(elementBusinessLogic.getAllDeploymentArtifactTypes())
				.thenReturn(deploymentEither);
		when(elementBusinessLogic.getResourceTypesMap())
				.thenReturn(resourceTypesMapEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.get();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void configurationExceptionDuringProcessingTest() {
		String path = "/v1/setup/ui";
		when(elementBusinessLogic.getAllArtifactTypes(designerUser.getUserId()))
				.thenThrow(new RuntimeException("Test exception: artifactTypes"));

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.get();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void screenNoCatalogComponentsFoundTest() {
		String path = "/v1/screen";

		Either<Map<String, List<CatalogComponent>>, ResponseFormat> screenEither = Either.right(badRequestResponseFormat);
		when(elementBusinessLogic.getCatalogComponents(eq(designerUser.getUserId()), any()))
				.thenReturn(screenEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.get();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
	}

	@Test
	public void screenExceptionDuringProcessingTest() {
		String path = "/v1/screen";

		when(elementBusinessLogic.getCatalogComponents(eq(designerUser.getUserId()), any()))
				.thenThrow(new RuntimeException("Test exception: screen"));

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.get();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	@Test
	public void screenTest() {
		String path = "/v1/screen";

		Either<Map<String, List<CatalogComponent>>, ResponseFormat> screenEither = Either.left(new HashMap<>());
		when(elementBusinessLogic.getCatalogComponents(eq(designerUser.getUserId()), any()))
				.thenReturn(screenEither);

		Response response = target()
				.path(path)
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.header(Constants.USER_ID_HEADER, designerUser.getUserId())
				.get();

		assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_OK);
	}
	
	@Override
	protected Application configure() {
		ApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class);
		return new ResourceConfig(ElementServlet.class)
				.register(new AbstractBinder() {

					@Override
					protected void configure() {
						bind(request).to(HttpServletRequest.class);
						bind(userBusinessLogic).to(UserBusinessLogic.class);
						bind(componentUtils).to(ComponentsUtils.class);
						bind(componentsCleanBusinessLogic).to(ComponentsCleanBusinessLogic.class);
						bind(elementBusinessLogic).to(ElementBusinessLogic.class);
					}
				})
				.property("contextConfig", context);
	}
}