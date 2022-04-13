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
package org.openecomp.sdc.vendorlicense.dao.impl.zusammen;

import static org.openecomp.sdc.versioning.dao.impl.zusammen.ItemZusammenDaoImpl.ItemInfoProperty.ITEM_TYPE;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import java.util.Collection;
import java.util.stream.Collectors;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorlicense.dao.VendorLicenseModelDao;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.convertor.ElementToVLMGeneralConvertor;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.versioning.ActionVersioningManagerFactory;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;
import org.openecomp.sdc.versioning.types.VersionableEntityStoreType;

public class VendorLicenseModelDaoZusammenImpl implements VendorLicenseModelDao {

    private final ZusammenAdaptor zusammenAdaptor;

    public VendorLicenseModelDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
        this.zusammenAdaptor = zusammenAdaptor;
    }

    @Override
    public void registerVersioning(String versionableEntityType) {
        VersionableEntityMetadata metadata = new VersionableEntityMetadata(VersionableEntityStoreType.Zusammen, "VendorLicenseModel", null, null);
        ActionVersioningManagerFactory.getInstance().createInterface().register(versionableEntityType, metadata);
    }

    @Override
    public Collection<VendorLicenseModelEntity> list(VendorLicenseModelEntity vendorLicenseModelEntity) {
        ElementToVLMGeneralConvertor convertor = new ElementToVLMGeneralConvertor();
        return zusammenAdaptor.listItems(ZusammenUtil.createSessionContext()).stream()
            .filter(item -> "VendorLicenseModel".equals(item.getInfo().getProperty(ITEM_TYPE.getName()))).map(item -> {
                VendorLicenseModelEntity entity = convertor.convert(item);
                entity.setId(item.getId().getValue());
                entity.setVersion(null);
                return entity;
            }).collect(Collectors.toList());
    }

    @Override
    public void create(VendorLicenseModelEntity vendorLicenseModel) {
        SessionContext context = ZusammenUtil.createSessionContext();
        ElementContext elementContext = new ElementContext(vendorLicenseModel.getId(), vendorLicenseModel.getVersion().getId());
        ZusammenElement generalElement = mapVlmToZusammenElement(vendorLicenseModel, Action.CREATE);
        zusammenAdaptor.saveElement(context, elementContext, generalElement, "Create VLM General Info Element");
        ZusammenElement licenseAgreementsElement = ZusammenUtil.buildStructuralElement(ElementType.LicenseAgreements, Action.CREATE);
        zusammenAdaptor.saveElement(context, elementContext, licenseAgreementsElement, "Create VLM licenseAgreementsElement");
        ZusammenElement featureGroupsElement = ZusammenUtil.buildStructuralElement(ElementType.FeatureGroups, Action.CREATE);
        zusammenAdaptor.saveElement(context, elementContext, featureGroupsElement, "Create VLM featureGroupsElement");
        ZusammenElement lkgsElement = ZusammenUtil.buildStructuralElement(ElementType.LicenseKeyGroups, Action.CREATE);
        zusammenAdaptor.saveElement(context, elementContext, lkgsElement, "Create VLM lkgsElement");
        ZusammenElement entitlementPoolsElement = ZusammenUtil.buildStructuralElement(ElementType.EntitlementPools, Action.CREATE);
        zusammenAdaptor.saveElement(context, elementContext, entitlementPoolsElement, "Create VLM entitlementPoolsElement");
    }

    @Override
    public void update(VendorLicenseModelEntity vendorLicenseModel) {
        ZusammenElement generalElement = mapVlmToZusammenElement(vendorLicenseModel, Action.UPDATE);
        SessionContext context = ZusammenUtil.createSessionContext();
        zusammenAdaptor.saveElement(context, new ElementContext(vendorLicenseModel.getId(), vendorLicenseModel.getVersion().getId()), generalElement,
            "Update VSP General Info Element");
    }

    @Override
    public VendorLicenseModelEntity get(VendorLicenseModelEntity vendorLicenseModel) {
        SessionContext context = ZusammenUtil.createSessionContext();
        ElementContext elementContext = new ElementContext(vendorLicenseModel.getId(), vendorLicenseModel.getVersion().getId());
        ElementToVLMGeneralConvertor convertor = new ElementToVLMGeneralConvertor();
        return zusammenAdaptor.getElementInfoByName(context, elementContext, null, ElementType.VendorLicenseModel.name()).map(generalElementInfo -> {
            VendorLicenseModelEntity entity = convertor.convert(generalElementInfo);
            entity.setId(vendorLicenseModel.getId());
            entity.setVersion(vendorLicenseModel.getVersion());
            return entity;
        }).orElse(null);
    }

    @Override
    public void delete(VendorLicenseModelEntity entity) {
        throw new UnsupportedOperationException("Delete vlm version is done using versioning manager");
    }

    private ZusammenElement mapVlmToZusammenElement(VendorLicenseModelEntity vendorLicenseModel, Action action) {
        ZusammenElement generalElement = ZusammenUtil.buildStructuralElement(ElementType.VendorLicenseModel, action);
        addVlmToInfo(generalElement.getInfo(), vendorLicenseModel);
        return generalElement;
    }

    private void addVlmToInfo(Info info, VendorLicenseModelEntity vendorLicenseModel) {
        info.addProperty(InfoPropertyName.name.name(), vendorLicenseModel.getVendorName());
        info.addProperty(InfoPropertyName.description.name(), vendorLicenseModel.getDescription());
        info.addProperty(InfoPropertyName.iconRef.name(), vendorLicenseModel.getIconRef());
    }

    public enum InfoPropertyName {name, description, iconRef,}
}
