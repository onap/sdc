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

package org.openecomp.sdc.be.ecomp.converters;

import org.openecomp.portalsdk.core.restful.domain.EcompRole;
import org.openecomp.sdc.be.user.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EcompRoleConverter {

	private static Logger log = LoggerFactory.getLogger(EcompRoleConverter.class.getName());

	private EcompRoleConverter() {
	}

	// TODO Add Either or Exception in case of convertation failure
	public static String convertEcompRoleToRole(EcompRole ecompRole) {

		log.debug("converting role");
		if (ecompRole == null) {
			log.debug("recieved null for roles");
			return null;
		}

		for (Role role : Role.values()) {
			if (role.ordinal() == ecompRole.getId()) {
				return role.name();
			}
		}
		log.debug("no roles converted");
		return null;
	}
}
