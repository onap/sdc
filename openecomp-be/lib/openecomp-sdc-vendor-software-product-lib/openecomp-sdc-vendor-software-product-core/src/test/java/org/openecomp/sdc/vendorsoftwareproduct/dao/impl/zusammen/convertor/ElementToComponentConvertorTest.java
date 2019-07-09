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

/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import static org.junit.Assert.assertEquals;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.item.Info;
import java.io.ByteArrayInputStream;
import org.junit.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;

public class ElementToComponentConvertorTest {

    private static final String ENTITY_ID = "entityId1";
    private static final String ENTITY_NAME = "entityTestName";
    private static final String ENTITY_DISPLAY_NAME = "entityTestDisplayName";
    private static final String ENTITY_DESCRIPTION = "entityTestDesc";
    private static final String COMPOSITION_DATA =
            "{\"name\":\"entityTestName\",\"description\":\"entityTestDesc\",\"displayName\":\"entityTestDisplayName\"}";

    private ElementToComponentConvertor converter = new ElementToComponentConvertor();

    @Test
    public void shouldConvertElementToEntitlementPoolEntity() {
        ZusammenElement elementToConvert = new ZusammenElement();
        elementToConvert.setData(new ByteArrayInputStream(COMPOSITION_DATA.getBytes()));
        elementToConvert.setElementId(new Id(ENTITY_ID));
        elementToConvert.setInfo(createInfo());
        ComponentEntity result = converter.convert(elementToConvert);
        assertEquals(ENTITY_ID, result.getId());
        assertEquals(ENTITY_NAME, result.getComponentCompositionData().getName());
        assertEquals(ENTITY_DISPLAY_NAME, result.getComponentCompositionData().getDisplayName());
        assertEquals(ENTITY_DESCRIPTION, result.getComponentCompositionData().getDescription());
    }

    @Test
    public void shouldConvertElementInfoToEntitlementPoolEntity() {
        ElementInfo elementToConvert = new ElementInfo();
        elementToConvert.setId(new Id(ENTITY_ID));
        elementToConvert.setInfo(createInfo());
        ComponentEntity result = converter.convert(elementToConvert);
        assertEquals(ENTITY_ID, result.getId());
        assertEquals(ENTITY_NAME, result.getComponentCompositionData().getName());
        assertEquals(ENTITY_DISPLAY_NAME, result.getComponentCompositionData().getDisplayName());
        assertEquals(ENTITY_DESCRIPTION, result.getComponentCompositionData().getDescription());
    }

    private Info createInfo() {
        Info info = new Info();
        info.addProperty("compositionData", COMPOSITION_DATA);
        return info;
    }
}
