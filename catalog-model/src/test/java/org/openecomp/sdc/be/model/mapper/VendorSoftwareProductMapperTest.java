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

package org.openecomp.sdc.be.model.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.model.VendorSoftwareProduct;
import org.openecomp.sdc.be.model.dto.VendorSoftwareProductDto;

class VendorSoftwareProductMapperTest {

    @Test
    void mapFromSuccess() {
        final var vendorSoftwareProductDto = new VendorSoftwareProductDto();
        vendorSoftwareProductDto.setName("name");
        vendorSoftwareProductDto.setDescription("description");
        vendorSoftwareProductDto.setCategory("category");
        vendorSoftwareProductDto.setSubCategory("subcategory");
        vendorSoftwareProductDto.setVendorName("vendorName");
        vendorSoftwareProductDto.setVendorId("vendorId");
        vendorSoftwareProductDto.setSelectedModelList(List.of("model1", "model2"));
        vendorSoftwareProductDto.setOnboardingMethod("onboardingMethod");
        vendorSoftwareProductDto.setId("id");
        vendorSoftwareProductDto.setVersionId("versionId");
        vendorSoftwareProductDto.setOnboardingOrigin("onboardingOrigin");
        vendorSoftwareProductDto.setNetworkPackageName("packageName");
        final VendorSoftwareProduct vendorSoftwareProduct = VendorSoftwareProductMapper.mapFrom(vendorSoftwareProductDto);
        assertVendorSoftwareProduct(vendorSoftwareProduct, vendorSoftwareProductDto);
    }

    @Test
    void mapFromNullModelListShouldReturnEmptyModelList() {
        final var vendorSoftwareProductDto = new VendorSoftwareProductDto();
        vendorSoftwareProductDto.setSelectedModelList(null);
        final VendorSoftwareProduct vendorSoftwareProduct = VendorSoftwareProductMapper.mapFrom(vendorSoftwareProductDto);
        assertVendorSoftwareProduct(vendorSoftwareProduct, vendorSoftwareProductDto);
    }

    private void assertVendorSoftwareProduct(final VendorSoftwareProduct vendorSoftwareProduct,
                                             final VendorSoftwareProductDto vendorSoftwareProductDto) {
        assertEquals(vendorSoftwareProduct.getId(), vendorSoftwareProductDto.getId(), "id should be equals");
        assertEquals(vendorSoftwareProduct.getName(), vendorSoftwareProductDto.getName(), "name should be equals");
        assertEquals(vendorSoftwareProduct.getDescription(), vendorSoftwareProductDto.getDescription(), "description should be equals");
        assertEquals(vendorSoftwareProduct.getCategory(), vendorSoftwareProductDto.getCategory(), "category should be equals");
        assertEquals(vendorSoftwareProduct.getSubCategory(), vendorSoftwareProductDto.getSubCategory(), "subCategory should be equals");
        assertEquals(vendorSoftwareProduct.getVendorName(), vendorSoftwareProductDto.getVendorName(), "vendorName should be equals");
        assertEquals(vendorSoftwareProduct.getVendorId(), vendorSoftwareProductDto.getVendorId(), "vendorId should be equals");
        assertEquals(vendorSoftwareProduct.getOnboardingMethod(), vendorSoftwareProductDto.getOnboardingMethod(), "onboardingMethod should be equals");
        assertEquals(vendorSoftwareProduct.getOnboardingOrigin(), vendorSoftwareProductDto.getOnboardingOrigin(), "onboardingOrigin should be equals");
        assertEquals(vendorSoftwareProduct.getVersionId(), vendorSoftwareProductDto.getVersionId(), "versionId should be equals");
        assertEquals(vendorSoftwareProduct.getNetworkPackageName(), vendorSoftwareProductDto.getNetworkPackageName(), "networkPackageName should be equals");
        if (vendorSoftwareProductDto.getSelectedModelList() == null) {
            assertTrue(vendorSoftwareProduct.getModelList().isEmpty(), "modelList should be an empty list");
        } else {
            assertEquals(vendorSoftwareProduct.getModelList(), vendorSoftwareProductDto.getSelectedModelList(), "modelList should be equals");
        }
    }
}