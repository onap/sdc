/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum;

@RunWith(MockitoJUnitRunner.class)
public class TypeUtilsTest {

    private static final String ANY_GROUP = "anyGroup";

    @SuppressWarnings("unchecked")
    private Consumer<String> anyConsumer = (Consumer<String>) Mockito.spy(Consumer.class);

    @Test
    public void testSetFieldShouldConsumeForJSONContainingParam() {
        Map<String, Object> toscaJson = new HashMap<>();
        toscaJson.put(ToscaTagNamesEnum.GROUPS.getElementName(), ANY_GROUP);
        TypeUtils.setField(toscaJson, ToscaTagNamesEnum.GROUPS, anyConsumer);
        Mockito.verify(anyConsumer).accept(ANY_GROUP);
    }

    @Test
    public void testSetFieldShouldDoNothingForJSONNotContainingParam() {
        Map<String, Object> toscaJson = new HashMap<>();
        toscaJson.put(ToscaTagNamesEnum.GROUPS.getElementName(), ANY_GROUP);
        TypeUtils.setField(toscaJson, ToscaTagNamesEnum.INPUTS, anyConsumer);
        Mockito.verifyZeroInteractions(anyConsumer);
    }

}