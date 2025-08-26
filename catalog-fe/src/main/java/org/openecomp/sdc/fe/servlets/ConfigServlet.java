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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.exception.NotFoundException;
import org.openecomp.sdc.fe.config.ConfigurationManager;
import org.openecomp.sdc.fe.config.FeEcompErrorManager;
import org.openecomp.sdc.fe.config.WorkspaceConfiguration;
import org.openecomp.sdc.fe.impl.PluginStatusBL;
import org.owasp.esapi.ESAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("rest/config")
public class ConfigServlet extends LoggingServlet {

    public static final String UNEXPECTED_FE_RESPONSE_LOGGING_ERROR = "Unexpected FE response logging error :";
    public static final String ERROR_FE_RESPONSE = "FE Response";
    private static final Logger log = LoggerFactory.getLogger(ConfigServlet.class.getName());

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @GetMapping("/ui/workspace")
    public ResponseEntity<?> getUIWorkspaceConfiguration(HttpServletRequest request) {
        try {
            logFeRequest(request);
            ServletContext context = request.getSession().getServletContext();
            ConfigurationManager configurationManager =
                    (ConfigurationManager) context.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR);

            WorkspaceConfiguration configuration = configurationManager.getWorkspaceConfiguration();
            if (configuration == null) {
                throw new NotFoundException(WorkspaceConfiguration.class.getSimpleName());
            }
            log.info("The value returned from getConfig is {}", configuration);
            String result = gson.toJson(configuration);

            ResponseEntity<String> response = ResponseEntity.ok(result);
            logFeResponse(request, response);
            return response;
        } catch (Exception e) {
            FeEcompErrorManager.getInstance().logFeHttpLoggingError(ERROR_FE_RESPONSE);
            log.error(UNEXPECTED_FE_RESPONSE_LOGGING_ERROR, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{}");
        }
    }

    @GetMapping("/ui/plugins")
    public ResponseEntity<?> getPluginsConfiguration(HttpServletRequest request) {
        try {
            logFeRequest(request);
            ServletContext context = request.getSession().getServletContext();
            PluginStatusBL pluginStatusBL =
                    (PluginStatusBL) context.getAttribute(Constants.PLUGIN_BL_COMPONENT);

            String result = pluginStatusBL.getPluginsList();
            ResponseEntity<String> response = ResponseEntity.ok(result);
            logFeResponse(request, response);
            return response;
        } catch (Exception e) {
            FeEcompErrorManager.getInstance().logFeHttpLoggingError(ERROR_FE_RESPONSE);
            log.error(UNEXPECTED_FE_RESPONSE_LOGGING_ERROR, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{}");
        }
    }

    @GetMapping("/ui/plugins/{pluginId}/online")
    public ResponseEntity<?> getPluginOnlineState(
            @PathVariable String pluginId, HttpServletRequest request) {
        try {
            logFeRequest(request);
            pluginId = ESAPI.encoder().encodeForHTML(pluginId);

            ServletContext context = request.getSession().getServletContext();
            PluginStatusBL pluginStatusBL =
                    (PluginStatusBL) context.getAttribute(Constants.PLUGIN_BL_COMPONENT);

            String result = pluginStatusBL.getPluginAvailability(pluginId);
            if (result == null) {
                log.debug("Plugin with pluginId: {} was not found in the configuration", pluginId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Plugin with pluginId: \"" + pluginId + "\" was not found in the configuration");
            }

            ResponseEntity<String> response = ResponseEntity.ok(result);
            logFeResponse(request, response);
            return response;
        } catch (Exception e) {
            FeEcompErrorManager.getInstance().logFeHttpLoggingError(ERROR_FE_RESPONSE);
            log.error(UNEXPECTED_FE_RESPONSE_LOGGING_ERROR, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{}");
        }
    }

    protected void inHttpRequest(HttpServletRequest httpRequest) {
        log.info("{} {} {}", httpRequest.getMethod(), httpRequest.getRequestURI(), httpRequest.getProtocol());
    }

    protected void outHttpResponse(ResponseEntity<?> response) {
        log.info("SC=\"{}\"", response.getStatusCodeValue());
    }
}
