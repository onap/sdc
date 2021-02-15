/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021, Nordix Foundation. All rights reserved.
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
package org.openecomp.sdc.be.tosca.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.tosca.AttributeConverter;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.springframework.beans.factory.ObjectProvider;

@ExtendWith(MockitoExtension.class)
class OutputConverterTest {

    @InjectMocks
    private OutputConverter testSubject;

    @Mock
    private ObjectProvider<AttributeConverter> attributeConverterProvider;

    @Test
    void test_convert_success() {
        final List<OutputDefinition> outputDefinitionList = new ArrayList<>();
        final OutputDefinition outputDefinition = new OutputDefinition();
        outputDefinitionList.add(outputDefinition);

        doReturn(new AttributeConverter(new HashMap<>())).when(attributeConverterProvider).getObject(any());
        final Map<String, ToscaProperty> result = testSubject.convert(outputDefinitionList, new HashMap<>());
        assertThat(result).isNotNull();
    }

    @Test
    void test_convert_isEmpty() {
        final Map<String, ToscaProperty> result = testSubject.convert(new ArrayList<>(), new HashMap<>());
        assertThat(result).isNotNull().isEmpty();
    }

}
