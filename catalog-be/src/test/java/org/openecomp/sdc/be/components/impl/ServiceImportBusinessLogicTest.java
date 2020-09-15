/*

 * Copyright (c) 2018 AT&T Intellectual Property.

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *     http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */

package org.openecomp.sdc.be.components.impl;


import fj.data.Either;
import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.csar.CsarBusinessLogic;
import org.openecomp.sdc.be.externalapi.servlet.ArtifactExternalServlet;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.ServletUtils;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.UploadResourceInfo;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.servlets.AbstractValidationsServlet;
import org.openecomp.sdc.be.user.UserBusinessLogic;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceImportBusinessLogicTest extends ServiceImportBussinessLogicBaseTestSetup {
    private final static String DEFAULT_ICON = "defaulticon";

    @InjectMocks
    static ServiceImportBusinessLogic serviceImportBusinessLogic;
    @Mock
    private ServiceBusinessLogic serviceBusinessLogic;
    @Mock
    private CsarBusinessLogic csarBusinessLogic;
    @Mock
    protected ToscaOperationFacade toscaOperationFacade;


    private static UserBusinessLogic userBusinessLogic = Mockito.mock(UserBusinessLogic.class);
    private static ComponentInstanceBusinessLogic componentInstanceBusinessLogic = Mockito.mock(ComponentInstanceBusinessLogic.class);
    private static ComponentsUtils componentsUtils = new ComponentsUtils(Mockito.mock(AuditingManager.class));
    private static ServletUtils servletUtils = mock(ServletUtils.class);
    private static ResourceImportManager resourceImportManager = mock(ResourceImportManager.class);
    private static ArtifactsBusinessLogic artifactsBusinessLogic = mock(ArtifactsBusinessLogic.class);

    private static AbstractValidationsServlet servlet = new ArtifactExternalServlet(userBusinessLogic,
            componentInstanceBusinessLogic, componentsUtils, servletUtils, resourceImportManager, artifactsBusinessLogic);


    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetComponentsUtils() {
        ComponentsUtils result;
        result = serviceImportBusinessLogic.getComponentsUtils();
    }

    @Test
    public void testSetComponentsUtils() {
        ComponentsUtils componentsUtils = null;

        serviceImportBusinessLogic.setComponentsUtils(componentsUtils);
    }

    @Test
    public void testInvalidEnvironmentContext() {
        Service newService = createServiceObject(false);
        String payloadName = "valid_vf";
        Map<String, byte[]> payload = crateCsarFromPayload();
        try {
            when(serviceBusinessLogic.validateServiceBeforeCreate(newService,user,AuditingActionEnum.CREATE_RESOURCE))
                    .thenReturn(Either.left(newService));
            when(toscaOperationFacade.validateCsarUuidUniqueness(payloadName)).thenReturn(StorageOperationStatus.OK);
            sIB1.createService(newService, AuditingActionEnum.CREATE_RESOURCE, user, payload, payloadName);
        } catch (Exception exp) {
            exp.printStackTrace();
            return;
        }
        fail();
    }

    private Map<String, byte[]> crateCsarFromPayload() {
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
        return returnValue;
    }


}