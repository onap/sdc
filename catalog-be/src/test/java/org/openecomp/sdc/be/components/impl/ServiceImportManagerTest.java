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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.UploadServiceInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceImportManagerTest {

    @Mock
    private ServiceBusinessLogic serviceBusinessLogic;

    @InjectMocks
    private ServiceImportManager serviceImportManager;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        serviceImportManager = new ServiceImportManager(serviceBusinessLogic, null);
    }

    @Test
    void testPopulateServiceMetadata() {
        UploadServiceInfo serviceMetaData = new UploadServiceInfo();
        serviceMetaData.setDescription("Description");
        serviceMetaData.setVendorName("VendorName");
        serviceMetaData.setVendorRelease("VendorRelease");
        serviceMetaData.setDerivedFromGenericVersion("derivedFromGenericVersion");
        Service service = new Service();
        service.setName("service");
        serviceImportManager.populateServiceMetadata(serviceMetaData, service);
        assertEquals("derivedFromGenericVersion", service.getDerivedFromGenericVersion());
    }

    @Test
    void testIsServiceExist() {
        when(serviceBusinessLogic.isServiceExist(anyString())).thenReturn(false);
        boolean result = serviceImportManager.isServiceExist("no exist");
        assertFalse(result);
    }
}
