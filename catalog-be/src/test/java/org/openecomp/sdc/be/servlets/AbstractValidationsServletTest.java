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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.servlets.AbstractValidationsServlet;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;

import static org.mockito.Mockito.mock;

public class AbstractValidationsServletTest {
	private static AbstractValidationsServlet servlet = mock(AbstractValidationsServlet.class);

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
			privateMethod = AbstractValidationsServlet.class.getDeclaredMethod("getScarFromPayload", UploadResourceInfo.class);
			privateMethod.setAccessible(true);
			returnValue = (Either<Map<String, byte[]>, ResponseFormat>) privateMethod.invoke(servlet, resourceInfo);
		} catch (IOException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		Assert.assertTrue(returnValue.isLeft());
		Map<String, byte[]> csar = returnValue.left().value();
		Assert.assertTrue(csar != null);
	}
}
