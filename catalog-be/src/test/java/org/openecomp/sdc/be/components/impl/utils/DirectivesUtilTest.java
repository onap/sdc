/*
 * -
 *  * ============LICENSE_START=======================================================
 *  *  Copyright (C) 2019  Nordix Foundation.
 *  * ================================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  * ============LICENSE_END=========================================================
 *
 */

package org.openecomp.sdc.be.components.impl.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

public class DirectivesUtilTest {

    @Before
    public void setup() {
        new ConfigurationManager(new FSConfigurationSource(
            ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
    }

    @Test
    public void testGivenValidDirectives_returnsTrue() {
        assertTrue(DirectivesUtil.isValid(ConfigurationManager.getConfigurationManager().getConfiguration()
            .getDirectives()));
    }

    @Test
    public void testGivenEmptyDirectives_returnsTrue() {
        assertTrue(DirectivesUtil.isValid(Collections.emptyList()));
    }

    @Test
    public void testGivenInvalidDirectives_returnsFalse() {
        final List<String> directives = new ArrayList<>();
        directives.add("invalidValue");
        assertFalse(DirectivesUtil.isValid(directives));
    }
}
