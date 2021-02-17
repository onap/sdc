/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nokia.
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

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import org.junit.Test;

public class Neo4jErrorsConfigurationTest {

    @Test
    public void validateBean() {
        assertThat(Neo4jErrorsConfiguration.class, allOf(hasValidBeanConstructor(), hasValidGettersAndSettersExcluding(), hasValidBeanToString()));
    }

    @Test
    public void testGetErrorMessage() {
        final String testKey = "key";
        final String testValue = "value";
        Neo4jErrorsConfiguration neo4jErrorsConfiguration = new Neo4jErrorsConfiguration();
        neo4jErrorsConfiguration.setErrors(Collections.singletonMap(testKey, testValue));
        assertEquals(neo4jErrorsConfiguration.getErrorMessage(testKey), testValue);
    }
}
