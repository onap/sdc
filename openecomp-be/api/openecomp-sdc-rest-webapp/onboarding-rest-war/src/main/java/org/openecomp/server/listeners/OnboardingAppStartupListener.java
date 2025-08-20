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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.openecomp.sdc.common.session.SessionContextProviderFactory;

public class OnboardingAppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println(">>> OnboardingAppStartupListener initializing <<<");
        SessionContextProviderFactory.getInstance().createInterface().create("onboarding", "dox");
        System.out.println(">>> OnboardingAppStartupListener initialized <<<");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println(">>> OnboardingAppStartupListener destroying <<<");
        SessionContextProviderFactory.getInstance().createInterface().close();
        System.out.println(">>> OnboardingAppStartupListener destroyed <<<");
    }
}
