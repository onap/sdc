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

package org.openecomp.sdc.be.components;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openecomp.sdc.be.components.impl.HealthCheckBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;
public class HealthCheckBusinessLogicTest {

	HealthCheckBusinessLogic healthCheckBusinessLogic = new HealthCheckBusinessLogic();
	
	@Test
	public void checkStausUpdated() {

		boolean statusChanged = healthCheckBusinessLogic.anyStatusChanged(null, null);
		assertFalse("check false", statusChanged);

		List<HealthCheckInfo> checkInfosLeft = new ArrayList<HealthCheckInfo>();
		List<HealthCheckInfo> checkInfosRight = new ArrayList<HealthCheckInfo>();

		statusChanged = healthCheckBusinessLogic.anyStatusChanged(checkInfosLeft, checkInfosRight);
		assertFalse("check false", statusChanged);

		HealthCheckInfo checkInfoTitanUp = new HealthCheckInfo(Constants.HC_COMPONENT_TITAN, HealthCheckStatus.UP, null, null);
		HealthCheckInfo checkInfoTitanDown = new HealthCheckInfo(Constants.HC_COMPONENT_TITAN, HealthCheckStatus.DOWN, null, null);

		/*
		 * HealthCheckInfo checkInfoUebUp = new HealthCheckInfo(HealthCheckComponent.DE, HealthCheckStatus.UP, null, null); HealthCheckInfo checkInfoUebDown = new HealthCheckInfo(HealthCheckComponent.DE, HealthCheckStatus.DOWN, null, null);
		 */

		checkInfosLeft.add(checkInfoTitanUp);

		checkInfosRight.add(checkInfoTitanUp);

		statusChanged = healthCheckBusinessLogic.anyStatusChanged(checkInfosLeft, checkInfosRight);
		assertFalse("check false", statusChanged);

		checkInfosRight.remove(checkInfoTitanUp);
		statusChanged = healthCheckBusinessLogic.anyStatusChanged(checkInfosLeft, checkInfosRight);
		assertTrue("check true", statusChanged);

		checkInfosRight.add(checkInfoTitanDown);
		statusChanged = healthCheckBusinessLogic.anyStatusChanged(checkInfosLeft, checkInfosRight);
		assertTrue("check true", statusChanged);

		checkInfosRight.remove(checkInfoTitanDown);
		statusChanged = healthCheckBusinessLogic.anyStatusChanged(checkInfosLeft, checkInfosRight);
		assertTrue("check true", statusChanged);

		checkInfosRight.add(checkInfoTitanUp);
		statusChanged = healthCheckBusinessLogic.anyStatusChanged(checkInfosLeft, checkInfosRight);
		assertFalse("check false", statusChanged);

		statusChanged = healthCheckBusinessLogic.anyStatusChanged(checkInfosLeft, null);
		assertTrue("check true", statusChanged);

	}

}
