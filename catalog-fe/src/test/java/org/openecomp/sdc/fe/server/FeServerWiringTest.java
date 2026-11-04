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

package org.openecomp.sdc.fe.server;

import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.fe.wiring.WiringSnapshot;

class FeServerWiringTest {

    @Test
    void serverIsWiredAsSnapshotted() throws Exception {
        WiringSnapshot.assertMatches(ServerSnapshot.render(FeServer.create(new Properties())), "server.txt");
    }

    @Test
    void catalogContextIsWiredAsSnapshotted() throws Exception {
        WiringSnapshot.assertMatches(ServerSnapshot.render(FeServer.catalogContext()), "sdc1-context.txt");
    }

    @Test
    void onboardingContextIsWiredAsSnapshotted() throws Exception {
        WiringSnapshot.assertMatches(ServerSnapshot.render(FeServer.onboardingContext()), "onboarding-context.txt");
    }
}
