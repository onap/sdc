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

package org.openecomp.sdc.common.test.config;

import static java.lang.String.format;

import org.openecomp.sdc.common.api.BasicConfiguration;

public class TestNotExistConfiguration extends BasicConfiguration {

	/**
	 * backend host
	 */
	private String beHost;
	/**
	 * backend http port
	 */
	private Integer beHttpPort;

	/**
	 * backend http secured port
	 */

	@Override
	public String toString() {
		return new StringBuilder().append(format("backend host: %s\n", beHost))
				.append(format("backend http port: %s\n", beHttpPort)).toString();
	}
}
