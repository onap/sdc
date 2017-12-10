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

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Application;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.Mockito;

public class ApplicationConfig extends Application {

	public Set<Class<?>> getClasses() {
		final Set<Class<?>> resources = new HashSet<Class<?>>();

		// Add your resources.
		resources.add(ResourceUploadServlet.class);
		resources.add(MultiPart.class);
		resources.add(FormDataContentDisposition.class);

		final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

		ResourceConfig resourceConfig = ResourceConfig.forApplication(this);

		resourceConfig.register(new AbstractBinder() {

			@Override
			protected void configure() {
				// TODO Auto-generated method stub
				bind(request).to(HttpServletRequest.class);
			}
		});

		return resources;
	}

}
