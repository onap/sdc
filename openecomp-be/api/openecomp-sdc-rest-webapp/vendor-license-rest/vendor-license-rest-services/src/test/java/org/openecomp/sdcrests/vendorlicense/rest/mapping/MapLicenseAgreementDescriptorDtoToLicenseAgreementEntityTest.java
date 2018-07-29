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
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseTerm;
import org.openecomp.sdcrests.vendorlicense.types.ChoiceOrOtherDto;
import org.openecomp.sdcrests.vendorlicense.types.LicenseAgreementDescriptorDto;

import static org.testng.Assert.assertEquals;


public class MapLicenseAgreementDescriptorDtoToLicenseAgreementEntityTest {

    @Test
    public void testName() {
        LicenseAgreementDescriptorDto source = new LicenseAgreementDescriptorDto();
        LicenseAgreementEntity target = new LicenseAgreementEntity();
        MapLicenseAgreementDescriptorDtoToLicenseAgreementEntity mapper =
                new MapLicenseAgreementDescriptorDtoToLicenseAgreementEntity();
        String param = "62d9b3aa-1d06-453f-a412-62d8cba7164d";
        source.setName(param);
        mapper.doMapping(source, target);
        assertEquals(target.getName(), param);
    }

    @Test
    public void testDescription() {
        LicenseAgreementDescriptorDto source = new LicenseAgreementDescriptorDto();
        LicenseAgreementEntity target = new LicenseAgreementEntity();
        MapLicenseAgreementDescriptorDtoToLicenseAgreementEntity mapper =
                new MapLicenseAgreementDescriptorDtoToLicenseAgreementEntity();
        String param = "3888dc1f-516b-4790-a5fe-73efadb2ba38";
        source.setDescription(param);
        mapper.doMapping(source, target);
        assertEquals(target.getDescription(), param);
    }

    @Test
    public void testRequirementsAndConstrains() {
        LicenseAgreementDescriptorDto source = new LicenseAgreementDescriptorDto();
        LicenseAgreementEntity target = new LicenseAgreementEntity();
        MapLicenseAgreementDescriptorDtoToLicenseAgreementEntity mapper =
                new MapLicenseAgreementDescriptorDtoToLicenseAgreementEntity();
        String param = "be6df4d3-b534-4c62-b611-4ed5b67e72ab";
        source.setRequirementsAndConstrains(param);
        mapper.doMapping(source, target);
        assertEquals(target.getRequirementsAndConstrains(), param);
    }

    @Test
    public void testLicenseTerm() {
        LicenseAgreementDescriptorDto source = new LicenseAgreementDescriptorDto();
        LicenseAgreementEntity target = new LicenseAgreementEntity();
        MapLicenseAgreementDescriptorDtoToLicenseAgreementEntity mapper =
                new MapLicenseAgreementDescriptorDtoToLicenseAgreementEntity();
        ChoiceOrOtherDto<LicenseTerm> licenseTermChoiceOrOtherDto = new ChoiceOrOtherDto<>();
        source.setLicenseTerm(licenseTermChoiceOrOtherDto);
        mapper.doMapping(source, target);
        assertEquals(target.getLicenseTerm().getChoice(), licenseTermChoiceOrOtherDto.getChoice());
    }
}
