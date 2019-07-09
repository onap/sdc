/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

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

import org.junit.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.PackageInfoDto;

/**
 * This class was generated.
 */
public class MapPackageInfoToPackageInfoDtoTest {

    @Test()
    public void testConversion() {

        final PackageInfo source = new PackageInfo();

        final String version = "5e20ad09-38c1-4230-bb53-c5b4faa26ced";
        source.setVersion(version);

        final String vspName = "4a0b3575-807f-4aa3-b142-3880374fe974";
        source.setVspName(vspName);

        final String description = "6798b60a-f6fa-4e46-aead-4edc7eb2d405";
        source.setVspDescription(description);

        final String packageId = "c381c91c-d872-4a95-8f63-3f15170693b9";
        source.setVspId(packageId);

        final String vendorName = "416c7ffa-f595-4459-8d5c-9f57a6ac5234";
        source.setVendorName(vendorName);

        final String category = "7a07b1e1-2e6d-43af-8ddc-1807dfa8acf2";
        source.setCategory(category);

        final String subCategory = "b2efb540-86af-4949-873c-6a77a7dcc92b";
        source.setSubCategory(subCategory);

        final String vendorRelease = "070bb92c-91c7-4ecb-87c3-7497d55eb9b3";
        source.setVendorRelease(vendorRelease);

        final String packageChecksum = "d691b1cc-91fd-4645-a46f-483000aeaf06";
        source.setPackageChecksum(packageChecksum);

        final String packageType = "a158c2f9-5970-40cd-9e7f-efa4be050c59";
        source.setPackageType(packageType);

        final PackageInfoDto target = new PackageInfoDto();
        final MapPackageInfoToPackageInfoDto mapper = new MapPackageInfoToPackageInfoDto();
        mapper.doMapping(source, target);

        assertEquals(description, target.getDescription());
        assertEquals(vspName, target.getVspName());
        assertEquals(version, target.getVersion());
        assertEquals(packageId, target.getPackageId());
        assertEquals(category, target.getCategory());
        assertEquals(subCategory, target.getSubCategory());
        assertEquals(vendorName, target.getVendorName());
        assertEquals(vendorRelease, target.getVendorRelease());
        assertEquals(packageChecksum, target.getPackageChecksum());
        assertEquals(packageType, target.getPackageType());
    }
}
