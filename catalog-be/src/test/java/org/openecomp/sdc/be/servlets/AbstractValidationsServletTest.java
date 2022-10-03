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

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceImportManager;
import org.openecomp.sdc.be.externalapi.servlet.ArtifactExternalServlet;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.datastructure.Wrapper;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class AbstractValidationsServletTest {
    private static UserBusinessLogic userBusinessLogic = mock(UserBusinessLogic.class);
    private static ComponentInstanceBusinessLogic componentInstanceBL = mock(ComponentInstanceBusinessLogic.class);
    private static ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
    private static ServletUtils servletUtils = mock(ServletUtils.class);
    private static ResourceImportManager resourceImportManager = mock(ResourceImportManager.class);
    private static ArtifactsBusinessLogic artifactsBusinessLogic = mock(ArtifactsBusinessLogic.class);

    private static AbstractValidationsServlet servlet = new ArtifactExternalServlet(
        componentInstanceBL, componentsUtils, servletUtils,  resourceImportManager, artifactsBusinessLogic);

    private static final String BASIC_TOSCA_TEMPLATE = "tosca_definitions_version: tosca_simple_yaml_%s";

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCsarFromPayload() {

        String payloadName = "valid_vf.csar";
        String rootPath = System.getProperty("user.dir");
        Path path;
        byte[] data;
        String payloadData;
        Map<String, byte[]> returnValue = null;
        try {
            path = Paths.get(rootPath + "/src/test/resources/valid_vf.csar");
            data = Files.readAllBytes(path);
            payloadData = Base64.encodeBase64String(data);
            UploadResourceInfo resourceInfo = new UploadResourceInfo();
            resourceInfo.setPayloadName(payloadName);
            resourceInfo.setPayloadData(payloadData);
            Method privateMethod = null;
            privateMethod = AbstractValidationsServlet.class.getDeclaredMethod("getCsarFromPayload", UploadResourceInfo.class);
            privateMethod.setAccessible(true);
            returnValue = (Map<String, byte[]>) privateMethod.invoke(servlet, resourceInfo);
        } catch (IOException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        assertNotNull(returnValue);
    }

    @Test
    public void testValidToscaVersion() throws Exception {
        Stream.of("1_0", "1_0_0", "1_1", "1_1_0").forEach(this::testValidToscaVersion);
    }


    private void testValidToscaVersion(String version)  {
        Wrapper<Response> responseWrapper = new Wrapper<>();
        servlet.validatePayloadIsTosca(responseWrapper, new UploadResourceInfo(), new User(), String.format(BASIC_TOSCA_TEMPLATE, version));
        assertTrue(responseWrapper.isEmpty());
    }



}
