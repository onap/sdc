/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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

package org.openecomp.sdcrests.conflict.rest.mapping;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openecomp.conflicts.types.Conflict;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdcrests.conflict.types.ConflictDto;
import static org.junit.Assert.assertEquals;

public class MapConflictToDtoTest {

    private MapConflictToDto mapConflictToDto;

    private final String testId = "testId";
    private final String testName = "testName";

    @Before
    public void setUp() {
        mapConflictToDto = new MapConflictToDto();
    }

    @Test
    public void validDoMappingIsCorrectForNetworkEntity() {
        final NetworkEntity testYoursNetworkEntity = new NetworkEntity();
        testYoursNetworkEntity.setId("YoursId");
        final NetworkEntity testTheirsNetworkEntity = new NetworkEntity();
        testTheirsNetworkEntity.setId("TheirsId");
        final Conflict<NetworkEntity> testConflict = new Conflict<>(testId, ElementType.Network, testName);
        testConflict.setYours(testYoursNetworkEntity);
        testConflict.setTheirs(testTheirsNetworkEntity);
        final ConflictDto testConflictDto = new ConflictDto();

        mapConflictToDto.doMapping(testConflict, testConflictDto);

        assertEquals(testConflict.getId(), testConflictDto.getId());
        assertEquals(testConflict.getName(), testConflictDto.getName());
        assertEquals(testConflict.getType(), testConflictDto.getType());
        assertEquals(testConflict.getYours().getId(), testConflictDto.getYours().get("id"));
        assertEquals(testConflict.getTheirs().getId(), testConflictDto.getTheirs().get("id"));
    }

    @Test
    public void validDoMappingIsCorrectForLicenseAgreement() {
        final LicenseAgreementEntity testYoursLicenseAgreementEntity = new LicenseAgreementEntity();
        testYoursLicenseAgreementEntity.setName("YoursName");
        final LicenseAgreementEntity testTheirsLicenseAgreementEntity = new LicenseAgreementEntity();
        testTheirsLicenseAgreementEntity.setName("TheirsName");
        final Conflict<LicenseAgreementEntity> testConflict = new Conflict<>(testId, ElementType.LicenseAgreement, testName);
        testConflict.setYours(testYoursLicenseAgreementEntity);
        testConflict.setTheirs(testTheirsLicenseAgreementEntity);
        final ConflictDto testConflictDto = new ConflictDto();

        mapConflictToDto.doMapping(testConflict, testConflictDto);

        assertEquals(testConflict.getId(), testConflictDto.getId());
        assertEquals(testConflict.getName(), testConflictDto.getName());
        assertEquals(testConflict.getType(), testConflictDto.getType());
        assertEquals(testConflict.getYours().getName(), testConflictDto.getYours().get("name"));
        assertEquals(testConflict.getTheirs().getName(), testConflictDto.getTheirs().get("name"));
    }
}
