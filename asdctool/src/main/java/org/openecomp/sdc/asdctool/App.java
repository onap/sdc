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

package org.openecomp.sdc.asdctool;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {

		String asdcToolPort = "8087";

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/asdctool");

		Server jettyServer = new Server(Integer.valueOf(asdcToolPort));
		jettyServer.setHandler(context);

		ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
		jerseyServlet.setInitOrder(0);

		// Tells the Jersey Servlet which REST service/class to load.
		// jerseyServlet.setInitParameter("jersey.config.server.provider.classnames",
		// EntryPoint.class.getCanonicalName());
		jerseyServlet.setInitParameter("jersey.config.server.provider.packages", "org.openecomp.sdc.asdctool.servlets");
		jerseyServlet.setInitParameter("jersey.config.server.provider.classnames",
				"org.glassfish.jersey.media.multipart.MultiPartFeature");

		try {
			jettyServer.start();

			System.out.println("Server was started on port " + asdcToolPort);

			jettyServer.join();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			jettyServer.destroy();
		}
	}
}
