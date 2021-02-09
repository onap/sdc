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

package org.openecomp.sdc.be.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:application-context-test.xml"})
@Ignore("application-context has to be adjusted")
public class ServletUtilsTest {

    @Autowired
    ServletUtils servletUtils;

    @Test
    public void testCtrServletUtils() {
        // default test
        assertThat(servletUtils)
            .isNotNull()
            .isInstanceOf(ServletUtils.class);

        ComponentsUtils componentsUtils = servletUtils.componentsUtils;
        UserBusinessLogic userBusinessLogic = servletUtils.userAdmin;
        Gson gson = servletUtils.gson;

        assertThat(gson)
            .isNotNull()
            .isInstanceOf(Gson.class)
            .isEqualTo(servletUtils.getGson());
        assertThat(componentsUtils)
            .isNotNull()
            .isInstanceOf(ComponentsUtils.class)
            .isEqualTo(servletUtils.getComponentsUtils());
        assertThat(userBusinessLogic)
            .isNotNull()
            .isInstanceOf(UserBusinessLogic.class)
            .isEqualTo(servletUtils.getUserAdmin());
    }
}
