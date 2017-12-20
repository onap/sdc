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
        .InfoPropertyName.name.name()));
    vspDetails.setDescription(info.getProperty(
        VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.description.name()));
    vspDetails.setIcon(info.getProperty(
        VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.icon.name()));
    vspDetails.setCategory(info.getProperty(
        VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.category.name()));
    vspDetails.setSubCategory(info.getProperty(
        VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.subCategory.name()));
    vspDetails.setVendorId(info.getProperty(
        VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.vendorId.name()));
    vspDetails.setVendorName(info.getProperty(
        VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.vendorName.name()));
    if (info.getProperty(
        VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.vendorVersion.name()) != null) {
      vspDetails.setVlmVersion(new Version(info.getProperty(
          VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.vendorVersion.name())));
    }

    vspDetails.setLicenseAgreement(info.getProperty(
        VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.licenseAgreement.name()));
    vspDetails.setFeatureGroups(info.getProperty(
        VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.featureGroups.name()));
    vspDetails.setOnboardingMethod(info.getProperty(
        VendorSoftwareProductInfoDaoZusammenImpl.InfoPropertyName.onboardingMethod.name()));

    return vspDetails;
  }


}
