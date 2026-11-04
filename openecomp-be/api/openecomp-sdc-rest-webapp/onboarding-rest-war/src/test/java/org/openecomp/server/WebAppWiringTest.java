/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

package org.openecomp.server;

import java.util.Properties;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.jupiter.api.Test;
import org.openecomp.server.wiring.WiringSnapshot;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;

class WebAppWiringTest {

    @Test
    void serverIsWiredAsSnapshotted() throws Exception {
        JettyServletWebServerFactory factory = new JettyServletWebServerFactory();
        OnboardingBeServer.customize(factory);
        Server server = new Server(factory.getThreadPool());
        WebAppContext context = new WebAppContext();
        context.setContextPath(factory.getContextPath());
        server.setHandler(context);
        OnboardingBeServer.configure(server, new Properties());
        WiringSnapshot.assertMatches(ServerSnapshot.render(server), "server.txt");
    }

    @Test
    void webAppIsWiredAsSnapshotted() throws Exception {
        WebAppContext context = new WebAppContext();
        context.clearAliasChecks();
        new OnboardingBeWebApp().configure(context);
        WiringSnapshot.assertMatches(ServerSnapshot.render(context), "webapp.txt");
    }
}
