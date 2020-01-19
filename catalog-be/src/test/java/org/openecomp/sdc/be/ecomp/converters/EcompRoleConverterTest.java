/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import org.junit.Test;
import org.onap.portalsdk.core.restful.domain.EcompRole;
import org.openecomp.sdc.be.user.Role;

public class EcompRoleConverterTest {

	@Test
	public void testConvertEcompRoleToRole() throws Exception {
		EcompRole ecompRole = new EcompRole();
		String result;

		// test 1
		for (Role iterable_element : Role.values()) {
			ecompRole.setName(iterable_element.name());
			EcompRoleConverter.convertEcompRoleToRole(ecompRole);
		}
		
		EcompRoleConverter.convertEcompRoleToRole(null);
		
		ecompRole.setId(new Long(4523535));
		EcompRoleConverter.convertEcompRoleToRole(ecompRole);
	}
}
