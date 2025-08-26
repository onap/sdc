/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import static org.openecomp.sdc.common.api.Constants.HEALTH_CHECK_SERVICE_ATTR;

import com.jcabi.aspects.Loggable;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.openecomp.sdc.common.servlets.BasicServlet;
import org.openecomp.sdc.fe.impl.HealthCheckService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Loggable(prepend = true, value = Loggable.TRACE, trim = false)
@RestController
@RequestMapping("rest/healthCheck")
public class FeHealthCheckServlet extends BasicServlet {

    @GetMapping
    public ResponseEntity<?> getFEandBeHealthCheck(HttpServletRequest request) {
        ServletContext context = request.getSession().getServletContext();
        HealthCheckService hcs = (HealthCheckService) context.getAttribute(HEALTH_CHECK_SERVICE_ATTR);
        return hcs.getFeHealth();
    }
}
