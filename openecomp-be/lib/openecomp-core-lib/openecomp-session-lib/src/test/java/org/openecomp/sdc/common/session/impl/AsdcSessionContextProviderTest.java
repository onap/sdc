/*
 *
 *  Copyright © 2017-2018 European Support Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 *
 */

package org.openecomp.sdc.common.session.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.session.SessionContext;

@ExtendWith(MockitoExtension.class)
class AsdcSessionContextProviderTest {

    private static final String USER_ID = "cs0008";

    @InjectMocks
    private AsdcSessionContextProvider asdcSessionContextProvider;

    @Test
    void testGetUserIdNull() {
        asdcSessionContextProvider.create(null, null);
        Assertions.assertThrows(CoreException.class, () -> {
            asdcSessionContextProvider.get();
        });
    }

    @Test
    void testGetTenantNull() {
        asdcSessionContextProvider.create(USER_ID, null);
        Assertions.assertThrows(CoreException.class, () -> {
            asdcSessionContextProvider.get();
        });
    }

    @Test
    void testGet() {
        asdcSessionContextProvider.create(USER_ID, "tenant");
        SessionContext sessionContext = asdcSessionContextProvider.get();

        assertNotNull(sessionContext);
        assertSame(USER_ID, sessionContext.getUser().getUserId());
        assertSame("tenant", sessionContext.getTenant());
    }
}
