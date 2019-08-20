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

package org.openecomp.sdcrests.health.rest.mapping;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.health.data.HealthCheckStatus;
import org.openecomp.sdc.health.data.HealthInfo;
import org.openecomp.sdc.health.data.MonitoredModules;
import org.openecomp.sdcrests.health.types.HealthInfoDto;
import org.openecomp.sdcrests.health.types.HealthInfoDtos;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MapHealthCheckInfoToDtoTest {

    private MapHealthCheckInfoToDto mapHealthCheckInfoToDto;

    @Before
    public void setUp() {
        mapHealthCheckInfoToDto = new MapHealthCheckInfoToDto();
    }

    @Test
    public void validateDoMappingCorrectMapsHealthInfoToHealthInfoDto() {

        final HealthInfo healthInfo = new HealthInfo(MonitoredModules.BE, HealthCheckStatus.UP, "1.0", "test");
        List<HealthInfo> testSource = Collections.singletonList(healthInfo);

        HealthInfoDtos testTarget = new HealthInfoDtos();

        mapHealthCheckInfoToDto.doMapping(testSource ,testTarget);

        final HealthInfo sourceElement = testSource.get(0);
        final HealthInfoDto responceElement = testTarget.getHealthInfos().get(0);
        assertEquals(sourceElement.getDescription(),responceElement.getDescription());
        assertEquals(sourceElement.getHealthCheckStatus().toString(),responceElement.getHealthStatus().toString());
        assertEquals(sourceElement.getVersion(),responceElement.getVersion());
        assertEquals(sourceElement.getHealthCheckComponent().toString(),responceElement.getHealthCheckComponent().toString());
    }
}
