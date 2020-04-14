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

package org.openecomp.sdc.asdctool.impl.validator.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openecomp.sdc.asdctool.impl.validator.config.ValidationConfigManager.getValidationConfiguration;
import static org.openecomp.sdc.asdctool.impl.validator.config.ValidationConfigManager.setValidationConfiguration;

@RunWith(PowerMockRunner.class)
public class ValidationConfigManagerTest {

    @Test
    public void testSetValidationConfiguration() {
        String path = "";

        Properties expected = getValidationConfiguration();
        setValidationConfiguration(path);
        Properties actual = getValidationConfiguration();

        assertThat(actual).isEqualTo(expected);
    }
}
