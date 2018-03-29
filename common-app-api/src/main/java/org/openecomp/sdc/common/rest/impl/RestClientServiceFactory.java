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

package org.openecomp.sdc.common.rest.impl;

import org.openecomp.sdc.common.rest.api.IRestClient;
import org.openecomp.sdc.common.rest.api.RestConfigurationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestClientServiceFactory {

	private static Logger log = LoggerFactory.getLogger(RestClientServiceFactory.class.getName());

	public static IRestClient createRestClientService(RestConfigurationInfo restConfigurationInfo) {

		log.trace("Enter createRestClientService");

		HttpRestClientServiceImpl restClientServiceImpl = new HttpRestClientServiceImpl();

		boolean result = restClientServiceImpl.init(restConfigurationInfo);
		if (result == false) {
			return null;
		}

		log.trace("Exit createRestClientService");

		return restClientServiceImpl;
	}

}
