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

package org.onap.sdc.backend.ci.tests.execute.category;

import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.be.model.User;
import org.onap.sdc.backend.ci.tests.api.ComponentBaseTest;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;

public abstract class CategoriesBaseTest extends ComponentBaseTest {

	protected static final String AUDIT_SERVICE_TYPE = "Service";
	protected static final String AUDIT_RESOURCE_TYPE = "Resource";
	protected static final String AUDIT_PRODUCT_TYPE = "Product";
	protected static final String GET_CATEGORY_HIERARCHY = "GetCategoryHierarchy";
	protected static User sdncAdminUserDetails = new ElementFactory().getDefaultUser(UserRoleEnum.ADMIN);
	protected static User sdncAdminUserDetails1 = new ElementFactory().getDefaultUser(UserRoleEnum.ADMIN);
	protected static User sdncDesignerUserDetails = new ElementFactory().getDefaultUser(UserRoleEnum.DESIGNER);
	protected static User sdncTesterUserDetails = new ElementFactory().getDefaultUser(UserRoleEnum.TESTER);
	protected static User sdncGovernorUserDetails = new ElementFactory().getDefaultUser(UserRoleEnum.GOVERNOR);
	protected static User sdncOpsUserDetails = new ElementFactory().getDefaultUser(UserRoleEnum.OPS);
	protected static User sdncProductManagerUserDetails = new ElementFactory().getDefaultUser(UserRoleEnum.PRODUCT_MANAGER1);
	protected static User sdncProductStrategistUserDetails = new ElementFactory()
			.getDefaultUser(UserRoleEnum.PRODUCT_STRATEGIST1);

}
