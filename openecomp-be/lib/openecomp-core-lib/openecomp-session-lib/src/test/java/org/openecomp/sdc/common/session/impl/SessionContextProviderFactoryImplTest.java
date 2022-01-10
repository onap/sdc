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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.common.session.SessionContextProvider;

@ExtendWith(MockitoExtension.class)
class SessionContextProviderFactoryImplTest {

    @InjectMocks
    private SessionContextProviderFactoryImpl sessionContextProviderFactoryImpl;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateInterface() {
        SessionContextProvider sessionContextProvider = sessionContextProviderFactoryImpl.createInterface();

        assertTrue(sessionContextProvider instanceof AsdcSessionContextProvider);
    }
}
