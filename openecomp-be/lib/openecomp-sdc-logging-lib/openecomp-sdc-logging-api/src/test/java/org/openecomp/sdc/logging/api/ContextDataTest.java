/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.logging.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Unit-testing context data builder.
 *
 * @author evitaliy
 * @since 04 Mar 18
 */
public class ContextDataTest {

    @Test
    public void allPropertiesReadWhenPopulated() {

        final String serviceName = "running-service";
        final String partnerName = "remote-partner";
        final String requestId = "123412341234";

        ContextData data = ContextData.builder()
                                      .serviceName(serviceName).partnerName(partnerName).requestId(requestId).build();

        assertEquals(requestId, data.getRequestId());
        assertEquals(serviceName, data.getServiceName());
        assertEquals(partnerName, data.getPartnerName());
    }

    @Test
    public void allPropertiesEmptyWhenUnpopulated() {
        ContextData data = ContextData.builder().build();
        assertNull(data.getRequestId());
        assertNull(data.getServiceName());
        assertNull(data.getPartnerName());
    }
}