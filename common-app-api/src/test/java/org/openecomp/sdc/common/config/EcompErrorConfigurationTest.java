/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Modifications Copyright (c) 2019 Samsung
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 * ===================================================================
 *
 */

package org.openecomp.sdc.common.config;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import java.util.Map;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;


public class EcompErrorConfigurationTest {

    private EcompErrorConfiguration ecompErrorConfiguration;

    private ConfigurationManager configurationManager;

    @Before
    public void loadEcompErrorConfiguration() {
        String appConfigDir = "src/test/resources/config/common";
        ConfigurationSource configurationSource =
                new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
        configurationManager = new ConfigurationManager(configurationSource);
        ecompErrorConfiguration = configurationManager.getEcompErrorConfiguration();
    }

    @Test
    public void testGetErrors() {
        //when
        Map<String, EcompErrorInfo> result = ecompErrorConfiguration.getErrors();
        //then
        assertEquals(result.size(), 3);
    }

    @Test
    public void testGetEcompErrorInfo() {
        //when
        EcompErrorInfo clonedEcompErrorInfo = ecompErrorConfiguration.getEcompErrorInfo("BeRestApiGeneralError");
        //then
        assertEquals("SYSTEM_ERROR", clonedEcompErrorInfo.getType());
        assertEquals("ASDC_4000", clonedEcompErrorInfo.getCode());
        assertEquals("ERROR", clonedEcompErrorInfo.getSeverity());
        assertEquals("Unexpected error during BE REST API execution", clonedEcompErrorInfo.getDescription());
        assertEquals("CRITICAL", clonedEcompErrorInfo.getAlarmSeverity());
    }

    @Test
    public void testToString() throws Exception {
        //when
        String result = ecompErrorConfiguration.toString();
        //then
        assertThat(result, containsString(
                "EcompErrorConfiguration [errors={BeRestApiGeneralError=org.openecomp.sdc.common.config.EcompErrorInfo@"));
    }

    @Test
    public void testValidateEcompoErrorInfo() {
        //given
        EcompErrorInfo ecompErrorInfo = ecompErrorConfiguration.getEcompErrorInfo("BeInitializationError");
        Map<String, EcompErrorInfo> errors = new HashMap<>();
        errors.put("BeInitializationError", ecompErrorInfo);
        //when
        ecompErrorConfiguration.setErrors(errors);
        //then
        assertEquals(ecompErrorConfiguration.getErrors(), errors);
    }

    @Test
    public void testMain() throws Exception {
        String[] args = new String[] {""};

        // default test
        EcompErrorConfiguration.main(args);
    }

}