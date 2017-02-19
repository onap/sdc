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

package org.openecomp.sdc;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.Container.Listener;
import org.eclipse.jetty.webapp.WebAppContext;
import org.openecomp.sdc.fe.listen.FEAppContextListener;

public class Main {
	public static void main(String[] args) throws Exception {
		// The port that we should run on can be set into an environment
		// variable
		// Look for that variable and default to 8080 if it isn't there.
		String webPort = System.getenv("PORT");
		if (webPort == null || webPort.isEmpty()) {
			webPort = "8080";
		}
		// String webPort = "9998";

		final Server server = new Server(Integer.valueOf(webPort));
		server.addEventListener((Listener) new FEAppContextListener());
		final WebAppContext root = new WebAppContext();

		root.setContextPath("/sdc1");
		// Parent loader priority is a class loader setting that Jetty accepts.
		// By default Jetty will behave like most web containers in that it will
		// allow your application to replace non-server libraries that are part
		// of the
		// container. Setting parent loader priority to true changes this
		// behavior.
		// Read more here:
		// http://wiki.eclipse.org/Jetty/Reference/Jetty_Classloading
		root.setParentLoaderPriority(true);

		final String webappDirLocation = "src/main/webapp/";
		root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
		root.setResourceBase(webappDirLocation);

		server.setHandler(root);

		server.start();
		server.join();
	}
}
