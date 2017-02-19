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

package org.openecomp.sdc.be.dao.utils;

/**
 * Holds constants to use in ALIEN.
 */
public final class Constants {
	public static final int DEFAULT_ES_SEARCH_SIZE = 50;
	public static final int MAX_ES_SEARCH_SIZE = 100;

	public static final String ALIEN_INTERNAL_TAG = "icon";
	public static final String DEFAULT_CAPABILITY_FIELD_NAME = "defaultCapabilities";

	public static final String GROUP_NAME_ALL_USERS = "ALL_USERS";
	public static final String GRAPH_EMPTY_VALUE = "__NANANA__";

	private Constants() {
	}
}
