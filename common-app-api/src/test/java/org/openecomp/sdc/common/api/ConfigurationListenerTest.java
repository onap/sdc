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
package org.openecomp.sdc.common.api;

import org.junit.Test;

public class ConfigurationListenerTest {

    private ConfigurationListener createTestSubject() {
        return new ConfigurationListener(null, null);
    }

    @Test
    public void testGetType() throws Exception {
        ConfigurationListener testSubject;
        Class<? extends BasicConfiguration> result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getType();
    }

    @Test
    public void testSetType() throws Exception {
        ConfigurationListener testSubject;
        Class<? extends BasicConfiguration> type = null;
        // default test
        testSubject = createTestSubject();
        testSubject.setType(type);
    }

    @Test
    public void testGetCallBack() throws Exception {
        ConfigurationListener testSubject;
        FileChangeCallback result;
        // default test
        testSubject = createTestSubject();
        result = testSubject.getCallBack();
    }

    @Test
    public void testSetCallBack() throws Exception {
        ConfigurationListener testSubject;
        FileChangeCallback callBack = null;
        // default test
        testSubject = createTestSubject();
        testSubject.setCallBack(callBack);
    }
}
