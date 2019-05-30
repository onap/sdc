/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Samsung. All rights reserved.
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

package org.openecomp.sdc.fe.servlets;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.common.api.Constants.HEALTH_CHECK_SERVICE_ATTR;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FeHealthCheckServletTest {

    private final FeHealthCheckServlet healthCheckServlet = new FeHealthCheckServlet();
    private final Response response = Response.status(200).entity("Ok").build();

    @Mock
    private HealthCheckService healthCheckService;

    @Mock
    private ServletContext servletContext;

    @Mock
    private HttpSession session;

    @Mock
    private HttpServletRequest request;

    @Test
    public void testGetFEandBeHealthCheck() {
        // given
        when(healthCheckService.getFeHealth()).thenReturn(response);
        when(servletContext.getAttribute(eq(HEALTH_CHECK_SERVICE_ATTR))).thenReturn(healthCheckService);
        when(session.getServletContext()).thenReturn(servletContext);
        when(request.getSession()).thenReturn(session);

        // when
        final Response healthCheck = healthCheckServlet.getFEandBeHealthCheck(request);

        // then
        assertEquals(response, healthCheck);
    }
}
