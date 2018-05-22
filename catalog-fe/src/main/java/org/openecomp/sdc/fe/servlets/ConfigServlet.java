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

import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.fe.config.FeEcompErrorManager;
import org.openecomp.sdc.fe.impl.PluginStatusBL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Root resource (exposed at "/" path)
 */
@Path("/config")
public class ConfigServlet extends LoggingServlet {

	private static final Logger log = LoggerFactory.getLogger(ConfigServlet.class.getName());


	@GET
	@Path("/ui/plugins")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPluginsConfiguration(@Context final HttpServletRequest request) {

		try {
			logFeRequest(request);

			ServletContext context = request.getSession().getServletContext();

			PluginStatusBL pluginStatusBL = (PluginStatusBL) context.getAttribute(Constants.PLUGIN_BL_COMPONENT);

            String result = pluginStatusBL.checkPluginsListAvailability();

			Response response = Response.status(Status.OK).entity(result).build();

			logFeResponse(request, response);

			return response;
		} catch (Exception e) {
			FeEcompErrorManager.getInstance().logFeHttpLoggingError("FE Response");
			log.error("Unexpected FE response logging error :", e);
			return Response.status(Status.OK).entity(null).build();
		}

	}

    protected  void inHttpRequest(HttpServletRequest httpRequest) {
        log.info("{} {} {}", httpRequest.getMethod(), httpRequest.getRequestURI(), httpRequest.getProtocol());
    }

    /**
     * Extracted for purpose of clear method name, for logback %M parameter
     * @param response http response
     */
    protected void outHttpResponse(Response response) {
        log.info("SC=\"{}\"", response.getStatus());
    }
}
