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

import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.container.TimeoutHandler;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.servlets.BasicServlet;
import org.openecomp.sdc.fe.config.Configuration;
import org.openecomp.sdc.fe.impl.DesignerStatusBL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Root resource (exposed at "/" path)
 */
@Path("/config")
public class ConfigServlet extends BasicServlet {

	private static final long serialVersionUID = 1L;
	private static Logger log = LoggerFactory.getLogger(ConfigServlet.class.getName());

	//@GET
	//@Path("/get")
	//@Produces(MediaType.APPLICATION_JSON)
	public String getConfig(@Context final HttpServletRequest request) {

		String result = null;

		ServletContext context = request.getSession().getServletContext();

		ConfigurationSource configurationSource = (ConfigurationSource) context
				.getAttribute(Constants.CONFIGURATION_SOURCE_ATTR);
		if (configurationSource != null) {
			Configuration configuration = configurationSource.getAndWatchConfiguration(Configuration.class, null);

			if (configuration == null) {
				log.warn("Configuration of type {} was not found", Configuration.class);
			}
			log.debug("{}", configuration);
			if (log.isInfoEnabled()) {
				log.info("Info level ENABLED...");
			}
			log.info("The value returned from getConfig is {}", configuration);

			result = gson.toJson(configuration);

		} else {
			log.warn("Source Configuration object was not initialized in the context.");
		}

		return result;

	}

	//@GET
	//@Path("/asyncget")
	public void asyncGet(@Suspended final AsyncResponse asyncResponse) {

		asyncResponse.setTimeoutHandler(new TimeoutHandler() {

			@Override
			public void handleTimeout(AsyncResponse asyncResponse) {
				asyncResponse.resume(
						Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("Operation time out.").build());
			}
		});
		asyncResponse.setTimeout(3, TimeUnit.SECONDS);

		new Thread(new Runnable() {
			@Override
			public void run() {
				String result = veryExpensiveOperation();
				asyncResponse.resume(result);
			}

			private String veryExpensiveOperation() {

				return "veryExpensiveOperation SUCCESS";

			}
		}).start();
	}

	@GET
	@Path("/ui/designers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDesignersConfiguration(@Context final HttpServletRequest request) {
		String result = null;

		ServletContext context = request.getSession().getServletContext();

		DesignerStatusBL designerStatusBL = (DesignerStatusBL) context.getAttribute(Constants.DESIGNER_BL_COMPONENT);		

		result = designerStatusBL.checkDesinerListAvailability();

		return Response.status(Status.OK).entity(result).build();

	}	
}
