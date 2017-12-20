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
    if(element == null) return null;
    return mapInfoToVendorLicenseModelEntity( element.getInfo());

  }

  @Override
  public VendorLicenseModelEntity convert(Item item) {
    if(item == null) return null;
    return mapInfoToVendorLicenseModelEntity( item.getInfo());
  }

  @Override
  public VendorLicenseModelEntity convert(ElementInfo elementInfo) {
    if(elementInfo == null) return null;
    return mapInfoToVendorLicenseModelEntity( elementInfo.getInfo());

  }


  private VendorLicenseModelEntity mapInfoToVendorLicenseModelEntity(Info info) {

    VendorLicenseModelEntity vendorLicenseModelEntity = new VendorLicenseModelEntity();

    vendorLicenseModelEntity.setVendorName(info.getProperty(
        VendorLicenseModelDaoZusammenImpl.InfoPropertyName.name.name()));
    vendorLicenseModelEntity.setDescription(info.getProperty(
        VendorLicenseModelDaoZusammenImpl.InfoPropertyName.description.name()));
    vendorLicenseModelEntity.setIconRef(info.getProperty(
        VendorLicenseModelDaoZusammenImpl.InfoPropertyName.iconRef.name()));
    vendorLicenseModelEntity.setOldVersion(info.getProperty(
        VendorLicenseModelDaoZusammenImpl.InfoPropertyName.oldVersion.name()));


    return vendorLicenseModelEntity;
  }


}
