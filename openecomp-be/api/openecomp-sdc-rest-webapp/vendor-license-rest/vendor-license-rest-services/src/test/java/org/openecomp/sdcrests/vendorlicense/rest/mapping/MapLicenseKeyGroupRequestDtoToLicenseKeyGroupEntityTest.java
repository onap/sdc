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
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyType;
import org.openecomp.sdc.vendorlicense.dao.types.OperationalScope;
import org.openecomp.sdc.vendorlicense.dao.types.ThresholdUnit;
import org.openecomp.sdcrests.vendorlicense.types.LicenseKeyGroupRequestDto;
import org.openecomp.sdcrests.vendorlicense.types.MultiChoiceOrOtherDto;

import static org.testng.Assert.assertEquals;


public class MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntityTest {


    @Test
    public void testExpiryDate() {
        LicenseKeyGroupRequestDto source = new LicenseKeyGroupRequestDto();
        LicenseKeyGroupEntity target = new LicenseKeyGroupEntity();
        MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity mapper =
                new MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity();
        String param = "77d85090-e40d-49d0-ae18-b6aff51728e3";
        source.setExpiryDate(param);
        mapper.doMapping(source, target);
        assertEquals(target.getExpiryDate(), param);
    }

    @Test
    public void testThresholdUnits() {
        LicenseKeyGroupRequestDto source = new LicenseKeyGroupRequestDto();
        LicenseKeyGroupEntity target = new LicenseKeyGroupEntity();
        MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity mapper =
                new MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity();
        ThresholdUnit thresholdUnit = ThresholdUnit.Absolute;
        source.setThresholdUnits(thresholdUnit);
        mapper.doMapping(source, target);
        assertEquals(target.getThresholdUnits(), thresholdUnit);
    }

    @Test
    public void testName() {
        LicenseKeyGroupRequestDto source = new LicenseKeyGroupRequestDto();
        LicenseKeyGroupEntity target = new LicenseKeyGroupEntity();
        MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity mapper =
                new MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity();
        String param = "74ee9b26-9a6c-48a4-b3ec-f9a0fe174bc9";
        source.setName(param);
        mapper.doMapping(source, target);
        assertEquals(target.getName(), param);
    }

    @Test
    public void testDescription() {
        LicenseKeyGroupRequestDto source = new LicenseKeyGroupRequestDto();
        LicenseKeyGroupEntity target = new LicenseKeyGroupEntity();
        MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity mapper =
                new MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity();
        String param = "a7aaa0f0-bd68-41b0-a27e-d4e62b1bd6e5";
        source.setDescription(param);
        mapper.doMapping(source, target);
        assertEquals(target.getDescription(), param);
    }

    @Test
    public void testThresholdValue() {
        LicenseKeyGroupRequestDto source = new LicenseKeyGroupRequestDto();
        LicenseKeyGroupEntity target = new LicenseKeyGroupEntity();
        MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity mapper =
                new MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity();
        Integer param = -2136417915;
        source.setThresholdValue(param);
        mapper.doMapping(source, target);
        assertEquals(target.getThresholdValue(), param);
    }

    @Test
    public void testType() {
        LicenseKeyGroupRequestDto source = new LicenseKeyGroupRequestDto();
        LicenseKeyGroupEntity target = new LicenseKeyGroupEntity();
        MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity mapper =
                new MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity();
        LicenseKeyType licenseKeyType = LicenseKeyType.Unique;
        source.setType(licenseKeyType);
        mapper.doMapping(source, target);
        assertEquals(target.getType(), licenseKeyType);
    }

    @Test
    public void testIncrements() {
        LicenseKeyGroupRequestDto source = new LicenseKeyGroupRequestDto();
        LicenseKeyGroupEntity target = new LicenseKeyGroupEntity();
        MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity mapper =
                new MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity();
        String param = "97f20c3a-6f3a-42cf-aa1a-870ba436bde6";
        source.setIncrements(param);
        mapper.doMapping(source, target);
        assertEquals(target.getIncrements(), param);
    }

    @Test
    public void testOperationalScope() {
        LicenseKeyGroupRequestDto source = new LicenseKeyGroupRequestDto();
        LicenseKeyGroupEntity target = new LicenseKeyGroupEntity();
        MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity mapper =
                new MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity();
        MultiChoiceOrOtherDto<OperationalScope> operationalScopeMultiChoiceOrOtherDto = new MultiChoiceOrOtherDto<>();
        source.setOperationalScope(operationalScopeMultiChoiceOrOtherDto);
        mapper.doMapping(source, target);
        assertEquals(target.getOperationalScope().getChoices(), operationalScopeMultiChoiceOrOtherDto.getChoices());
    }

    @Test
    public void testStartDate() {
        LicenseKeyGroupRequestDto source = new LicenseKeyGroupRequestDto();
        LicenseKeyGroupEntity target = new LicenseKeyGroupEntity();
        MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity mapper =
                new MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity();
        String param = "9aa18de7-ca80-4690-b604-2e0994424f2d";
        source.setStartDate(param);
        mapper.doMapping(source, target);
        assertEquals(target.getStartDate(), param);
    }

    @Test
    public void testManufacturerReferenceNumber() {
        LicenseKeyGroupRequestDto source = new LicenseKeyGroupRequestDto();
        LicenseKeyGroupEntity target = new LicenseKeyGroupEntity();
        MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity mapper = new
            MapLicenseKeyGroupRequestDtoToLicenseKeyGroupEntity();
        String param = "02402e1e-7092-485a-9574-46e2d49cca97";
        source.setManufacturerReferenceNumber(param);
        mapper.doMapping(source, target);
        assertEquals(target.getManufacturerReferenceNumber(), param);
    }
}
