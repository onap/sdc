/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.common.kpi.api;

import org.junit.Test;

public class ASDCKpiApiTest {

    private ASDCKpiApi createTestSubject() {
        return new ASDCKpiApi();
    }

    @Test
    public void testCountImportResourcesKPI() throws Exception {
        // default test
        ASDCKpiApi.countImportResourcesKPI();
    }

    @Test
    public void testCountCreatedResourcesKPI() throws Exception {
        // default test
        ASDCKpiApi.countCreatedResourcesKPI();
    }

    @Test
    public void testCountCreatedServicesKPI() throws Exception {
        // default test
        ASDCKpiApi.countCreatedServicesKPI();
    }

    @Test
    public void testCountUsersAuthorizations() throws Exception {
        // default test
        ASDCKpiApi.countUsersAuthorizations();
    }

    @Test
    public void testCountActivatedDistribution() throws Exception {
        // default test
        ASDCKpiApi.countActivatedDistribution();
    }
}
