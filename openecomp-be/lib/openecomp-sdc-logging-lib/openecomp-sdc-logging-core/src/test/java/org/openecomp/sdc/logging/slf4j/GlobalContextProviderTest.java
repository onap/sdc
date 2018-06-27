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

package org.openecomp.sdc.logging.slf4j;

import static org.junit.Assert.assertNotNull;

import java.util.Map;
import org.junit.Test;

/**
 * Tests data supplied by the global logging context.
 *
 * @author evitaliy
 * @since 23 Mar 2018
 */

public class GlobalContextProviderTest {

    @Test
    public void providedValuesPopulated() {
        GlobalContextProvider provider = new GlobalContextProvider();
        Map<ContextField, String> values = provider.values();
        assertNotNull(values.get(ContextField.INSTANCE_ID));
        assertNotNull(values.get(ContextField.SERVER));
        assertNotNull(values.get(ContextField.SERVER_IP_ADDRESS));
    }
}