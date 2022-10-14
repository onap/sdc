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

package org.openecomp.sdc.itempermissions.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.itempermissions.dao.impl.PermissionsServicesImpl;

@ExtendWith(MockitoExtension.class)
class PermissionsRulesImplTest {

    private static final String ITEM1_ID = "1";
    private static final String USER1_ID = "testUser1";
    private static final String PERMISSION_OWNER = "Owner";
    private static final String PERMISSION_CONTRIBUTOR = "Contributor";
    private static final String INVALID_PERMISSION = "Invalid_Permission";
    private static final String SUBMIT_ACTION = "Submit_Item";
    private static final String EDIT_ACTION = "Edit_Item";
    private static final String CHANGE_PERMISSIONS_ACTION = "Change_Item_Permissions";
    private static final String INVALID_ACTION = "Invalid_Action";

    @Mock
    private PermissionsServicesImpl permissionsServices;

    @InjectMocks
    @Spy
    private PermissionsRulesImpl permissionsRules;

    @Test
    void testIsAllowedWhenInvalidPermission() {
        Assertions.assertThrows(CoreException.class, () -> {
            permissionsRules.isAllowed(INVALID_PERMISSION, EDIT_ACTION);
        });
    }

    @Test
    void testIsAllowedWhenInvalidAction() {
        Assertions.assertThrows(CoreException.class, () -> {
            permissionsRules.isAllowed(PERMISSION_CONTRIBUTOR, INVALID_ACTION);
        });
    }

    @Test
    void testIsAllowedCaseSubmitOwner() {
        assertTrue(permissionsRules.isAllowed(PERMISSION_OWNER, SUBMIT_ACTION));
    }

    @Test
    void testIsAllowedCaseSubmitNotOwner() {
        assertTrue(permissionsRules.isAllowed(PERMISSION_CONTRIBUTOR, SUBMIT_ACTION));
    }

    @Test
    void testIsAllowedCaseEditOwner() {
        assertTrue(permissionsRules.isAllowed(PERMISSION_OWNER, EDIT_ACTION));
    }

    @Test
    void testIsAllowedCaseEditContributer() {
        assertTrue(permissionsRules.isAllowed(PERMISSION_CONTRIBUTOR, EDIT_ACTION));
    }

    @Test
    void testIsAllowedCaseChangePermissionsContributer() {
        assertFalse(permissionsRules.isAllowed(PERMISSION_CONTRIBUTOR, CHANGE_PERMISSIONS_ACTION));
    }

    @Test
    void testIsAllowedCaseChangePermissionsOwner() {
        assertTrue(permissionsRules.isAllowed(PERMISSION_OWNER, CHANGE_PERMISSIONS_ACTION));
    }

    @Test
    void testUpdatePermissionWhenInvalidPermission() {
        final HashSet<String> stringHashSet = new HashSet<>();
        Assertions.assertThrows(CoreException.class, () -> {
            permissionsRules.updatePermission(ITEM1_ID, USER1_ID, INVALID_PERMISSION, stringHashSet, stringHashSet);
        });
    }

    @Test
    void testExecuteActionInvalidAction() {
        Assertions.assertThrows(CoreException.class, () -> {
            permissionsRules.executeAction(ITEM1_ID, USER1_ID, INVALID_ACTION);
        });
    }

}
