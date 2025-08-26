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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.rest.api.RestConfigurationInfo;
import org.openecomp.sdc.common.servlets.BasicServlet;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



/**
 * Root resource (exposed at "/" path)
 */
@RestController
@RequestMapping("/configmgr")
public class ConfigMgrServlet extends BasicServlet {

    private static final Logger log = Logger.getLogger(ConfigMgrServlet.class.getName());

    @GetMapping("/get")
    public String getConfig(HttpServletRequest request,
                            @RequestParam(value = "type", required = false) String type) {

        String result = null;

        ServletContext context = request.getSession().getServletContext();
        ConfigurationManager configurationManager = 
                (ConfigurationManager) context.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR);

        if (type == null || type.equals("configuration")) {
            Configuration configuration = configurationManager.getConfiguration();
            if (configuration == null) {
                log.warn("Configuration of type {} was not found", Configuration.class);
            } else {
                log.info("The value returned from getConfig is {}", configuration);
                result = gson.toJson(configuration);
            }
        } else if (type.equals("rest")) {
            RestConfigurationInfo configuration = configurationManager.getRestClientConfiguration();
            if (configuration == null) {
                log.warn("Configuration of type {} was not found", RestConfigurationInfo.class);
            } else {
                log.info("The value returned from getConfig is {}", configuration);
                result = gson.toJson(configuration);
            }
        }
        return result;
    }
}
