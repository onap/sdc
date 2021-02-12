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
import static org.junit.Assert.assertNull;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.LicenseType;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.types.LicensingData;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspDescriptionDto;

/**
 * This class was generated.
 */
public class MapVspDescriptionDtoToVspDetailsTest {

    @Test()
    public void testConversion() {

        final VspDescriptionDto source = new VspDescriptionDto();

        final String name = "e507ee61-df86-4cbd-8efc-fc5ac7182a42";
        source.setName(name);

        final String description = "9493bf30-d5dd-4436-9cf8-917ef5a9f2c4";
        source.setDescription(description);

        final String icon = "593e7453-239d-4979-abc7-2b3d0c9bfa30";
        source.setIcon(icon);

        final String category = "0dee3401-f7e5-4da4-bd36-8f88c4380884";
        source.setCategory(category);

        final String subCategory = "4db52d19-bb60-4490-b88a-a14d1237316a";
        source.setSubCategory(subCategory);

        final String vendorName = "200378cb-ee09-47fb-975b-25fe4ddd3794";
        source.setVendorName(vendorName);

        final String vendorId = "a634f9b4-685e-4903-b23d-572b67d1374c";
        source.setVendorId(vendorId);

        final VspDetails target = new VspDetails();
        final MapVspDescriptionDtoToVspDetails mapper = new MapVspDescriptionDtoToVspDetails();
        mapper.doMapping(source, target);

        assertEquals(name, target.getName());
        assertEquals(description, target.getDescription());
        assertEquals(category, target.getCategory());
        assertEquals(subCategory, target.getSubCategory());
        assertEquals(icon, target.getIcon());
        assertEquals(vendorName, target.getVendorName());
        assertEquals(vendorId, target.getVendorId());
    }

    @Test
    public void testLicenceTypeMapping() {
        final VspDescriptionDto source = new VspDescriptionDto();
        LicensingData licensingData = new LicensingData();
        licensingData.setLicenseAgreement("testLicenseAgreement");
        licensingData.setFeatureGroups(Collections.emptyList());
        source.setLicenseType(LicenseType.EXTERNAL);
        source.setLicensingData(licensingData);

        final VspDetails target = new VspDetails();
        final MapVspDescriptionDtoToVspDetails mapper = new MapVspDescriptionDtoToVspDetails();
        mapper.doMapping(source, target);
        assertEquals(LicenseType.EXTERNAL.name(), target.getLicenseType());
        assertNull(target.getLicenseAgreement());
        assertNull(target.getFeatureGroups());
    }
}
