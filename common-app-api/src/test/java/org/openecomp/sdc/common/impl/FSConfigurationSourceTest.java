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
package org.openecomp.sdc.common.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ErrorConfiguration;
import org.openecomp.sdc.be.config.Neo4jErrorsConfiguration;
import org.openecomp.sdc.common.api.Constants;

public class FSConfigurationSourceTest {

    @Test
    public void calculateFileNameWhenSplitRequired() {
        Class<ErrorConfiguration> clazz = ErrorConfiguration.class;
        String expected = "error-configuration" + Constants.YAML_SUFFIX;
        String actual = FSConfigurationSource.calculateFileName(clazz);
        assertEquals(expected, actual);
    }

    @Test
    public void calculateFileNameWhenNoSplitRequired() {
        Class<Configuration> clazz = Configuration.class;
        String expected = "configuration" + Constants.YAML_SUFFIX;
        String actual = FSConfigurationSource.calculateFileName(clazz);
        assertEquals(expected, actual);
    }

    @Test
    public void calculateFileNameWithCamelCaseAndDigits() {
        Class<Neo4jErrorsConfiguration> clazz = Neo4jErrorsConfiguration.class;
        String expected = "neo4j-errors-configuration" + Constants.YAML_SUFFIX;
        String actual = FSConfigurationSource.calculateFileName(clazz);
        assertEquals(expected, actual);
    }
}
