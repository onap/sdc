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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = {"classpath:application-context-test.xml"})
public class ServletUtilsTest {

    private ServletUtils servletUtils;
    private ComponentsUtils componentsUtils;
    private UserBusinessLogic userBusinessLogic;
    private Gson gson;

    @BeforeEach
    public void setup() {
        servletUtils = mock(ServletUtils.class);
        userBusinessLogic = mock(UserBusinessLogic.class);
        componentsUtils = mock(ComponentsUtils.class);
        gson = new GsonBuilder().setPrettyPrinting().create();
        when(servletUtils.getComponentsUtils()).thenReturn(componentsUtils);
        when(servletUtils.getGson()).thenReturn(gson);
        when(servletUtils.getUserAdmin()).thenReturn(userBusinessLogic);
    }

    @Test
    public void testCtrServletUtils() {
        assertThat(servletUtils)
            .isNotNull()
            .isInstanceOf(ServletUtils.class);
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
