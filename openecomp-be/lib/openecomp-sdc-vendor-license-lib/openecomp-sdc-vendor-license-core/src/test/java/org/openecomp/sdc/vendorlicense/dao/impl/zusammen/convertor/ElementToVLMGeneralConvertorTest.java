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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.item.Info;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.VendorLicenseModelDaoZusammenImpl;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;

class ElementToVLMGeneralConvertorTest {

    private static final String ENTITY_NAME = "entityTestName";
    private static final String ENTITY_DESCRIPTION = "entityTestDesc";

    private ElementToVLMGeneralConvertor converter = new ElementToVLMGeneralConvertor();

    @Test
    void shouldConvertElementToVendorLicenseModelEntity() {
        ZusammenElement elementToConvert = new ZusammenElement();
        Info info = new Info();
        info.addProperty(VendorLicenseModelDaoZusammenImpl.InfoPropertyName.NAME.name(), ENTITY_NAME);
        info.addProperty(VendorLicenseModelDaoZusammenImpl.InfoPropertyName.DESCRIPTION.name(), ENTITY_DESCRIPTION);
        elementToConvert.setInfo(info);
        VendorLicenseModelEntity result = converter.convert(elementToConvert);
        assertEquals(ENTITY_NAME, result.getVendorName());
        assertEquals(ENTITY_DESCRIPTION, result.getDescription());
    }

    @Test
    void shouldConvertElementInfoToVendorLicenseModelEntity() {
        ElementInfo elementInfo = new ElementInfo();
        Info info = new Info();
        info.addProperty(VendorLicenseModelDaoZusammenImpl.InfoPropertyName.NAME.name(), ENTITY_NAME);
        info.addProperty(VendorLicenseModelDaoZusammenImpl.InfoPropertyName.DESCRIPTION.name(), ENTITY_DESCRIPTION);
        elementInfo.setInfo(info);
        VendorLicenseModelEntity result = converter.convert(elementInfo);
        assertEquals(ENTITY_NAME, result.getVendorName());
        assertEquals(ENTITY_DESCRIPTION, result.getDescription());
    }

}
