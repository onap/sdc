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

import fj.data.Either;
import org.junit.Test;
import org.onap.portalsdk.core.restful.domain.EcompUser;
import org.openecomp.sdc.be.model.User;

public class EcompUserConverterTest {

	@Test
	public void testConvertUserToEcompUser() throws Exception {
		User asdcUser = new User();
		Either<EcompUser, String> result;

		// test 1
		result = EcompUserConverter.convertUserToEcompUser(asdcUser);
	}

	@Test
	public void testConvertEcompUserToUser() throws Exception {
		EcompUser ecompUser = new EcompUser();
		User result;

		// test 1
		result = EcompUserConverter.convertEcompUserToUser(ecompUser);
	}
}
