/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.info;

import com.google.code.beanmatchers.BeanMatchers;
import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static org.hamcrest.MatcherAssert.assertThat;

public class ToscaNodeTypeInfoTest {

    @Test
    public void shouldHaveValidDefaultConstructor() {
        assertThat(ServiceInfo.class, hasValidBeanConstructor());
    }

    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(ToscaNodeTypeInfo.class, BeanMatchers.hasValidGettersAndSetters());
    }
    
    @Test
    public void allPropertiesShouldBeRepresentedInToStringOutput() {
        assertThat(ToscaNodeTypeInfo.class, BeanMatchers.hasValidBeanToString());
    }

}