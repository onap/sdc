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
import org.openecomp.sdc.vendorlicense.dao.types.ChoiceOrOther;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseTerm;
import org.openecomp.sdcrests.vendorlicense.types.LicenseAgreementDescriptorDto;

import static org.testng.Assert.assertEquals;


public class MapLicenseAgreementEntityToLicenseAgreementDescriptorDtoTest {

    @Test
    public void testLicenseTerm() {
        LicenseAgreementEntity source = new LicenseAgreementEntity();
        LicenseAgreementDescriptorDto target = new LicenseAgreementDescriptorDto();
        MapLicenseAgreementEntityToLicenseAgreementDescriptorDto mapper =
                new MapLicenseAgreementEntityToLicenseAgreementDescriptorDto();
        ChoiceOrOther<LicenseTerm> licenseTermChoiceOrOther = new ChoiceOrOther<>();
        licenseTermChoiceOrOther.setChoice(LicenseTerm.Other);
        source.setLicenseTerm(licenseTermChoiceOrOther);
        mapper.doMapping(source, target);
        assertEquals(target.getLicenseTerm().getChoice(), licenseTermChoiceOrOther.getChoice());
    }

    @Test
    public void testName() {
        LicenseAgreementEntity source = new LicenseAgreementEntity();
        LicenseAgreementDescriptorDto target = new LicenseAgreementDescriptorDto();
        MapLicenseAgreementEntityToLicenseAgreementDescriptorDto mapper =
                new MapLicenseAgreementEntityToLicenseAgreementDescriptorDto();
        String param = "b76b2520-2573-4337-9bf8-fbd9b41aecc9";
        source.setName(param);
        mapper.doMapping(source, target);
        assertEquals(target.getName(), param);
    }

    @Test
    public void testDescription() {
        LicenseAgreementEntity source = new LicenseAgreementEntity();
        LicenseAgreementDescriptorDto target = new LicenseAgreementDescriptorDto();
        MapLicenseAgreementEntityToLicenseAgreementDescriptorDto mapper =
                new MapLicenseAgreementEntityToLicenseAgreementDescriptorDto();
        String param = "1a9f859d-0644-450d-9cda-0caced655b94";
        source.setDescription(param);
        mapper.doMapping(source, target);
        assertEquals(target.getDescription(), param);
    }

    @Test
    public void testRequirementsAndConstrains() {
        LicenseAgreementEntity source = new LicenseAgreementEntity();
        LicenseAgreementDescriptorDto target = new LicenseAgreementDescriptorDto();
        MapLicenseAgreementEntityToLicenseAgreementDescriptorDto mapper =
                new MapLicenseAgreementEntityToLicenseAgreementDescriptorDto();
        String param = "1efaca15-4d8a-422c-a33a-7b57f9aaa99c";
        source.setRequirementsAndConstrains(param);
        mapper.doMapping(source, target);
        assertEquals(target.getRequirementsAndConstrains(), param);
    }
}
