/*
 * Copyright (C) 2020 CMCC, Inc. and others. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 */

package org.openecomp.sdc.be.components.impl.utils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateServiceFromYamlParameterTest {
    private CreateServiceFromYamlParameter createTestSubject(){
        return new CreateServiceFromYamlParameter();
    }

    @Test
    public void testYamlName() {
        CreateServiceFromYamlParameter testSubject;

        // default test
        testSubject = createTestSubject();
        assertThat(testSubject).isInstanceOf(CreateServiceFromYamlParameter.class);
    }

}