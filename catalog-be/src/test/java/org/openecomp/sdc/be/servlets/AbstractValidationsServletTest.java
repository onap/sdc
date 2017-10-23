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
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.tinkerpop.gremlin.structure.T;
import org.glassfish.grizzly.servlet.ServletUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.servlets.ResourceUploadServlet.ResourceAuthorityTypeEnum;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;

import com.google.common.base.Supplier;
import com.google.gson.Gson;

import aj.org.objectweb.asm.Type;
import fj.data.Either;

public class AbstractValidationsServletTest {
	private static AbstractValidationsServlet servlet = new AbstractValidationsServlet() {
	};

	private static final String BASIC_TOSCA_TEMPLATE = "tosca_definitions_version: tosca_simple_yaml_%s";

	@Before
	public void setUp() throws Exception {
		servlet.initLog(mock(Logger.class));
	}

	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetScarFromPayload() {

		String payloadName = "valid_vf.csar";
		String rootPath = System.getProperty("user.dir");
		Path path = null;
		byte[] data = null;
		String payloadData = null;
		Either<Map<String, byte[]>, ResponseFormat> returnValue = null;
		try {
			path = Paths.get(rootPath + "/src/test/resources/valid_vf.csar");
			data = Files.readAllBytes(path);
			payloadData = Base64.encodeBase64String(data);
			UploadResourceInfo resourceInfo = new UploadResourceInfo();
			resourceInfo.setPayloadName(payloadName);
			resourceInfo.setPayloadData(payloadData);
			Method privateMethod = null;
			privateMethod = AbstractValidationsServlet.class.getDeclaredMethod("getScarFromPayload",
					UploadResourceInfo.class);
			privateMethod.setAccessible(true);
			returnValue = (Either<Map<String, byte[]>, ResponseFormat>) privateMethod.invoke(servlet, resourceInfo);
		} catch (IOException | NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		assertTrue(returnValue.isLeft());
		Map<String, byte[]> csar = returnValue.left().value();
		assertTrue(csar != null);
	}

	@Test
	public void testValidToscaVersion() throws Exception {
		Stream.of("1_0", "1_0_0", "1_1", "1_1_0").forEach(this::testValidToscaVersion);
	}

	private void testValidToscaVersion(String version) {
		Wrapper<Response> responseWrapper = new Wrapper<>();
		servlet.validatePayloadIsTosca(responseWrapper, new UploadResourceInfo(), new User(),
				String.format(BASIC_TOSCA_TEMPLATE, version));
		assertTrue(responseWrapper.isEmpty());
	}

	
}
