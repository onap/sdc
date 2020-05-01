/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020, Nordix Foundation. All rights reserved.
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

package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.common.session.impl.AsdcSessionContextProvider;
import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageInfoDao;

@ExtendWith(MockitoExtension.class)
class PackageInfoDaoFactoryImplTest {

    private static final String USER_ID = "cs0008";

    @InjectMocks
    private AsdcSessionContextProvider asdcSessionContextProvider;

    @BeforeEach
    void setUp() {
        asdcSessionContextProvider.create(USER_ID, "");
    }

    @Disabled
    // TODO - recheck after https://gerrit.onap.org/r/c/sdc/+/106825
    @Test
    void createInterface() {
        final PackageInfoDao testSubject = PackageInfoDaoFactoryImpl.getInstance().createInterface();
        assertNotNull(testSubject);
        assertSame(PackageInfoDaoImpl.class, testSubject.getClass());
    }
}
