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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.openecomp.sdc.be.components.impl.AdditionalInformationBusinessLogic;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.user.UserBusinessLogic;


public class AdditionalInformationServletTest {

    private AdditionalInformationServlet createTestSubject() {
        UserBusinessLogic userBusinessLogic = mock(UserBusinessLogic.class);
        ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
        AdditionalInformationBusinessLogic additionalInformationBusinessLogic =
            mock(AdditionalInformationBusinessLogic.class);
        return new AdditionalInformationServlet(componentsUtils,
            additionalInformationBusinessLogic);
    }


    @Test
    public void testCreateResourceAdditionalInformationLabel() throws Exception {
        AdditionalInformationServlet testSubject;
        String resourceId = "";
        String data = "";
        HttpServletRequest request = null;
        String userUserId = "";
        Response result;

        // default test
        testSubject = createTestSubject();

    }


    @Test
    public void testCreateServiceAdditionalInformationLabel() throws Exception {
        AdditionalInformationServlet testSubject;
        String serviceId = "";
        String data = "";
        HttpServletRequest request = null;
        String userUserId = "";
        Response result;

        // default test
        testSubject = createTestSubject();

    }


    @Test
    public void testUpdateResourceAdditionalInformationLabel() throws Exception {
        AdditionalInformationServlet testSubject;
        String resourceId = "";
        String labelId = "";
        String data = "";
        HttpServletRequest request = null;
        String userId = "";
        Response result;

        // default test
        testSubject = createTestSubject();

    }


    @Test
    public void testUpdateServiceAdditionalInformationLabel() throws Exception {
        AdditionalInformationServlet testSubject;
        String serviceId = "";
        String labelId = "";
        String data = "";
        HttpServletRequest request = null;
        String userId = "";
        Response result;

        // default test
        testSubject = createTestSubject();

    }


    @Test
    public void testUpdateResourceAdditionalInformationLabel_1() throws Exception {
        AdditionalInformationServlet testSubject;
        String resourceId = "";
        String labelId = "";
        HttpServletRequest request = null;
        String userId = "";
        Response result;

        // default test
        testSubject = createTestSubject();

    }


    @Test
    public void testDeleteServiceAdditionalInformationLabel() throws Exception {
        AdditionalInformationServlet testSubject;
        String serviceId = "";
        String labelId = "";
        HttpServletRequest request = null;
        String userId = "";
        Response result;

        // default test
        testSubject = createTestSubject();

    }


    @Test
    public void testGetResourceAdditionalInformationLabel() throws Exception {
        AdditionalInformationServlet testSubject;
        String resourceId = "";
        String labelId = "";
        HttpServletRequest request = null;
        String userId = "";
        Response result;

        // default test
        testSubject = createTestSubject();

    }


    @Test
    public void testGetServiceAdditionalInformationLabel() throws Exception {
        AdditionalInformationServlet testSubject;
        String serviceId = "";
        String labelId = "";
        HttpServletRequest request = null;
        String userId = "";
        Response result;

        // default test
        testSubject = createTestSubject();

    }


    @Test
    public void testGetAllResourceAdditionalInformationLabel() throws Exception {
        AdditionalInformationServlet testSubject;
        String resourceId = "";
        HttpServletRequest request = null;
        String userId = "";
        Response result;

        // default test
        testSubject = createTestSubject();

    }


    @Test
    public void testGetAllServiceAdditionalInformationLabel() throws Exception {
        AdditionalInformationServlet testSubject;
        String serviceId = "";
        HttpServletRequest request = null;
        String userId = "";
        Response result;

        // default test
        testSubject = createTestSubject();

    }


}
