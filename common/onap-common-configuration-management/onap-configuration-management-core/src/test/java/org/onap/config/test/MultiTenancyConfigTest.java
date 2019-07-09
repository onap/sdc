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

package org.onap.config.test;

import java.io.IOException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.config.api.Configuration;
import org.onap.config.api.ConfigurationManager;
import org.onap.config.util.ConfigTestConstant;
import org.onap.config.util.TestUtil;

/**
 * Created by ARR on 10/14/2016.
 * Scenario 17 - Verify Configuration management System - Support for Multi-Tenancy.
 */
public class MultiTenancyConfigTest {

    private static final String NAMESPACE = "tenancy";

    @Before
    public void setUp() throws IOException {
        String data = "{name:\"SCM\"}";
        TestUtil.writeFile(data);
    }

    @Test
    public void testConfigurationWithMultiTenancyFileFormat() {
        Configuration config = ConfigurationManager.lookup();

        Assert.assertEquals(config.getAsString("OPENECOMP", NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH),
                "20");

        Assert.assertEquals(config.getAsString("Telefonica", NAMESPACE, ConfigTestConstant.ARTIFACT_STATUS), "Deleted");

        Assert.assertEquals(config.getAsString("TID", NAMESPACE, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH), "14");

    }

    @After
    public void tearDown() throws Exception {
        TestUtil.cleanUp();
    }
}
