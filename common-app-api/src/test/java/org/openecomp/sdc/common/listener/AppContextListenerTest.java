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
package org.openecomp.sdc.common.listener;

import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import org.junit.Test;

public class AppContextListenerTest {

    private AppContextListener createTestSubject() {
        return new AppContextListener();
    }

    @Test
    public void testContextDestroyed() throws Exception {
        AppContextListener testSubject;
        ServletContextEvent context = null;
        // default test
        testSubject = createTestSubject();
        testSubject.contextDestroyed(context);
    }

    //	@Test
    public void testGetManifestInfo() throws Exception {
        ServletContext application = null;
        Map<String, String> result;
        // default test
        result = AppContextListener.getManifestInfo(application);
    }
}
