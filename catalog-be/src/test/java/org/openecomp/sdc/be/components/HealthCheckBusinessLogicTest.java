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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.be.components;

import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.health.HealthCheckBusinessLogic;
import org.openecomp.sdc.be.switchover.detector.SwitchoverDetector;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
public class HealthCheckBusinessLogicTest {

    private final SwitchoverDetector switchoverDetector = Mockito.mock(SwitchoverDetector.class);

    HealthCheckBusinessLogic healthCheckBusinessLogic = new HealthCheckBusinessLogic();

    @Test
    public void checkStausUpdated() {

        boolean statusChanged = healthCheckBusinessLogic.anyStatusChanged(null, null);
        assertFalse("check false", statusChanged);

        List<HealthCheckInfo> checkInfosLeft = new ArrayList<>();
        List<HealthCheckInfo> checkInfosRight = new ArrayList<>();

        statusChanged = healthCheckBusinessLogic.anyStatusChanged(checkInfosLeft, checkInfosRight);
        assertFalse("check false", statusChanged);

        HealthCheckInfo checkInfoJanusGraphUp = new HealthCheckInfo(Constants.HC_COMPONENT_JANUSGRAPH, HealthCheckStatus.UP, null, null);
        HealthCheckInfo checkInfoJanusGraphDown = new HealthCheckInfo(Constants.HC_COMPONENT_JANUSGRAPH, HealthCheckStatus.DOWN, null, null);

        HealthCheckInfo checkInfoDmaapProducerUp = new HealthCheckInfo(Constants.HC_COMPONENT_DMAAP_PRODUCER, HealthCheckStatus.UP, null, null);

        HealthCheckInfo healthCheckInfoCADIUp = new HealthCheckInfo(Constants.HC_COMPONENT_CADI, HealthCheckStatus.UP, null, null);

        /*
         * HealthCheckInfo checkInfoUebUp = new HealthCheckInfo(HealthCheckComponent.DE, HealthCheckStatus.UP, null, null); HealthCheckInfo checkInfoUebDown = new HealthCheckInfo(HealthCheckComponent.DE, HealthCheckStatus.DOWN, null, null);
         */

        checkInfosLeft.add(checkInfoJanusGraphUp);

        checkInfosRight.add(checkInfoJanusGraphUp);

        checkInfosLeft.add(checkInfoDmaapProducerUp);

        checkInfosRight.add(checkInfoDmaapProducerUp);

        checkInfosLeft.add(healthCheckInfoCADIUp);

        checkInfosRight.add(healthCheckInfoCADIUp);

        statusChanged = healthCheckBusinessLogic.anyStatusChanged(checkInfosLeft, checkInfosRight);
        assertFalse("check false", statusChanged);

        checkInfosRight.remove(checkInfoJanusGraphUp);
        statusChanged = healthCheckBusinessLogic.anyStatusChanged(checkInfosLeft, checkInfosRight);
        assertTrue("check true", statusChanged);

        checkInfosRight.add(checkInfoJanusGraphDown);
        statusChanged = healthCheckBusinessLogic.anyStatusChanged(checkInfosLeft, checkInfosRight);
        assertTrue("check true", statusChanged);

        checkInfosRight.remove(checkInfoJanusGraphDown);
        statusChanged = healthCheckBusinessLogic.anyStatusChanged(checkInfosLeft, checkInfosRight);
        assertTrue("check true", statusChanged);

        checkInfosRight.add(checkInfoJanusGraphUp);
        statusChanged = healthCheckBusinessLogic.anyStatusChanged(checkInfosLeft, checkInfosRight);
        assertFalse("check false", statusChanged);

        checkInfosRight.remove(healthCheckInfoCADIUp);
        statusChanged = healthCheckBusinessLogic.anyStatusChanged(checkInfosLeft, checkInfosRight);
        assertTrue("check true", statusChanged);

        checkInfosRight.add(healthCheckInfoCADIUp);
        statusChanged = healthCheckBusinessLogic.anyStatusChanged(checkInfosLeft, null);
        assertTrue("check true", statusChanged);

    }

}
