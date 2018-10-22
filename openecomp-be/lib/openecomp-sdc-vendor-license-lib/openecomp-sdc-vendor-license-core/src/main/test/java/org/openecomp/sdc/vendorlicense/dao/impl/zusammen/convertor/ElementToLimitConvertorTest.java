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

package org.openecomp.sdc.vendorlicense.dao.impl.zusammen.convertor;

import static org.junit.Assert.assertEquals;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.item.Info;
import org.junit.Test;
import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;

public class ElementToLimitConvertorTest {

    private static final String ENTITY_ID = "entityId1";
    private static final String ENTITY_NAME = "entityTestName";
    private static final String ENTITY_DESCRIPTION = "entityTestDesc";
    private static final String LIMIT_TYPE = "ServiceProvider";

    private ElementToLimitConvertor converter = new ElementToLimitConvertor();

    @Test
    public void shouldConvertElementToLimitEntity() {
        ZusammenElement elementToConvert = new ZusammenElement();
        elementToConvert.setElementId(new Id(ENTITY_ID));
        Info info = new Info();
        info.setName(ENTITY_NAME);
        info.setDescription(ENTITY_DESCRIPTION);
        info.addProperty("type",LIMIT_TYPE);
        elementToConvert.setInfo(info);
        LimitEntity result = converter.convert(elementToConvert);
        assertEquals(ENTITY_ID,result.getId());
        assertEquals(ENTITY_NAME, result.getName());
        assertEquals(ENTITY_DESCRIPTION, result.getDescription());
        assertEquals(LIMIT_TYPE, result.getType().name());
    }

}
