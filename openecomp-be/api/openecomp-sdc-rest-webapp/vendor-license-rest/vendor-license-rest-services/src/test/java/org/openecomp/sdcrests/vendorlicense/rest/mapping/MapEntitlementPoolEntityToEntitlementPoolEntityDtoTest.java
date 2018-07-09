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

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther;
import org.openecomp.sdc.vendorlicense.dao.types.OperationalScope;
import org.openecomp.sdcrests.vendorlicense.types.EntitlementPoolEntityDto;


public class MapEntitlementPoolEntityToEntitlementPoolEntityDtoTest {

    @Test
    public void testReferencingFeatureGroups() {
        EntitlementPoolEntity source = new EntitlementPoolEntity();
        EntitlementPoolEntityDto target = new EntitlementPoolEntityDto();
        MapEntitlementPoolEntityToEntitlementPoolEntityDto mapper =
                new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
        Set<String> param = new HashSet<>(Collections.singletonList("a3ad0c7a-9f4c-44d2-8c0c-b4b47cd6d264"));
        source.setReferencingFeatureGroups(param);
        mapper.doMapping(source, target);
        assertEquals(target.getReferencingFeatureGroups(), param);
    }

    @Test
    public void testDescription() {
        EntitlementPoolEntity source = new EntitlementPoolEntity();
        EntitlementPoolEntityDto target = new EntitlementPoolEntityDto();
        MapEntitlementPoolEntityToEntitlementPoolEntityDto mapper =
                new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
        String param = "58191782-5de2-4d42-b8ca-119707d38150";
        source.setDescription(param);
        mapper.doMapping(source, target);
        assertEquals(target.getDescription(), param);
    }

    @Test
    public void testIncrements() {
        EntitlementPoolEntity source = new EntitlementPoolEntity();
        EntitlementPoolEntityDto target = new EntitlementPoolEntityDto();
        MapEntitlementPoolEntityToEntitlementPoolEntityDto mapper =
                new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
        String param = "ea6b2eab-f959-41c2-9a09-2898eb5401be";
        source.setIncrements(param);
        mapper.doMapping(source, target);
        assertEquals(target.getIncrements(), param);
    }

    @Test
    public void testExpiryDate() {
        EntitlementPoolEntity source = new EntitlementPoolEntity();
        EntitlementPoolEntityDto target = new EntitlementPoolEntityDto();
        MapEntitlementPoolEntityToEntitlementPoolEntityDto mapper =
                new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
        String param = "7e27d6a4-78be-44df-b099-dd41317586ba";
        source.setExpiryDate(param);
        mapper.doMapping(source, target);
        assertEquals(target.getExpiryDate(), param);
    }

    @Test
    public void testId() {
        EntitlementPoolEntity source = new EntitlementPoolEntity();
        EntitlementPoolEntityDto target = new EntitlementPoolEntityDto();
        MapEntitlementPoolEntityToEntitlementPoolEntityDto mapper =
                new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
        String param = "e2163b3f-971e-4523-a5bc-0e6f7dd44e37";
        source.setId(param);
        mapper.doMapping(source, target);
        assertEquals(target.getId(), param);
    }

    @Test
    public void testThresholdValue() {
        EntitlementPoolEntity source = new EntitlementPoolEntity();
        EntitlementPoolEntityDto target = new EntitlementPoolEntityDto();
        MapEntitlementPoolEntityToEntitlementPoolEntityDto mapper =
                new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
        Integer param = 2146099790;
        source.setThresholdValue(param);
        mapper.doMapping(source, target);
        assertEquals(target.getThresholdValue(), param);
    }

    @Test
    public void testName() {
        EntitlementPoolEntity source = new EntitlementPoolEntity();
        EntitlementPoolEntityDto target = new EntitlementPoolEntityDto();
        MapEntitlementPoolEntityToEntitlementPoolEntityDto mapper =
                new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
        mapper.doMapping(source, target);
        assertEquals(target.getName(), source.getName());
        String param = "f8faa28a-435d-4cea-98ee-de7a46b52ec5";
        source.setName(param);
        mapper.doMapping(source, target);
        assertEquals(target.getName(), param);
    }

    @Test
    public void testOperationalScope() {
        EntitlementPoolEntity source = new EntitlementPoolEntity();
        EntitlementPoolEntityDto target = new EntitlementPoolEntityDto();
        MapEntitlementPoolEntityToEntitlementPoolEntityDto mapper =
                new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
        MultiChoiceOrOther<OperationalScope> param = new MultiChoiceOrOther<>();
        param.setChoices(new HashSet(Arrays.asList("a", "b")));
        source.setOperationalScope(param);
        mapper.doMapping(source, target);
        assertEquals(target.getOperationalScope().getChoices(), param.getChoices());
    }

    @Test
    public void testStartDate() {
        EntitlementPoolEntity source = new EntitlementPoolEntity();
        EntitlementPoolEntityDto target = new EntitlementPoolEntityDto();
        MapEntitlementPoolEntityToEntitlementPoolEntityDto mapper =
                new MapEntitlementPoolEntityToEntitlementPoolEntityDto();
        String param = "afeadea1-9fb7-4d2d-bcc3-3ef298ed1802";
        source.setStartDate(param);
        mapper.doMapping(source, target);
        assertEquals(target.getStartDate(), param);
    }

    @Test
    public void testManufacturerReferenceNumber() {
        EntitlementPoolEntity source = new EntitlementPoolEntity();
        EntitlementPoolEntityDto target = new EntitlementPoolEntityDto();
        MapEntitlementPoolEntityToEntitlementPoolEntityDto mapper = new
            MapEntitlementPoolEntityToEntitlementPoolEntityDto();

        String param = "02402e1e-7092-485a-9574-46e2d49cca97";
        source.setManufacturerReferenceNumber(param);
        mapper.doMapping(source, target);
        assertEquals(target.getManufacturerReferenceNumber(), param);
    }
}
