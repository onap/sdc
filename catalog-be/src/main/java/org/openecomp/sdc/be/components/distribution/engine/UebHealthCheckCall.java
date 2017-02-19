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

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UebHealthCheckCall implements Callable<Boolean> {

	CambriaHandler cambriaHandler = new CambriaHandler();

	String server;
	String publicApiKey;

	private static Logger healthLogger = LoggerFactory.getLogger(DistributionEngineClusterHealth.UEB_HEALTH_LOG_CONTEXT);

	private static Logger logger = LoggerFactory.getLogger(UebHealthCheckCall.class.getName());

	public UebHealthCheckCall(String server, String publicApiKey) {
		super();
		this.server = server;
		this.publicApiKey = publicApiKey;
	}

	@Override
	public Boolean call() {

		healthLogger.trace("Going to run health check towards ueb server {}", server);

		boolean result = false;
		CambriaErrorResponse cambriaErrorResponse = cambriaHandler.getApiKey(server, publicApiKey);

		logger.debug("After running Health check towards ueb server {}. Result is {}", server, cambriaErrorResponse);

		if (cambriaErrorResponse.httpCode < CambriaErrorResponse.HTTP_INTERNAL_SERVER_ERROR) {
			logger.debug("After running Health check towards ueb server {}. Error code is {}. Set result to true", server, cambriaErrorResponse.httpCode);
			result = true;
		}

		healthLogger.trace("Result after running health check towards ueb server {} is {}. Returned result is {} ", server, cambriaErrorResponse, result);

		return result;
	}

	public String getServer() {
		return server;
	}

	public CambriaHandler getCambriaHandler() {
		return cambriaHandler;
	}

	public void setCambriaHandler(CambriaHandler cambriaHandler) {
		this.cambriaHandler = cambriaHandler;
	}

}
