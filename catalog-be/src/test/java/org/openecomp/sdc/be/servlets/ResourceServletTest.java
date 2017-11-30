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

import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fj.data.Either;

public class ResourceServletTest extends JerseyTest {
	public static final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
	public static final ResourceImportManager resourceImportManager = Mockito.mock(ResourceImportManager.class);
	final static HttpSession session = Mockito.mock(HttpSession.class);
	final static ServletContext servletContext = Mockito.mock(ServletContext.class);
	final static WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
	final static WebApplicationContext webApplicationContext = Mockito.mock(WebApplicationContext.class);
	public static final ServletUtils servletUtils = Mockito.mock(ServletUtils.class);
	public static final ComponentsUtils componentUtils = Mockito.mock(ComponentsUtils.class);
	public static final UserBusinessLogic userAdmin = Mockito.mock(UserBusinessLogic.class);
	Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@BeforeClass
	public static void setup() {
		ExternalConfiguration.setAppName("catalog-be");
		when(request.getSession()).thenReturn(session);
		when(session.getServletContext()).thenReturn(servletContext);
		when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
		when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
		when(webApplicationContext.getBean(ResourceImportManager.class)).thenReturn(resourceImportManager);
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

		ImmutablePair<Resource, ActionStatus> pair = new ImmutablePair<Resource, ActionStatus>(new Resource(), ActionStatus.OK);
		Either<ImmutablePair<Resource, ActionStatus>, ResponseFormat> ret = Either.left(pair);
		when(resourceImportManager.importUserDefinedResource(Mockito.anyString(), Mockito.any(UploadResourceInfo.class), Mockito.any(User.class), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(ret);

	}

	@Before
	public void beforeTest() {
		Mockito.reset(componentUtils);

		Mockito.doAnswer(new Answer<ResponseFormat>() {
			public ResponseFormat answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				ActionStatus action = (ActionStatus) args[0];
				ResponseFormat resp = (action == ActionStatus.OK) ? new ResponseFormat(HttpStatus.CREATED.value()) : new ResponseFormat(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return resp;
			}
		}).when(componentUtils).getResponseFormat(Mockito.any(ActionStatus.class));

	}

	@Test
	public void testHappyScenarioTest() {
		UploadResourceInfo validJson = buildValidJson();
		setMD5OnRequest(true, validJson);
		Response response = target().path("/v1/catalog/resources").request(MediaType.APPLICATION_JSON).post(Entity.json(gson.toJson(validJson)), Response.class);
		Mockito.verify(componentUtils, Mockito.times(1)).getResponseFormat(Mockito.any(ActionStatus.class));
		Mockito.verify(componentUtils, Mockito.times(1)).getResponseFormat(ActionStatus.OK);
		assertTrue(response.getStatus() == HttpStatus.CREATED.value());

	}

	@Test
	public void testNonValidMd5Fail() {
		UploadResourceInfo validJson = buildValidJson();

		setMD5OnRequest(false, validJson);

		Response response = target().path("/v1/catalog/resources").request(MediaType.APPLICATION_JSON).post(Entity.json(gson.toJson(validJson)), Response.class);
		Mockito.verify(componentUtils, Mockito.times(1)).getResponseFormat(Mockito.any(ActionStatus.class));
		Mockito.verify(componentUtils, Mockito.times(1)).getResponseFormat(ActionStatus.INVALID_RESOURCE_CHECKSUM);
		assertTrue(response.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR.value());

	}

	@Test
	public void testNonValidPayloadNameFail() {
		UploadResourceInfo mdJson = buildValidJson();
		mdJson.setPayloadName("myCompute.xml");

		runAndVerifyActionStatusError(mdJson, ActionStatus.INVALID_TOSCA_FILE_EXTENSION);

	}

	@Test
	public void testNullPayloadFail() {
		UploadResourceInfo mdJson = buildValidJson();
		mdJson.setPayloadData(null);
		runAndVerifyActionStatusError(mdJson, ActionStatus.INVALID_RESOURCE_PAYLOAD);

	}

	@Test
	public void testNonYmlPayloadFail() {
		UploadResourceInfo mdJson = buildValidJson();
		String payload = "{ json : { isNot : yaml } ";
		encodeAndSetPayload(mdJson, payload);
		runAndVerifyActionStatusError(mdJson, ActionStatus.INVALID_YAML_FILE);

	}

	@Test
	public void testNonToscaPayloadFail() {
		UploadResourceInfo mdJson = buildValidJson();

		String payload = "node_types: \r\n" + "  org.openecomp.resource.importResource4test:\r\n" + "    derived_from: tosca.nodes.Root\r\n" + "    description: update update";
		encodeAndSetPayload(mdJson, payload);
		runAndVerifyActionStatusError(mdJson, ActionStatus.INVALID_TOSCA_TEMPLATE);

	}

	@Test
	public void testServiceToscaPayloadFail() {
		UploadResourceInfo mdJson = buildValidJson();

		String payload = "tosca_definitions_version: tosca_simple_yaml_1_0_0\r\n" + "node_types: \r\n" + "  org.openecomp.resource.importResource4test:\r\n" + "    derived_from: tosca.nodes.Root\r\n" + "    topology_template: thisIsService\r\n"
				+ "    description: update update";

		encodeAndSetPayload(mdJson, payload);
		runAndVerifyActionStatusError(mdJson, ActionStatus.NOT_RESOURCE_TOSCA_TEMPLATE);

	}

	@Test
	public void testMultipleResourcesInPayloadFail() {
		UploadResourceInfo mdJson = buildValidJson();

		String payload = "tosca_definitions_version: tosca_simple_yaml_1_0_0\r\n" + "node_types: \r\n" + "  org.openecomp.resource.importResource4test2:\r\n" + "    derived_from: tosca.nodes.Root\r\n" + "  org.openecomp.resource.importResource4test:\r\n"
				+ "    derived_from: tosca.nodes.Root\r\n" + "    description: update update";
		
		encodeAndSetPayload(mdJson, payload);
		runAndVerifyActionStatusError(mdJson, ActionStatus.NOT_SINGLE_RESOURCE);

	}

	@Test
	public void testNonValidNameSpaceInPayloadFail() {
		UploadResourceInfo mdJson = buildValidJson();

		String payload = "tosca_definitions_version: tosca_simple_yaml_1_0_0\r\n" + "node_types: \r\n" + "  org.openecomp.resourceX.importResource4test:\r\n" + "    derived_from: tosca.nodes.Root\r\n" + "    description: update update";

		encodeAndSetPayload(mdJson, payload);
		runAndVerifyActionStatusError(mdJson, ActionStatus.INVALID_RESOURCE_NAMESPACE);

	}

	private void encodeAndSetPayload(UploadResourceInfo mdJson, String payload) {
		byte[] encodedBase64Payload = Base64.encodeBase64(payload.getBytes());
		mdJson.setPayloadData(new String(encodedBase64Payload));
	}

	private void runAndVerifyActionStatusError(UploadResourceInfo mdJson, ActionStatus invalidResourcePayload) {
		setMD5OnRequest(true, mdJson);
		Response response = target().path("/v1/catalog/resources").request(MediaType.APPLICATION_JSON).post(Entity.json(gson.toJson(mdJson)), Response.class);
		Mockito.verify(componentUtils, Mockito.times(1)).getResponseFormat(Mockito.any(ActionStatus.class));
		Mockito.verify(componentUtils, Mockito.times(1)).getResponseFormat(invalidResourcePayload);
		assertTrue(response.getStatus() == HttpStatus.INTERNAL_SERVER_ERROR.value());
	}

	private void setMD5OnRequest(boolean isValid, UploadResourceInfo json) {
		String md5 = (isValid) ? GeneralUtility.calculateMD5Base64EncodedByString(gson.toJson(json)) : "stam=";
		when(request.getHeader(Constants.MD5_HEADER)).thenReturn(md5);

	}

	private UploadResourceInfo buildValidJson() {
		UploadResourceInfo ret = new UploadResourceInfo();
		ret.setName("ciMyCompute");
		ret.setPayloadName("ciMyCompute.yml");
		ret.addSubCategory("Application Layer 4+", "Application Servers");
		ret.setDescription("ResourceDescription");
		ret.setVendorName("VendorName");
		ret.setVendorRelease("VendorRelease");
		ret.setContactId("AT1234");
		ret.setIcon("router");
		ret.setTags(Arrays.asList(new String[] { "ciMyCompute" }));
		ret.setPayloadData(
				"dG9zY2FfZGVmaW5pdGlvbnNfdmVyc2lvbjogdG9zY2Ffc2ltcGxlX3lhbWxfMV8wXzANCm5vZGVfdHlwZXM6IA0KICBvcmcub3BlbmVjb21wLnJlc291cmNlLk15Q29tcHV0ZToNCiAgICBkZXJpdmVkX2Zyb206IHRvc2NhLm5vZGVzLlJvb3QNCiAgICBhdHRyaWJ1dGVzOg0KICAgICAgcHJpdmF0ZV9hZGRyZXNzOg0KICAgICAgICB0eXBlOiBzdHJpbmcNCiAgICAgIHB1YmxpY19hZGRyZXNzOg0KICAgICAgICB0eXBlOiBzdHJpbmcNCiAgICAgIG5ldHdvcmtzOg0KICAgICAgICB0eXBlOiBtYXANCiAgICAgICAgZW50cnlfc2NoZW1hOg0KICAgICAgICAgIHR5cGU6IHRvc2NhLmRhdGF0eXBlcy5uZXR3b3JrLk5ldHdvcmtJbmZvDQogICAgICBwb3J0czoNCiAgICAgICAgdHlwZTogbWFwDQogICAgICAgIGVudHJ5X3NjaGVtYToNCiAgICAgICAgICB0eXBlOiB0b3NjYS5kYXRhdHlwZXMubmV0d29yay5Qb3J0SW5mbw0KICAgIHJlcXVpcmVtZW50czoNCiAgICAgIC0gbG9jYWxfc3RvcmFnZTogDQogICAgICAgICAgY2FwYWJpbGl0eTogdG9zY2EuY2FwYWJpbGl0aWVzLkF0dGFjaG1lbnQNCiAgICAgICAgICBub2RlOiB0b3NjYS5ub2Rlcy5CbG9ja1N0b3JhZ2UNCiAgICAgICAgICByZWxhdGlvbnNoaXA6IHRvc2NhLnJlbGF0aW9uc2hpcHMuQXR0YWNoZXNUbw0KICAgICAgICAgIG9jY3VycmVuY2VzOiBbMCwgVU5CT1VOREVEXSAgDQogICAgY2FwYWJpbGl0aWVzOg0KICAgICAgaG9zdDogDQogICAgICAgIHR5cGU6IHRvc2NhLmNhcGFiaWxpdGllcy5Db250YWluZXINCiAgICAgICAgdmFsaWRfc291cmNlX3R5cGVzOiBbdG9zY2Eubm9kZXMuU29mdHdhcmVDb21wb25lbnRdIA0KICAgICAgZW5kcG9pbnQgOg0KICAgICAgICB0eXBlOiB0b3NjYS5jYXBhYmlsaXRpZXMuRW5kcG9pbnQuQWRtaW4gDQogICAgICBvczogDQogICAgICAgIHR5cGU6IHRvc2NhLmNhcGFiaWxpdGllcy5PcGVyYXRpbmdTeXN0ZW0NCiAgICAgIHNjYWxhYmxlOg0KICAgICAgICB0eXBlOiB0b3NjYS5jYXBhYmlsaXRpZXMuU2NhbGFibGUNCiAgICAgIGJpbmRpbmc6DQogICAgICAgIHR5cGU6IHRvc2NhLmNhcGFiaWxpdGllcy5uZXR3b3JrLkJpbmRhYmxl");
		return ret;
	}

	@Override
	protected Application configure() {

		ResourceConfig resourceConfig = new ResourceConfig(ResourcesServlet.class);
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
