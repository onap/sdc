/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import org.openecomp.sdc.health.data.HealthInfo;
import org.openecomp.sdcrests.health.types.HealthCheckStatus;
import org.openecomp.sdcrests.health.types.HealthInfoDto;
import org.openecomp.sdcrests.health.types.HealthInfoDtos;
import org.openecomp.sdcrests.health.types.MonitoredModules;
import org.openecomp.sdcrests.mapping.MappingBase;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Talio on 8/10/2016.
 */
public class MapHealthCheckInfoToDto
        extends MappingBase<Collection<HealthInfo>, HealthInfoDtos> {
    @Override
    public void doMapping(Collection<HealthInfo> source, HealthInfoDtos target) {

        List<HealthInfoDto> healthInfos = source.stream()
                .map(healthInfo -> new HealthInfoDto(
                        MonitoredModules.toValue(healthInfo.getHealthCheckComponent().toString()),
                        HealthCheckStatus.valueOf(healthInfo.getHealthCheckStatus().toString()),
                        healthInfo.getVersion(), healthInfo.getDescription())).collect(Collectors.toList());
        target.setHealthInfos(healthInfos);
    }
}
