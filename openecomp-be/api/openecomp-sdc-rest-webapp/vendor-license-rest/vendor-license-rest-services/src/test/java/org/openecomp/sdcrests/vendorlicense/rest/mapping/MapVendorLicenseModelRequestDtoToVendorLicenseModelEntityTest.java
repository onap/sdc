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

package org.openecomp.sdcrests.vendorlicense.rest.mapping;

import org.junit.Test;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdcrests.vendorlicense.types.VendorLicenseModelRequestDto;

import static org.testng.Assert.assertEquals;


public class MapVendorLicenseModelRequestDtoToVendorLicenseModelEntityTest {

    @Test
    public void testDescription() {
        VendorLicenseModelRequestDto source = new VendorLicenseModelRequestDto();
        VendorLicenseModelEntity target = new VendorLicenseModelEntity();
        MapVendorLicenseModelRequestDtoToVendorLicenseModelEntity mapper =
                new MapVendorLicenseModelRequestDtoToVendorLicenseModelEntity();
        String param = "b77cbdda-ad74-49df-81e6-60438f5f5ff5";
        source.setDescription(param);
        mapper.doMapping(source, target);
        assertEquals(target.getDescription(), param);
    }

    @Test
    public void testVendorName() {
        VendorLicenseModelRequestDto source = new VendorLicenseModelRequestDto();
        VendorLicenseModelEntity target = new VendorLicenseModelEntity();
        MapVendorLicenseModelRequestDtoToVendorLicenseModelEntity mapper =
                new MapVendorLicenseModelRequestDtoToVendorLicenseModelEntity();
        String param = "e0559039-599b-447c-a859-fec2cc26e08d";
        source.setVendorName(param);
        mapper.doMapping(source, target);
        assertEquals(target.getVendorName(), param);
    }

    @Test
    public void testIconRef() {
        VendorLicenseModelRequestDto source = new VendorLicenseModelRequestDto();
        VendorLicenseModelEntity target = new VendorLicenseModelEntity();
        MapVendorLicenseModelRequestDtoToVendorLicenseModelEntity mapper =
                new MapVendorLicenseModelRequestDtoToVendorLicenseModelEntity();
        String param = "e79f4e78-ff71-4033-a8d8-3b86d08c9366";
        source.setIconRef(param);
        mapper.doMapping(source, target);
        assertEquals(target.getIconRef(), param);
    }
}
