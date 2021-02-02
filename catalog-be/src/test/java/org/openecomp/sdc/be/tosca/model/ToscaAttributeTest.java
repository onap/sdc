/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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

package org.openecomp.sdc.be.tosca.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.DEFAULT;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.DESCRIPTION;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.ENTRY_SCHEMA;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.KEY_SCHEMA;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.STATUS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.TYPE;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ToscaAttributeTest {

    @Test
    void testMapConversion() {
        final ToscaAttribute toscaAttribute = new ToscaAttribute();
        assertToscaAttributeMap(toscaAttribute);

        toscaAttribute.setType("type");
        toscaAttribute.setDescription("description");
        toscaAttribute.setKeySchema(new ToscaSchemaDefinition());
        toscaAttribute.setEntrySchema(new ToscaSchemaDefinition());
        toscaAttribute.setDefault("default");
        toscaAttribute.setStatus("status");

        assertToscaAttributeMap(toscaAttribute);
    }

    private void assertToscaAttributeMap(final ToscaAttribute toscaAttribute) {
        final Map<String, Object> toscaAttributeAsMap = toscaAttribute.asToscaMap();
        assertEquals(toscaAttribute.getType(), toscaAttributeAsMap.get(TYPE.getElementName()));
        assertEquals(toscaAttribute.getDescription(),
            toscaAttributeAsMap.get(DESCRIPTION.getElementName()));
        assertEquals(toscaAttribute.getKeySchema(),
            toscaAttributeAsMap.get(KEY_SCHEMA.getElementName()));
        assertEquals(toscaAttribute.getEntrySchema(),
            toscaAttributeAsMap.get(ENTRY_SCHEMA.getElementName()));
        assertEquals(toscaAttribute.getDefault(), toscaAttributeAsMap.get(DEFAULT.getElementName()));
        assertEquals(toscaAttribute.getStatus(), toscaAttributeAsMap.get(STATUS.getElementName()));
    }
}