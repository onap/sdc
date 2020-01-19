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

package org.openecomp.sdc.common.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.common.api.HealthCheckInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckUtilTest {

    private HealthCheckUtil healthCheckUtil;

    private final String testComponent = "service";

    @Mock
    private HealthCheckInfo healthCheckInfo;

    private List<HealthCheckInfo> healthCheckInfos;

    @Before
    public void setUp() {
        healthCheckUtil = new HealthCheckUtil();
        healthCheckInfos = Collections.singletonList(healthCheckInfo);
        when(healthCheckInfo.getHealthCheckComponent()).thenReturn(testComponent);
    }

    @Test
    public void validateGetAggregateStatusReturnsTrue() {
        final Collection<String> excludes = Collections.emptyList();
        when(healthCheckInfo.getHealthCheckStatus()).thenReturn(HealthCheckInfo.HealthCheckStatus.UP);

        final boolean result = healthCheckUtil.getAggregateStatus(healthCheckInfos, excludes);

        assertTrue(result);
    }

    @Test
    public void validateGetAggregateStatusReturnsFalseIfStatusIsDown() {
        final Collection<String> excludes = Collections.emptyList();
        when(healthCheckInfo.getHealthCheckStatus()).thenReturn(HealthCheckInfo.HealthCheckStatus.DOWN);

        final boolean result = healthCheckUtil.getAggregateStatus(healthCheckInfos, excludes);

        assertFalse(result);
    }

    @Test
    public void validateGetAggregateDescriptionReturnsProperDescription() {
        when(healthCheckInfo.getHealthCheckStatus()).thenReturn(HealthCheckInfo.HealthCheckStatus.DOWN);

        final String result = healthCheckUtil.getAggregateDescription(healthCheckInfos );

        assertTrue(result.contains(testComponent));
        assertTrue(result.contains("Down"));
    }

}
