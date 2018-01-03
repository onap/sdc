package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Item;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.VendorSoftwareProductInfoDaoZusammenImpl;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.versioning.dao.types.Version;


public class ElementToVSPGeneralConvertor extends ElementConvertor {
  @Override
  public VspDetails convert(Element element) {
    if (element == null) {
      return null;
    }
    return mapInfoToVspDetails(element.getInfo());

  }

  @Override
  public VspDetails convert(Item item) {
    if (item == null) {
      return null;
    }
    VspDetails vspDetails = mapInfoToVspDetails(item.getInfo());
    vspDetails.setId(item.getId().getValue());
    return vspDetails;
  }

  @Override
  public VspDetails convert(ElementInfo elementInfo) {
    if (elementInfo == null) {
      return null;
    }
    return mapInfoToVspDetails(elementInfo.getInfo());

  }


  private VspDetails mapInfoToVspDetails(Info info) {

    VspDetails vspDetails = new VspDetails();

    vspDetails.setName(info.getProperty(VendorSoftwareProductInfoDaoZusammenImpl
        .InfoPropertyName.NAME.getValue()));
    vspDetails.setDescription(info.getProperty(
        VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.DESCRIPTION.getValue()));
    vspDetails.setIcon(info.getProperty(
        VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.ICON.getValue()));
    vspDetails.setCategory(info.getProperty(
        VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.CATEGORY.getValue()));
    vspDetails.setSubCategory(info.getProperty(
        VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.SUB_CATEGORY.getValue()));
    vspDetails.setVendorId(info.getProperty(
        VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.VENDOR_ID.getValue()));
    vspDetails.setVendorName(info.getProperty(
        VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.VENDOR_NAME.getValue()));
    if (info.getProperty(
        VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.VENDOR_VERSION.getValue()) != null) {
      vspDetails.setVlmVersion(new Version(info.getProperty(
          VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.VENDOR_VERSION.getValue())));
    }

    vspDetails.setLicenseAgreement(info.getProperty(
        VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.LICENSE_AGREEMENT.getValue()));
    vspDetails.setFeatureGroups(info.getProperty(
        VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.FEATURE_GROUPS.getValue()));
    vspDetails.setOnboardingMethod(info.getProperty(
        VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.ON_BOARDING_METHOD.getValue()));

    return vspDetails;
  }


}
