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
 * Copyright © 2018 European Support Limited
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
import org.junit.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;

public class ElementToComponentDependencyModelConvertorTest {

    private static final String ENTITY_ID = "entityId1";
    private static final String SOURCE_ID = "sourceId1";
    private static final String TARGET_ID = "targetId1";
    private static final String RELATION_TYPE = "dependsOn";

    private ElementToComponentDependencyModelConvertor converter = new ElementToComponentDependencyModelConvertor();


    @Test
    public void shouldConvertElementToComponentDependencyModelEntity() {
        ZusammenElement elementToConvert = new ZusammenElement();
        elementToConvert.setElementId(new Id(ENTITY_ID));
        elementToConvert.setInfo(createInfo());
        ComponentDependencyModelEntity result = converter.convert(elementToConvert);
        assertEquals(ENTITY_ID, result.getId());
        assertEquals(SOURCE_ID, result.getSourceComponentId());
        assertEquals(TARGET_ID, result.getTargetComponentId());
        assertEquals(RELATION_TYPE, result.getRelation());
    }

    @Test
    public void shouldConvertElementInfoToComponentDependencyModelEntity() {
        ElementInfo elementToConvert = new ElementInfo();
        elementToConvert.setId(new Id(ENTITY_ID));
        elementToConvert.setInfo(createInfo());
        ComponentDependencyModelEntity result = converter.convert(elementToConvert);
        assertEquals(ENTITY_ID, result.getId());
        assertEquals(SOURCE_ID, result.getSourceComponentId());
        assertEquals(TARGET_ID, result.getTargetComponentId());
        assertEquals(RELATION_TYPE, result.getRelation());
    }

    private Info createInfo() {
        Info info = new Info();
        info.addProperty("sourcecomponent_id", SOURCE_ID);
        info.addProperty("targetcomponent_id", TARGET_ID);
        info.addProperty("relation", RELATION_TYPE);
        return info;
    }
}
