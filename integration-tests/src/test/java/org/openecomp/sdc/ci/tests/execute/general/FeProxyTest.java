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

package org.openecomp.sdc.ci.tests.execute.general;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.CategoryRestUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class FeProxyTest extends ComponentBaseTest {

	@Rule
	public static TestName name = new TestName();

	public FeProxyTest() {
		super(name, FeProxyTest.class.getName());
	}

	@Test
	public void testFeProxy() throws Exception {
		User defaultUser = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		RestResponse allCategoriesTowardsFe = CategoryRestUtils.getAllCategoriesTowardsFe(defaultUser,
				BaseRestUtils.RESOURCE_COMPONENT_TYPE);
		AssertJUnit.assertEquals("Check response code after get categories towards FE", 200,
				allCategoriesTowardsFe.getErrorCode().intValue());
	}

}
