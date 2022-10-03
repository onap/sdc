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

package org.openecomp.sdc.be.servlets;

import static org.mockito.Mockito.mock;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.openecomp.sdc.be.components.impl.DistributionMonitoringBusinessLogic;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.user.UserBusinessLogic;

public class DistributionServiceServletTest {

    private DistributionServiceServlet createTestSubject() {
        UserBusinessLogic userBusinessLogic = mock(UserBusinessLogic.class);
        ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
        DistributionMonitoringBusinessLogic distributionMonitoringLogic = mock(DistributionMonitoringBusinessLogic.class);
        return new DistributionServiceServlet(componentsUtils,
            distributionMonitoringLogic);
    }


    @Test
    public void testGetServiceById() throws Exception {
        DistributionServiceServlet testSubject;
        String serviceUUID = "";
        HttpServletRequest request = null;
        String userId = "";
        Response result;

        // default test
        testSubject = createTestSubject();
    }


    @Test
    public void testGetListOfDistributionStatuses() throws Exception {
        DistributionServiceServlet testSubject;
        String did = "";
        HttpServletRequest request = null;
        String userId = "";
        Response result;

        // default test
        testSubject = createTestSubject();
    }


    @Test
    public void testInit() throws Exception {
        DistributionServiceServlet testSubject;
        HttpServletRequest request = null;

        // default test
        testSubject = createTestSubject();
    }


    @Test
    public void testGetDistributionBL() throws Exception {
        DistributionServiceServlet testSubject;
        ServletContext context = null;
        DistributionMonitoringBusinessLogic result;

        // default test
        testSubject = createTestSubject();
    }
}
