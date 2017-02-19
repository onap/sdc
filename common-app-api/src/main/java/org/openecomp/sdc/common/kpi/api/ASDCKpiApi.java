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

package org.openecomp.sdc.common.kpi.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ASDCKpiApi {

	private static Logger log = LoggerFactory.getLogger(ASDCKpiApi.class.getName());

	/* Number of activated resource imports. */
	public static void countImportResourcesKPI() {
		// TODO Auto-generated method stub
		log.trace("Number of  activated resource imports.");

	}

	/* Number of created resources. */
	public static void countCreatedResourcesKPI() {
		// TODO Auto-generated method stub
		log.trace("Number of  created resources.");

	}

	/* Number of created services */
	public static void countCreatedServicesKPI() {
		// TODO Auto-generated method stub
		log.trace("Number of created services.");

	}

	/*
	 * Number of ASDC portal accesses ( number of activated user authorizations)
	 */
	public static void countUsersAuthorizations() {
		// TODO Auto-generated method stub
		log.trace("Number of activated distribution");

	}

	/* Number of activated distribution */
	public static void countActivatedDistribution() {
		// TODO Auto-generated method stub
		log.trace("Number of activated distribution");

	}

}
