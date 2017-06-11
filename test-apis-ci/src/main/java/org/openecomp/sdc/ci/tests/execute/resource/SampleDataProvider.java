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

package org.openecomp.sdc.ci.tests.execute.resource;

import java.io.IOException;

import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

public class SampleDataProvider {

	@DataProvider
	public static Object[][] getResourceByType(ITestContext context) throws IOException, Exception {
		return new Object[][] {
				{ AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true) },
				{ AtomicOperationUtils.createResourceByType(ResourceTypeEnum.CP, UserRoleEnum.DESIGNER, true) },
				{ AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VL, UserRoleEnum.DESIGNER, true) } };
	}

}
