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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.CapabilityTypeImportManager;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.web.context.WebApplicationContext;

import fj.data.Either;

public class TypesUploadServletTest extends JerseyTest {

	public static final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
	public static final HttpSession session = Mockito.mock(HttpSession.class);
	public static final ServletContext servletContext = Mockito.mock(ServletContext.class);
	public static final WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
	public static final WebApplicationContext webApplicationContext = Mockito.mock(WebApplicationContext.class);
	public static final CapabilityTypeImportManager importManager = Mockito.mock(CapabilityTypeImportManager.class);
	public static final ServletUtils servletUtils = Mockito.mock(ServletUtils.class);
	public static final UserBusinessLogic userAdmin = Mockito.mock(UserBusinessLogic.class);
	public static final ComponentsUtils componentUtils = Mockito.mock(ComponentsUtils.class);
	public static final ResponseFormat responseFormat = Mockito.mock(ResponseFormat.class);

	@BeforeClass
	public static void setup() {
		ExternalConfiguration.setAppName("catalog-be");
		when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR))
				.thenReturn(webAppContextWrapper);
		when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
		when(webApplicationContext.getBean(CapabilityTypeImportManager.class)).thenReturn(importManager);
		when(webApplicationContext.getBean(ServletUtils.class)).thenReturn(servletUtils);
		when(servletUtils.getComponentsUtils()).thenReturn(componentUtils);
		when(servletUtils.getUserAdmin()).thenReturn(userAdmin);
		String userId = "jh0003";
		User user = new User();
		user.setUserId(userId);
		user.setRole(Role.ADMIN.name());
		Either<User, ActionStatus> eitherUser = Either.left(user);
		when(userAdmin.getUser(userId, false)).thenReturn(eitherUser);
		when(request.getHeader(Constants.USER_ID_HEADER)).thenReturn(userId);
		when(responseFormat.getStatus()).thenReturn(HttpStatus.CREATED_201.getStatusCode());
		when(componentUtils.getResponseFormat(ActionStatus.CREATED)).thenReturn(responseFormat);

	}

	@Test
	public void creatingCapabilityTypeSuccessTest() {
		List<CapabilityTypeDefinition> emptyList = new ArrayList<CapabilityTypeDefinition>();
		Either<List<CapabilityTypeDefinition>, ResponseFormat> either = Either.left(emptyList);
		when(importManager.createCapabilityTypes(Mockito.anyString())).thenReturn(either);
		FileDataBodyPart filePart = new FileDataBodyPart("capabilityTypeZip",
				new File("src/test/resources/types/capabilityTypes.zip"));
		MultiPart multipartEntity = new FormDataMultiPart();
		multipartEntity.bodyPart(filePart);

		Response response = target().path("/v1/catalog/uploadType/capability").request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(multipartEntity, MediaType.MULTIPART_FORM_DATA), Response.class);

		assertTrue(response.getStatus() == HttpStatus.CREATED_201.getStatusCode());

	}

	@Override
	protected void configureClient(ClientConfig config) {
		config.register(MultiPartFeature.class);
	}

	@Override
	protected Application configure() {
		ResourceConfig resourceConfig = new ResourceConfig(TypesUploadServlet.class);

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
				for (String mandatoryHeader : configurationManager.getConfiguration().getIdentificationHeaderFields()) {

					when(request.getHeader(mandatoryHeader)).thenReturn(mandatoryHeader);

				}

				when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR))
						.thenReturn(configurationManager);
			}
		});

		return resourceConfig;
	}

	private TypesUploadServlet createTestSubject() {
		return new TypesUploadServlet();
	}

	
	@Test
	public void testUploadCapabilityType() throws Exception {
		TypesUploadServlet testSubject;
		File file = null;
		HttpServletRequest request = null;
		String creator = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testUploadInterfaceLifecycleType() throws Exception {
		TypesUploadServlet testSubject;
		File file = null;
		HttpServletRequest request = null;
		String creator = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testUploadCategories() throws Exception {
		TypesUploadServlet testSubject;
		File file = null;
		HttpServletRequest request = null;
		String creator = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testUploadDataTypes() throws Exception {
		TypesUploadServlet testSubject;
		File file = null;
		HttpServletRequest request = null;
		String creator = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testUploadGroupTypes() throws Exception {
		TypesUploadServlet testSubject;
		File file = null;
		HttpServletRequest request = null;
		String creator = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testUploadPolicyTypes() throws Exception {
		TypesUploadServlet testSubject;
		File file = null;
		HttpServletRequest request = null;
		String creator = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	
}
