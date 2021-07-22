/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.CATEGORY;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.DESCRIPTION;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.FEATURE_GROUPS;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.ICON;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.LICENSE_AGREEMENT;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.LICENSE_TYPE;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.MODELS;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.NAME;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.ON_BOARDING_METHOD;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.SUB_CATEGORY;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.VENDOR_ID;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.VENDOR_NAME;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.VENDOR_VERSION;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Item;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;

class ElementToVSPGeneralConvertorTest {

    final ElementToVSPGeneralConvertor elementToVSPGeneralConvertor = new ElementToVSPGeneralConvertor();

    @Test
    void convertElementTest() {
        VspDetails actualVspDetails = elementToVSPGeneralConvertor.convert((Element) null);
        assertNull(actualVspDetails);
        final Element elementMock = mock(Element.class);
        final Info defaultInfo = createDefaultInfo();
        when(elementMock.getInfo()).thenReturn(defaultInfo);
        actualVspDetails = elementToVSPGeneralConvertor.convert(elementMock);
        assertNotNull(actualVspDetails);
        assertVspDetails(actualVspDetails, defaultInfo);
    }

    @Test
    void convertElementInfoTest() {
        VspDetails actualVspDetails = elementToVSPGeneralConvertor.convert((ElementInfo) null);
        assertNull(actualVspDetails);
        final ElementInfo elementInfoMock = mock(ElementInfo.class);
        final Info defaultInfo = createDefaultInfo();
        when(elementInfoMock.getInfo()).thenReturn(defaultInfo);
        actualVspDetails = elementToVSPGeneralConvertor.convert(elementInfoMock);
        assertNotNull(actualVspDetails);
        assertVspDetails(actualVspDetails, defaultInfo);
    }

    @Test
    void convertItemTest() {
        VspDetails actualVspDetails = elementToVSPGeneralConvertor.convert((Item) null);
        assertNull(actualVspDetails);
        final Item elementInfoMock = mock(Item.class);
        final Info defaultInfo = createDefaultInfo();
        final var itemId = new Id();
        final var id = "anId";
        itemId.setValue(id);

        when(elementInfoMock.getInfo()).thenReturn(defaultInfo);
        when(elementInfoMock.getId()).thenReturn(itemId);
        actualVspDetails = elementToVSPGeneralConvertor.convert(elementInfoMock);

        assertNotNull(actualVspDetails);
        assertEquals(actualVspDetails.getId(), id);
        assertVspDetails(actualVspDetails, defaultInfo);
    }

    private void assertVspDetails(final VspDetails vspDetails, final Info info) {
        assertEquals(vspDetails.getName(), info.getProperty(NAME.getValue()));
        assertEquals(vspDetails.getDescription(), info.getProperty(DESCRIPTION.getValue()));
        assertEquals(vspDetails.getIcon(), info.getProperty(ICON.getValue()));
        assertEquals(vspDetails.getCategory(), info.getProperty(CATEGORY.getValue()));
        assertEquals(vspDetails.getSubCategory(), info.getProperty(SUB_CATEGORY.getValue()));
        assertEquals(vspDetails.getVendorId(), info.getProperty(VENDOR_ID.getValue()));
        assertEquals(vspDetails.getVendorName(), info.getProperty(VENDOR_NAME.getValue()));
        assertEquals(vspDetails.getVlmVersion().getId(), info.getProperty(VENDOR_VERSION.getValue()));
        assertEquals(vspDetails.getLicenseType(), info.getProperty(LICENSE_TYPE.getValue()));
        assertEquals(vspDetails.getLicenseAgreement(), info.getProperty(LICENSE_AGREEMENT.getValue()));
        assertEquals(vspDetails.getFeatureGroups(), info.getProperty(FEATURE_GROUPS.getValue()));
        assertEquals(vspDetails.getOnboardingMethod(), info.getProperty(ON_BOARDING_METHOD.getValue()));
        assertEquals(vspDetails.getModelIdList(), info.getProperty(MODELS.getValue()));
    }

    private Info createDefaultInfo() {
        var info = new Info();
        final Set<InfoPropertyName> collectionProperties = Set.of(FEATURE_GROUPS, MODELS);
        Arrays.stream(InfoPropertyName.values()).filter(propertyName -> !collectionProperties.contains(propertyName))
            .forEach(propertyName -> info.addProperty(propertyName.getValue(), propertyName.getValue()));
        info.addProperty(FEATURE_GROUPS.getValue(), List.of("group1", "group2"));
        info.addProperty(MODELS.getValue(), List.of("model1", "model2"));
        return info;
    }
}