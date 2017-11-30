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

package org.openecomp.sdc.be.externalapi.servlet;


import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.be.components.impl.ElementBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.ecomp.converters.AssetMetadataConverter;
import org.openecomp.sdc.be.externalapi.servlet.representation.ResourceAssetMetadata;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.FunctionalInterfaces;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.web.context.WebApplicationContext;

import fj.data.Either;

public class AssetsDataServletTest extends JerseyTest {

	public static final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
	public static final HttpSession session = Mockito.mock(HttpSession.class);
	public static final ServletContext servletContext = Mockito.mock(ServletContext.class);
	public static final WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
	public static final WebApplicationContext webApplicationContext = Mockito.mock(WebApplicationContext.class);
	public static final ResponseFormat responseFormat = Mockito.mock(ResponseFormat.class);
	public static final ServletUtils servletUtils = Mockito.mock(ServletUtils.class);
	public static final ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);
	public static final ResourceImportManager resourceImportManager = Mockito.mock(ResourceImportManager.class);
	public static final ResourceBusinessLogic resourceBusinessLogic = Mockito.mock(ResourceBusinessLogic.class);
	public static final ElementBusinessLogic elementBusinessLogic = Mockito.mock(ElementBusinessLogic.class);
	public static final Resource resource = Mockito.mock(Resource.class);
	public static final CategoryDefinition categoryDefinition = Mockito.mock(CategoryDefinition.class);
	public static final SubCategoryDefinition subCategoryDefinition = Mockito.mock(SubCategoryDefinition.class);
	public static final AssetMetadataConverter assetMetadataConverter = Mockito.mock(AssetMetadataConverter.class);
//	public static final ResourceAssetMetadata resourceAssetMetadata = Mockito.mock(ResourceAssetMetadata.class);
	public static final ResourceAssetMetadata resourceAssetMetadata = new ResourceAssetMetadata();;
	
	
	
	

	@BeforeClass
	public static void setup() {
		ExternalConfiguration.setAppName("catalog-be");
		when(request.getSession()).thenReturn(session);
		when(request.getHeader(Constants.X_ECOMP_INSTANCE_ID_HEADER)).thenReturn("mockXEcompInstanceId");
		when(request.getHeader(Constants.USER_ID_HEADER)).thenReturn("mockAttID");
		when(request.getRequestURL()).thenReturn(new StringBuffer("sdc/v1/catalog/resources"));
		
		when(session.getServletContext()).thenReturn(servletContext);
		when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
		when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
		
		when(webApplicationContext.getBean(ServletUtils.class)).thenReturn(servletUtils);
		when(webApplicationContext.getBean(ResourceBusinessLogic.class)).thenReturn(resourceBusinessLogic);
		
		when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
		mockResponseFormat();

		when(resource.getName()).thenReturn("MockVFCMT");
		when(resource.getSystemName()).thenReturn("mockvfcmt");
		Either<Resource, ResponseFormat>  eitherRet = Either.left(resource);
		when(componentsUtils.convertJsonToObjectUsingObjectMapper(Mockito.any(), Mockito.any(), Mockito.eq(Resource.class), Mockito.any(), Mockito.eq(ComponentTypeEnum.RESOURCE))).thenReturn(eitherRet);
	
		when(webApplicationContext.getBean(ResourceImportManager.class)).thenReturn(resourceImportManager);
		when(webApplicationContext.getBean(ElementBusinessLogic.class)).thenReturn(elementBusinessLogic);
		when(categoryDefinition.getName()).thenReturn("Template");
		when(subCategoryDefinition.getName()).thenReturn("Monitoring Template");
		when(categoryDefinition.getSubcategories()).thenReturn(Arrays.asList(subCategoryDefinition));
		when(elementBusinessLogic.getAllResourceCategories()).thenReturn(Either.left(Arrays.asList(categoryDefinition)));
		when(resourceBusinessLogic.createResource(Mockito.eq(resource), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(Either.left(resource));
		when(webApplicationContext.getBean(AssetMetadataConverter.class)).thenReturn(assetMetadataConverter);
		
		Mockito.doReturn(Either.left(resourceAssetMetadata)).when(assetMetadataConverter).convertToSingleAssetMetadata(Mockito.eq(resource), Mockito.anyString(),
				Mockito.eq(true));

		
		
	}



	private static void mockResponseFormat() {
		when(componentsUtils.getResponseFormat(Mockito.any(ActionStatus.class), Mockito.any(String[].class))).thenAnswer((Answer<ResponseFormat>) invocation -> {
            ResponseFormat ret;
            final ActionStatus actionStatus = invocation.getArgument(0);
            switch( actionStatus ){
            case CREATED :{
                ret = new ResponseFormat(HttpStatus.SC_CREATED);
                break;
            }
            default :{
                ret = new ResponseFormat(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                break;
            }
            }
            return ret;
        });
	}

	

	@Test
	public void createVfcmtHappyScenario() {
		final JSONObject createRequest = buildCreateJsonRequest();
		Response response = target().path("/v1/catalog/resources").request(MediaType.APPLICATION_JSON).header(Constants.X_ECOMP_INSTANCE_ID_HEADER, "mockXEcompInstanceId").header(Constants.USER_ID_HEADER, "mockAttID")
				.post(Entity.json(createRequest.toJSONString()), Response.class);
		assertTrue(response.getStatus() == HttpStatus.SC_CREATED);

	}
	private static final String BASIC_CREATE_REQUEST = "{\r\n" + 
			"  \"name\": \"VFCMT_1\",\r\n" + 
			"  \"description\": \"VFCMT Description\",\r\n" + 
			"  \"resourceType\" : \"VFCMT\",\r\n" + 
			"  \"category\": \"Template\",\r\n" + 
			"  \"subcategory\": \"Monitoring Template\",\r\n" + 
			"  \"vendorName\" : \"DCAE\",\r\n" + 
			"  \"vendorRelease\" : \"1.0\",\r\n" + 
			"  \"tags\": [\r\n" + 
			"    \"VFCMT_1\"\r\n" + 
			"  ],\r\n" + 
			"  \"icon\" : \"defaulticon\",\r\n" + 
			"  \"contactId\": \"cs0008\"\r\n" + 
			"}";
	private JSONObject buildCreateJsonRequest() {
		
		JSONParser parser = new JSONParser();
		JSONObject jsonObj = (JSONObject) FunctionalInterfaces.swallowException( () -> parser.parse(BASIC_CREATE_REQUEST));
		return jsonObj;

	}
	@Override
	protected Application configure() {

		ResourceConfig resourceConfig = new ResourceConfig(CrudExternalServlet.class);
		forceSet(TestProperties.CONTAINER_PORT, "0");
		resourceConfig.register(new AbstractBinder() {

			@Override
			protected void configure() {
				bind(request).to(HttpServletRequest.class);
			}
		});

		return resourceConfig;
	}
}
