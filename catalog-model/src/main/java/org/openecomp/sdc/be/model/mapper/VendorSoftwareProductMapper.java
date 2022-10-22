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

import java.util.ArrayList;
import java.util.Objects;
import org.openecomp.sdc.be.model.VendorSoftwareProduct;
import org.openecomp.sdc.be.model.dto.VendorSoftwareProductDto;

public class VendorSoftwareProductMapper {

    private VendorSoftwareProductMapper() {
    }

    public static VendorSoftwareProduct mapFrom(final VendorSoftwareProductDto vendorSoftwareProductDto) {
        Objects.requireNonNull(vendorSoftwareProductDto);
        final var vendorSoftwareProduct = new VendorSoftwareProduct();
        vendorSoftwareProduct.setName(vendorSoftwareProductDto.getName());
        vendorSoftwareProduct.setDescription(vendorSoftwareProductDto.getDescription());
        vendorSoftwareProduct.setCategory(vendorSoftwareProductDto.getCategory());
        vendorSoftwareProduct.setSubCategory(vendorSoftwareProductDto.getSubCategory());
        vendorSoftwareProduct.setVendorName(vendorSoftwareProductDto.getVendorName());

        vendorSoftwareProduct.setTenant(vendorSoftwareProductDto.getTenant());

        vendorSoftwareProduct.setVendorId(vendorSoftwareProductDto.getVendorId());
        vendorSoftwareProduct.setModelList(
            vendorSoftwareProductDto.getSelectedModelList() == null ? new ArrayList<>() : vendorSoftwareProductDto.getSelectedModelList());
        vendorSoftwareProduct.setOnboardingMethod(vendorSoftwareProductDto.getOnboardingMethod());
        vendorSoftwareProduct.setId(vendorSoftwareProductDto.getId());
        vendorSoftwareProduct.setVersionId(vendorSoftwareProductDto.getVersionId());
        vendorSoftwareProduct.setOnboardingOrigin(vendorSoftwareProductDto.getOnboardingOrigin());
        vendorSoftwareProduct.setNetworkPackageName(vendorSoftwareProductDto.getNetworkPackageName());
        return vendorSoftwareProduct;
    }


}
