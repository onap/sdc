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

package org.onap.sdc.frontend.ci.tests.utilities;

import org.onap.sdc.frontend.ci.tests.pages.AdminGeneralPage;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;

public class AdminWorkspaceUIUtilies {

    private AdminWorkspaceUIUtilies() {

    }

    public static void createNewUser(String userId, UserRoleEnum userRole) {
        AdminGeneralPage.getUserManagementTab().setNewUserBox(userId);
        AdminGeneralPage.getUserManagementTab().selectUserRole(userRole);
        AdminGeneralPage.getUserManagementTab().clickCreateButton();
    }

    public static void updateUserRole(int rowIndx, UserRoleEnum userRole) {
        AdminGeneralPage.getUserManagementTab().updateUser(rowIndx);
        AdminGeneralPage.getUserManagementTab().updateUserRole(userRole, rowIndx);
        AdminGeneralPage.getUserManagementTab().saveAfterUpdateUser(rowIndx);
    }

    public static void deleteFirstRow() {
        AdminGeneralPage.getUserManagementTab().deleteUser(0);
    }

    public static void searchForUser(String searchString) {
        AdminGeneralPage.getUserManagementTab().searchUser(searchString);
    }


}
