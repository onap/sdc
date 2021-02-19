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


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.UploadServiceInfo;

@ExtendWith(MockitoExtension.class)
class ServiceImportManagerTest {

    @InjectMocks
    private ServiceImportManager serviceImportManager;

    private ServiceImportManager createTestSubject() {
        return new ServiceImportManager();
    }

    @Test
    void testGetServiceImportBusinessLogic() {
        ServiceImportManager testSubject;
        ServiceImportBusinessLogic result;

        testSubject = createTestSubject();
        result = testSubject.getServiceImportBusinessLogic();
    }

    @Test
    void testSetServiceImportBusinessLogic() {
        ServiceImportManager testSubject;
        ServiceImportBusinessLogic serviceImportBusinessLogic = null;

        testSubject = createTestSubject();
        testSubject.setServiceImportBusinessLogic(serviceImportBusinessLogic);
    }

    @Test
    void testGetServiceBusinessLogic() {
        ServiceImportManager testSubject;
        ServiceBusinessLogic result;

        testSubject = createTestSubject();
        result = testSubject.getServiceBusinessLogic();
    }

    @Test
    void testSetServiceBusinessLogic() {
        ServiceImportManager testSubject;
        ServiceBusinessLogic serviceBusinessLogic = null;

        testSubject = createTestSubject();
        testSubject.setServiceBusinessLogic(serviceBusinessLogic);
    }

    @Test
    void testPopulateServiceMetadata() {
        UploadServiceInfo serviceMetaData = null;
        Service service = null;
        if (serviceMetaData != null || service != null) {
            serviceImportManager.populateServiceMetadata(serviceMetaData, service);
        }
    }

}
