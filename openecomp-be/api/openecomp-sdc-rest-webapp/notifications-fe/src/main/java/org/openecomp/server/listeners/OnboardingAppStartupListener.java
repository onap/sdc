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

package org.openecomp.server.listeners;


import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class OnboardingAppStartupListener implements ServletContextListener {

  ContextLoaderListener springListener;

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    springListener = new ContextLoaderListener();
    springListener.initWebApplicationContext(servletContextEvent.getServletContext());
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    springListener.closeWebApplicationContext(servletContextEvent.getServletContext());
  }
}
