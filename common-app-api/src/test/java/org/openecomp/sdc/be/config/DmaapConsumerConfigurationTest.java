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
package org.openecomp.sdc.be.config;

import static org.hamcrest.MatcherAssert.assertThat;

import com.google.code.beanmatchers.BeanMatchers;
import org.junit.Test;

public class DmaapConsumerConfigurationTest {

    @Test
    public void shouldHaveValidGettersAndSetters() {
        assertThat(DmaapConsumerConfiguration.class, BeanMatchers.hasValidGettersAndSetters());
    }

    @Test
    public void shouldHaveValidCtor() {
        assertThat(DmaapConsumerConfiguration.class, BeanMatchers.hasValidBeanConstructor());
    }

    @Test
    public void shouldHaveValidToString() {
        assertThat(DmaapConsumerConfiguration.class, BeanMatchers.hasValidBeanToString());
    }

    @Test
    public void shouldHaveValidGettersAndSettersNested() {
        assertThat(DmaapConsumerConfiguration.Credential.class, BeanMatchers.hasValidGettersAndSetters());
    }

    @Test
    public void shouldHaveValidCtorNested() {
        assertThat(DmaapConsumerConfiguration.Credential.class, BeanMatchers.hasValidBeanConstructor());
    }

    @Test
    public void shouldHaveValidToStringNested() {
        assertThat(DmaapConsumerConfiguration.Credential.class, BeanMatchers.hasValidBeanToString());
    }
}
