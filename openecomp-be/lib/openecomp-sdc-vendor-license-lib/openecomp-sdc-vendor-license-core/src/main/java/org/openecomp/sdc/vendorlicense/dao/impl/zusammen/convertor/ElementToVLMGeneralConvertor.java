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
package org.openecomp.sdc.vendorlicense.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Item;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.VendorLicenseModelDaoZusammenImpl;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;

public class ElementToVLMGeneralConvertor extends ElementConvertor {

    @Override
    public VendorLicenseModelEntity convert(Element element) {
        if (element == null) {
            return null;
        }
        return mapInfoToVendorLicenseModelEntity(element.getInfo());
    }

    @Override
    public VendorLicenseModelEntity convert(Item item) {
        if (item == null) {
            return null;
        }
        return mapInfoToVendorLicenseModelEntity(item.getInfo());
    }

    @Override
    public VendorLicenseModelEntity convert(ElementInfo elementInfo) {
        if (elementInfo == null) {
            return null;
        }
        return mapInfoToVendorLicenseModelEntity(elementInfo.getInfo());
    }

    private VendorLicenseModelEntity mapInfoToVendorLicenseModelEntity(Info info) {
        VendorLicenseModelEntity vendorLicenseModelEntity = new VendorLicenseModelEntity();
        vendorLicenseModelEntity.setVendorName(info.getProperty(VendorLicenseModelDaoZusammenImpl.InfoPropertyName.NAME.name()));
        vendorLicenseModelEntity.setDescription(info.getProperty(VendorLicenseModelDaoZusammenImpl.InfoPropertyName.DESCRIPTION.name()));
        vendorLicenseModelEntity.setIconRef(info.getProperty(VendorLicenseModelDaoZusammenImpl.InfoPropertyName.ICON_REF.name()));
        return vendorLicenseModelEntity;
    }
}
