/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class NonManoConfigurationManagerTest {

    @Test
    public void getInstance() {
        final NonManoConfigurationManager instance = NonManoConfigurationManager.getInstance();
        assertThat("Singleton instance should never be null", instance, is(notNullValue()));
    }

    @Test
    public void getNonManoConfiguration() {
        final NonManoConfiguration nonManoConfiguration = NonManoConfigurationManager.getInstance()
            .getNonManoConfiguration();
        assertThat("NonManoConfiguration instance should never be null", nonManoConfiguration, is(notNullValue()));
        assertThat("NonManoConfiguration FolderMapping configuration should no be empty",
            nonManoConfiguration.getNonManoKeyFolderMapping(), is(not(anEmptyMap())));

        for (final NonManoArtifactType value : NonManoArtifactType.values()) {
            assertThat(String.format("Expected %s value should not be null", value),
                nonManoConfiguration.getNonManoType(value), is(notNullValue()));
        }
    }
}