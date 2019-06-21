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

import javax.servlet.ServletContext;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckServiceTest {


    @Mock
    private ServletContext context;

    private final HealthCheckService healthCheckService = new HealthCheckService(context);
    private final Response response = Response.status(500).entity("{}").build();


    @Test
    public void testGetFeHealth() {
        //given
        Response feHealth = healthCheckService.getFeHealth();

        //then
        assertEquals(response.getEntity(), feHealth.getEntity());
        assertEquals(response.getStatus(), feHealth.getStatus());
    }

    @Test
    public void testGetLastHealthStatus() {
        //given
        HealthCheckService.HealthStatus healthStatus = healthCheckService.getLastHealthStatus();

        //then
        assertEquals(response.getEntity(), healthStatus.getBody());
        assertEquals(response.getStatus(), healthStatus.getStatusCode());
    }

    @Test
    public void testGetTask () {
        //given
        HealthCheckService.HealthCheckScheduledTask healthCheckScheduledTask = healthCheckService.getTask();
        HealthCheckService.HealthStatus  healthStatus = healthCheckScheduledTask.checkHealth();

        //then
        assertEquals(response.getStatus(),healthStatus.getStatusCode());
    }
}
