/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.core.converter.impl.pnfd.parser;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.core.converter.pnfd.model.ConversionDefinition;

public class PnfdCustomNodeTypeBlockParserTest {

    @Test
    public void testBuildParseBlock() {
        final PnfdCustomNodeTypeBlockParser blockParser = spy(new PnfdCustomNodeTypeBlockParser(null));
        final ConversionDefinition conversionDefinition = Mockito.mock(ConversionDefinition.class);
        final Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("type", null);
        stringObjectMap.put("name", null);
        assertEquals(Optional.empty(), blockParser.buildParsedBlock(
            stringObjectMap, stringObjectMap, conversionDefinition));
    }

}
