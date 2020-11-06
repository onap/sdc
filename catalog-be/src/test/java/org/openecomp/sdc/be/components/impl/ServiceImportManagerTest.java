/*
 * Copyright (C) 2020 CMCC, Inc. and others. All rights reserved.
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


import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.UploadServiceInfo;
import org.openecomp.sdc.common.api.Constants;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


public class ServiceImportManagerTest {
    @InjectMocks
    private ServiceImportManager serviceImportManager;
    public static final ServiceBusinessLogic serviceBusinessLogic = Mockito.mock(ServiceBusinessLogic.class);
    public static final ServletContext servletContext = Mockito.mock(ServletContext.class);
    public static final WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
    public static WebApplicationContext webApplicationContext = Mockito.mock(WebApplicationContext.class);

    @Before
    public void setup() {
        serviceImportManager = new ServiceImportManager();
        servletContext.setAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR,webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);
    }

    private ServiceImportManager createTestSubject() {
        return new ServiceImportManager();
    }

    @Test
    public void testGetServiceImportBusinessLogic() {
        ServiceImportManager testSubject;
        ServiceImportBusinessLogic result;

        testSubject = createTestSubject();
        result = testSubject.getServiceImportBusinessLogic();
    }

    @Test
    public void testSetServiceImportBusinessLogic() {
        ServiceImportManager testSubject;
        ServiceImportBusinessLogic serviceImportBusinessLogic=null;

        testSubject = createTestSubject();
        testSubject.setServiceImportBusinessLogic(serviceImportBusinessLogic);
    }


    @Test
    public void testGetServiceBusinessLogic() {
        ServiceImportManager testSubject;
        ServiceBusinessLogic result;

        testSubject = createTestSubject();
        result = testSubject.getServiceBusinessLogic();
    }

    @Test
    public void testSetServiceBusinessLogic() {
        ServiceImportManager testSubject;
        ServiceBusinessLogic serviceBusinessLogic = null;

        testSubject = createTestSubject();
        testSubject.setServiceBusinessLogic(serviceBusinessLogic);
    }

    @Test
    public void testPopulateServiceMetadata() {
        UploadServiceInfo serviceMetaData = new UploadServiceInfo();
        serviceMetaData.setDescription("Description");
        serviceMetaData.setVendorName("VendorName");
        serviceMetaData.setVendorRelease("VendorRelease");
        Service service = new Service();
        service.setName("service");
        serviceImportManager.populateServiceMetadata(serviceMetaData, service);
    }

    @Test
    public void testInit(){
        try {
            serviceImportManager.init(servletContext);
        } catch (Exception e) {
            assertEquals(NullPointerException.class,e.getClass());
        }
    }
}