/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdcrests.vsp.rest.mapping;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.LicenseType;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspDetailsDto;

/**
 * This class was generated.
 */
public class MapVspDetailsToDtoTest {

    @Test()
    public void testConversion() {

        final VspDetails source = new VspDetails();

        final String id = "072b88a8-0b34-4e3e-a0be-1f664100f76c";
        source.setId(id);

        final String versionId = "345-96f20205613f";
        final Version version = new Version(versionId);
        source.setVersion(version);

        final String name = "cd60b1d0-cf2f-48b4-b578-96f20205613f";
        source.setName(name);

        final String description = "3baf3188-c7b4-48a2-9b4d-0258216ce4b5";
        source.setDescription(description);

        final String category = "d05d10e3-35d6-45aa-83a5-f41194468fc2";
        source.setCategory(category);

        final String subCategory = "9a4652df-ff91-4817-bfa0-ded9fd57012e";
        source.setSubCategory(subCategory);

        final String icon = "2ee3b647-f3f0-4c85-9134-b4e960246b06";
        source.setIcon(icon);

        final String vendorName = "e9351739-6a2b-4214-b386-e71dc48ff97d";
        source.setVendorName(vendorName);

        final String vendorId = "b2f67f43-3b19-4037-a982-ed7bc2ed8d68";
        source.setVendorId(vendorId);

        final String vlmVersionId = "3b194037a982ed7b";
        source.setVlmVersion(new Version(vlmVersionId));

        final String onboardingMethod = "b46520ac-e62f-4a24-8f40-ee6e65889bfc";
        source.setOnboardingMethod(onboardingMethod);

        final String licenseType = LicenseType.EXTERNAL.name();
        source.setLicenseType(licenseType);

        final VspDetailsDto target = new VspDetailsDto();
        final MapVspDetailsToDto mapper = new MapVspDetailsToDto();
        mapper.doMapping(source, target);

        assertEquals(id, source.getId());
        assertEquals(versionId, target.getVersion());
        assertEquals(name, target.getName());
        assertEquals(description, target.getDescription());
        assertEquals(icon, target.getIcon());
        assertEquals(category, target.getCategory());
        assertEquals(subCategory, target.getSubCategory());
        assertEquals(vendorId, target.getVendorId());
        assertEquals(vendorName, target.getVendorName());
        assertEquals(vlmVersionId, target.getLicensingVersion());
        assertEquals(onboardingMethod, target.getOnboardingMethod());
        assertEquals(LicenseType.EXTERNAL, target.getLicenseType());
    }
}
