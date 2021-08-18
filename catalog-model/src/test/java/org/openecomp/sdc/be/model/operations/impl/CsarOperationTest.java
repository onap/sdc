/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.model.operations.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.client.onboarding.api.OnboardingClient;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.VendorSoftwareProduct;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;

class CsarOperationTest {

    @Mock
    private OnboardingClient onboardingClient;

    @InjectMocks
    private CsarOperation csarOperation;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findVspLatestPackageSuccessTest() {
        final var csarUuid = "csarUuid";
        var user = new User("userId");
        final Map<String, byte[]> csarFileMap = new HashMap<>();
        csarFileMap.put("test", "test".getBytes(StandardCharsets.UTF_8));
        when(onboardingClient.findLatestPackage(csarUuid, user.getUserId())).thenReturn(Either.left(csarFileMap));
        final Either<Map<String, byte[]>, StorageOperationStatus> vspLatestPackage = csarOperation.findVspLatestPackage(csarUuid, user);
        assertTrue(vspLatestPackage.isLeft());
        final Map<String, byte[]> actualCsarFileMap = vspLatestPackage.left().value();
        assertEquals(csarFileMap, actualCsarFileMap);
    }

    @Test
    void findVspLatestPackage_csarNotFoundTest() {
        //given
        final var vspId = "vspId";
        var user = new User("userId");
        //when
        when(onboardingClient.findLatestPackage(vspId, user.getUserId())).thenReturn(Either.right(StorageOperationStatus.CSAR_NOT_FOUND));
        final Either<Map<String, byte[]>, StorageOperationStatus> vspLatestPackage = csarOperation.findVspLatestPackage(vspId, user);
        //then
        assertTrue(vspLatestPackage.isRight());
        final StorageOperationStatus storageOperationStatus = vspLatestPackage.right().value();
        assertEquals(StorageOperationStatus.CSAR_NOT_FOUND, storageOperationStatus);
    }

    @Test
    void findVspSuccessTest() {
        //given
        final var vspId = "vspId";
        final var vspVersionId = "vspVersionId";
        var user = new User("userId");
        var vendorSoftwareProduct = new VendorSoftwareProduct();
        vendorSoftwareProduct.setId(vspId);
        vendorSoftwareProduct.setVersionId(vspVersionId);
        //when
        when(onboardingClient.findVendorSoftwareProduct(vspId, vspVersionId, user.getUserId())).thenReturn(Optional.of(vendorSoftwareProduct));
        final Optional<VendorSoftwareProduct> vspOptional = csarOperation.findVsp(vspId, vspVersionId, user);
        //then
        assertTrue(vspOptional.isPresent());
        assertEquals(vendorSoftwareProduct, vspOptional.get());
    }

    @Test
    void findVsp_vspNotFoundTest() {
        //given
        final var vspId = "vspId";
        final var vspVersionId = "vspVersionId";
        var user = new User("userId");
        //when
        when(onboardingClient.findVendorSoftwareProduct(vspId, vspVersionId, user.getUserId())).thenReturn(Optional.empty());
        final Optional<VendorSoftwareProduct> vspOptional = csarOperation.findVsp(vspId, vspVersionId, user);
        //then
        assertTrue(vspOptional.isEmpty());
    }
}