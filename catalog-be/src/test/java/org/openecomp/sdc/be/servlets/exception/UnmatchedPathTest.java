/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright © 2026 Deutsche Telekom. All rights reserved.
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

package org.openecomp.sdc.be.servlets.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.servlets.JerseySpringBaseTest;

class UnmatchedPathTest extends JerseySpringBaseTest {

    @BeforeEach
    public void before() throws Exception {
        super.setUp();
    }

    @AfterEach
    public void after() throws Exception {
        super.tearDown();
    }

    @Test
    void shouldAnswerAnUnmatchedPathWithNotFound() {
        final Response response = target("/v1/userjh0003").request().get();

        assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
}
