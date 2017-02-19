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

package org.openecomp.sdc.be.servlets;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.servlets.BasicServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcabi.aspects.Loggable;

/**
 * Root resource (exposed at "/" path)
 */
@Loggable(prepend = true, value = Loggable.DEBUG, trim = false)
@Path("/config")
public class ConfigServlet extends BasicServlet {

	private static Logger log = LoggerFactory.getLogger(ConfigServlet.class.getName());

	@GET
	@Path("/get")
	@Produces(MediaType.APPLICATION_JSON)
	public String getConfig(@Context final HttpServletRequest request) {

		String result = null;

		ServletContext context = request.getSession().getServletContext();

		ConfigurationSource configurationSource = (ConfigurationSource) context.getAttribute(Constants.CONFIGURATION_SOURCE_ATTR);
		if (configurationSource != null) {
			Configuration configuration = configurationSource.getAndWatchConfiguration(Configuration.class, null);

			if (configuration == null) {
				log.warn("Configuration of type {} was not found", Configuration.class);
			}
			log.debug("{}", configuration);
			if (log.isInfoEnabled()) {
				log.debug("Info level ENABLED...");
			}
			log.info("The value returned from getConfig is {}", configuration);

			result = gson.toJson(configuration);

		} else {
			log.warn("Source Configuration object was not initialized in the context.");
		}

		return result;

	}

}
