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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;


public class EcompErrorConfigurationTest {

    private EcompErrorConfiguration ecompErrorConfiguration;

    private ConfigurationManager configurationManager;

    @BeforeEach
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
        assertTrue(result.contains(
                "EcompErrorConfiguration [errors={BeRestApiGeneralError=org.openecomp.sdc.common.config.EcompErrorInfo@"));
    }

    @Test
    public void testValidateEcompoErrorInfo() {
        Map<String, EcompErrorInfo> errors = new HashMap();
        EcompErrorInfo ecompErrorInfo = ecompErrorConfiguration.getEcompErrorInfo("BeInitializationError");
        errors.put("BeInitializationError", ecompErrorInfo);
        //when
        ecompErrorConfiguration.setErrors(errors);
        //then
        assertEquals(errors, ecompErrorConfiguration.getErrors());

        // error info is null
        errors.clear();
        ecompErrorConfiguration.setErrors(new HashMap());
        errors.put("error1", null);
        ecompErrorConfiguration.setErrors(errors);
        assertEquals(0, ecompErrorConfiguration.getErrors().size());
        assertNull(ecompErrorConfiguration.getEcompErrorInfo("error1"));
        errors.clear();

        // type is null or invalid
        EcompErrorInfo errorInfo = new EcompErrorInfo();
        errors.put("error1", errorInfo);
        ecompErrorConfiguration.setErrors(errors);
        assertEquals(0, ecompErrorConfiguration.getErrors().size());
        errorInfo.setType("type");
        ecompErrorConfiguration.setErrors(errors);
        assertEquals(0, ecompErrorConfiguration.getErrors().size());

        // severity is null or invalid
        errorInfo.setType(EcompErrorConfiguration.EcompErrorType.CONFIG_ERROR.name());
        ecompErrorConfiguration.setErrors(errors);
        assertEquals(0, ecompErrorConfiguration.getErrors().size());
        errorInfo.setSeverity("severity");
        ecompErrorConfiguration.setErrors(errors);
        assertEquals(0, ecompErrorConfiguration.getErrors().size());

        // alarmSeverify is null or invalid
        errorInfo.setSeverity(EcompErrorConfiguration.EcompErrorSeverity.INFO.name());
        ecompErrorConfiguration.setErrors(errors);
        assertEquals(0, ecompErrorConfiguration.getErrors().size());
        errorInfo.setAlarmSeverity("alarmSeverify");
        ecompErrorConfiguration.setErrors(errors);
        assertEquals(0, ecompErrorConfiguration.getErrors().size());

        // code is null or invalid
        errorInfo.setAlarmSeverity(EcompErrorConfiguration.EcompAlarmSeverity.CRITICAL.name());
        ecompErrorConfiguration.setErrors(errors);
        assertEquals(0, ecompErrorConfiguration.getErrors().size());
        errorInfo.setCode("ASDC_0001");
        ecompErrorConfiguration.setErrors(errors);
        assertEquals(0, ecompErrorConfiguration.getErrors().size());

    }

}
