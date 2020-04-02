/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020, Nordix Foundation. All rights reserved.
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
package org.openecomp.sdc.be.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

public class DAOJanusGraphStrategyTest {

    @Before
    public void setUp() throws Exception {
        new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
            "src/test/resources/config/catalog-dao"));
    }

    private DAOJanusGraphStrategy createTestSubject() {
        return new DAOJanusGraphStrategy();
    }

    @Test
    public void getConfigFile() {
        DAOJanusGraphStrategy testSubject;

        // default test
        testSubject = createTestSubject();
        assertThat(testSubject.getConfigFile()).isNotNull();
    }
}
