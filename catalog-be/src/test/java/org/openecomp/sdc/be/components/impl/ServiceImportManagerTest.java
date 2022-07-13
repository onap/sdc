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


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.UploadServiceInfo;

@ExtendWith(MockitoExtension.class)
class ServiceImportManagerTest {

    private final ServiceBusinessLogic serviceBusinessLogic = Mockito.mock(ServiceBusinessLogic.class);
    @InjectMocks
    private ServiceImportManager serviceImportManager;

    @BeforeEach
    public void setup() {
        serviceImportManager = new ServiceImportManager();
    }

    private ServiceImportManager createTestSubject() {
        return new ServiceImportManager();
    }

    @Test
    void testGetServiceImportBusinessLogic() {
        ServiceImportManager testSubject;
        ServiceImportBusinessLogic result;

        testSubject = createTestSubject();
        result = testSubject.getServiceImportBusinessLogic();
        assertNull(result);
    }

    @Test
    void testSetServiceImportBusinessLogic() {
        ServiceImportManager testSubject;
        ServiceImportBusinessLogic serviceImportBusinessLogic = null;

        testSubject = createTestSubject();
        testSubject.setServiceImportBusinessLogic(serviceImportBusinessLogic);
        assertNotNull(testSubject);
    }

    @Test
    void testGetServiceBusinessLogic() {
        ServiceImportManager testSubject;
        ServiceBusinessLogic result;

        testSubject = createTestSubject();
        result = testSubject.getServiceBusinessLogic();
        assertNull(result);
    }

    @Test
    void testSetServiceBusinessLogic() {
        ServiceImportManager testSubject;
        ServiceBusinessLogic serviceBusinessLogic = null;

        testSubject = createTestSubject();
        testSubject.setServiceBusinessLogic(serviceBusinessLogic);
        assertNotNull(testSubject);
    }

    @Test
    void testPopulateServiceMetadata() {
        UploadServiceInfo serviceMetaData = new UploadServiceInfo();
        serviceMetaData.setDescription("Description");
        serviceMetaData.setVendorName("VendorName");
        serviceMetaData.setVendorRelease("VendorRelease");
        Service service = new Service();
        service.setName("service");
        serviceImportManager.populateServiceMetadata(serviceMetaData, service);
    }
}
