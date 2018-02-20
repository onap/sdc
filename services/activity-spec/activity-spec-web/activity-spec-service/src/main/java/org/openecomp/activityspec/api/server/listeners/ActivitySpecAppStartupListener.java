/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.activityspec.api.server.listeners;

import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import static org.openecomp.activityspec.utils.ActivitySpecConstant.TENANT;
import static org.openecomp.activityspec.utils.ActivitySpecConstant.USER;

public class ActivitySpecAppStartupListener implements ServletContextListener {

  ContextLoaderListener springListener;

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    SessionContextProviderFactory.getInstance().createInterface().create(USER,
        TENANT);
    springListener = new ContextLoaderListener();
    springListener.initWebApplicationContext(servletContextEvent.getServletContext());
    SessionContextProviderFactory.getInstance().createInterface().create(USER,
        TENANT);
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    springListener.closeWebApplicationContext(servletContextEvent.getServletContext());
  }
}
