/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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
package org.openecomp.sdc.be.components.impl;

import org.junit.Test;
import org.openecomp.sdc.be.model.Service;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ActivationRequestInformationTest {

    private static final String TENANT = "tenant";
    private static final String WORKLOAD_CONTEXT = "workloadContext";

    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(ActivationRequestInformation.class, hasValidGettersAndSetters());
    }

    @Test
    public void testFullArgConstructor() {
        Service serviceToActivate = new Service();
        ActivationRequestInformation activationRequestInformation = new ActivationRequestInformation(serviceToActivate,
            WORKLOAD_CONTEXT,
            TENANT);
        assertEquals(activationRequestInformation.getServiceToActivate(), serviceToActivate);
        assertEquals(activationRequestInformation.getTenant(), TENANT);
        assertEquals(activationRequestInformation.getWorkloadContext(), WORKLOAD_CONTEXT);
    }
}