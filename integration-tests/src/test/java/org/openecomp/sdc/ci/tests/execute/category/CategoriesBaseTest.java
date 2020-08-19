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

package org.openecomp.sdc.ci.tests.execute.category;

import org.junit.rules.TestName;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;

public abstract class CategoriesBaseTest extends ComponentBaseTest {

	public CategoriesBaseTest(TestName testName, String className) {
		super(testName, className);
	}

	protected static final String AUDIT_SERVICE_TYPE = "Service";
	protected static final String AUDIT_RESOURCE_TYPE = "Resource";
	protected static final String AUDIT_PRODUCT_TYPE = "Product";
	protected static final String GET_CATEGORY_HIERARCHY = "GetCategoryHierarchy";
	protected static User sdncAdminUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
	protected static User sdncAdminUserDetails1 = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
	protected static User sdncDesignerUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
	protected static User sdncTesterUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
	protected static User sdncGovernorUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.GOVERNOR);
	protected static User sdncOpsUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.OPS);
	protected static User sdncProductManagerUserDetails = ElementFactory.getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1);
	protected static User sdncProductStrategistUserDetails = ElementFactory
			.getDefaultUser(UserRoleEnum.PRODUCT_STRATEGIST1);

}
