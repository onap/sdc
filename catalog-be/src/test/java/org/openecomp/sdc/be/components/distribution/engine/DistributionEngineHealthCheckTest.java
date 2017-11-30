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

package org.openecomp.sdc.be.components.distribution.engine;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.BaseConfDependentTest;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DistributionEngineHealthCheckTest extends BaseConfDependentTest {

	@Mock
	private CambriaHandler cambriaHandler = Mockito.mock(CambriaHandler.class);

	DistributionEngineClusterHealth distributionEngineClusterHealth = new DistributionEngineClusterHealth();

	Gson gson = new Gson();

	Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

	//
	// @Test
	// public void validateDownWhenEnvAreDown() {
	//
	// Map<String, AtomicBoolean> envNamePerStatus = new HashMap<>();
	// envNamePerStatus.put("PROD1", new AtomicBoolean(false));
	// envNamePerStatus.put("PROD2", new AtomicBoolean(false));
	//
	// distributionEngineClusterHealth.startHealthCheckTask(envNamePerStatus);
	//
	// HealthCheckInfo healthCheckInfo =
	// distributionEngineClusterHealth.getHealthCheckInfo();
	// assertEquals("verify down", HealthCheckStatus.DOWN,
	// healthCheckInfo.getHealthCheckStatus());
	// assertEquals("verify DE component", HealthCheckComponent.DE,
	// healthCheckInfo.getHealthCheckComponent());
	//
	// }

	@Test
	public void validateUpWhenQuerySucceed() {

		// Map<String, AtomicBoolean> envNamePerStatus = new HashMap<>();
		// envNamePerStatus.put("PROD1", new AtomicBoolean(true));
		// envNamePerStatus.put("PROD2", new AtomicBoolean(false));
		//
		// distributionEngineClusterHealth.startHealthCheckTask(envNamePerStatus,
		// false);

		CambriaErrorResponse cambriaOkResponse = new CambriaErrorResponse(CambriaOperationStatus.OK, 200);
		CambriaErrorResponse cambriaErrorResponse = new CambriaErrorResponse(CambriaOperationStatus.INTERNAL_SERVER_ERROR, 500);
		CambriaErrorResponse cambriaNotErrorResponse = new CambriaErrorResponse(CambriaOperationStatus.AUTHENTICATION_ERROR, 403);

		List<String> uebServers = ConfigurationManager.getConfigurationManager().getDistributionEngineConfiguration().getUebServers();
		if (uebServers.size() >= 2) {
			when(cambriaHandler.getApiKey(Mockito.eq(uebServers.get(0)), Mockito.any(String.class))).thenReturn(cambriaOkResponse);
			when(cambriaHandler.getApiKey(Mockito.eq(uebServers.get(1)), Mockito.any(String.class))).thenReturn(cambriaOkResponse);
		}

		UebHealthCheckCall healthCheckCall1 = new UebHealthCheckCall(uebServers.get(0), "publicKey");
		healthCheckCall1.setCambriaHandler(cambriaHandler);
		Boolean call1 = healthCheckCall1.call();
		assertTrue("check response okay", call1);

		UebHealthCheckCall healthCheckCall2 = new UebHealthCheckCall(uebServers.get(1), "publicKey");
		healthCheckCall2.setCambriaHandler(cambriaHandler);

		Boolean call2 = healthCheckCall2.call();
		assertTrue("check response okay", call2);

		if (uebServers.size() >= 2) {
			when(cambriaHandler.getApiKey(Mockito.eq(uebServers.get(0)), Mockito.any(String.class))).thenReturn(cambriaErrorResponse);
			when(cambriaHandler.getApiKey(Mockito.eq(uebServers.get(1)), Mockito.any(String.class))).thenReturn(cambriaOkResponse);
		}
		healthCheckCall1 = new UebHealthCheckCall(uebServers.get(0), "publicKey");
		healthCheckCall1.setCambriaHandler(cambriaHandler);

		call1 = healthCheckCall1.call();
		assertFalse("check response okay", call1);

		healthCheckCall2 = new UebHealthCheckCall(uebServers.get(1), "publicKey");
		healthCheckCall2.setCambriaHandler(cambriaHandler);

		call2 = healthCheckCall2.call();
		assertTrue("check response okay", call2);

		if (uebServers.size() >= 2) {
			when(cambriaHandler.getApiKey(Mockito.eq(uebServers.get(0)), Mockito.any(String.class))).thenReturn(cambriaErrorResponse);
			when(cambriaHandler.getApiKey(Mockito.eq(uebServers.get(1)), Mockito.any(String.class))).thenReturn(cambriaNotErrorResponse);
		}
		healthCheckCall1 = new UebHealthCheckCall(uebServers.get(0), "publicKey");
		healthCheckCall1.setCambriaHandler(cambriaHandler);

		call1 = healthCheckCall1.call();
		assertFalse("check response okay", call1);

		healthCheckCall2 = new UebHealthCheckCall(uebServers.get(1), "publicKey");
		healthCheckCall2.setCambriaHandler(cambriaHandler);

		call2 = healthCheckCall2.call();
		assertTrue("check response okay", call2);

	}

}
