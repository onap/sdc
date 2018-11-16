/*

 * Copyright (c) 2018 Huawei Intellectual Property.

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *     http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */
package org.openecomp.sdc.fe.config;

import org.junit.Test;

public class ConnectionTest {

    private Connection createTestSubject() {
        return new Connection();
    }

    @Test
    public void testGetUrl() {
        Connection testSubject;
        String result;

        testSubject = createTestSubject();
        result = testSubject.getUrl();
    }

    @Test
    public void testSetUrl() {
        Connection testSubject;

        testSubject = createTestSubject();
        testSubject.setUrl("http://test");
    }


    @Test
    public void testGetPoolSize() {
        Connection testSubject;
        Integer result;

        testSubject = createTestSubject();
        result = testSubject.getPoolSize();
    }

    @Test
    public void testSetPoolSize() {
        Connection testSubject;

        testSubject = createTestSubject();
        testSubject.setPoolSize(10);
    }

    @Test
    public void testToString() {
        Connection testSubject;

        testSubject = createTestSubject();
        testSubject.toString();
    }


}
