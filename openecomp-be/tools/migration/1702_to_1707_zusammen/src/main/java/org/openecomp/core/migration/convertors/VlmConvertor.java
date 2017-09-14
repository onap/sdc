package org.openecomp.core.migration.convertors;

import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import org.openecomp.core.migration.store.ElementHandler;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.StructureElement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VlmConvertor {

  private static Set<String> compNicLoaded = new HashSet<>();

  public static CollaborationElement[] convertVlmToElement(VendorLicenseModelEntity vendorLicenseModel) {

    CollaborationElement[] vspElements = new CollaborationElement[1];
    List<String> vspNamespace = getVlmNamespace(vendorLicenseModel);


    String vspEntityId = StructureElement.General.name();

    vspElements[0] = ElementHandler.getElementEntity(
        vendorLicenseModel.getId(), vendorLicenseModel.getVersion().toString(),
        vspEntityId,
        vspNamespace,
        getVlmGeneralInfo(vendorLicenseModel),
        null,
        null,
        null);
    return vspElements;
  }


  public static ItemVersionData getItemVersionData(
      VendorLicenseModelEntity vendorLicenseModel) {
    ItemVersionData itemVersionData = new ItemVersionData();
    Info info = new Info();
    info.setName("main version");
    itemVersionData.setInfo(info);
    return itemVersionData;
  }

  public static Info getVlmInfo(VendorLicenseModelEntity vendorLicenseModel) {

    Info info = new Info();
    info.setName(vendorLicenseModel.getVendorName());
    info.setDescription(vendorLicenseModel.getDescription());
    info.addProperty("type", "vlm");
    info.addProperty("version", vendorLicenseModel.getVersion());
    info.addProperty("iconRef", vendorLicenseModel.getIconRef());
    return info;
  }

  private static List<String> getVlmNamespace(VendorLicenseModelEntity vlmEntity) {
    return ElementHandler.getElementPath("");
  }

  private static Info getVlmGeneralInfo(VendorLicenseModelEntity vendorLicenseModel) {


    Info info = new Info();
    info.setName(StructureElement.General.name());
    info.addProperty(InfoPropertyName.type.name(), InfoPropertyName.vlm.name());
    info.addProperty(InfoPropertyName.iconRef.name(), vendorLicenseModel.getIconRef());
    info.addProperty(InfoPropertyName.name.name(), vendorLicenseModel.getVendorName());
    info.addProperty(InfoPropertyName.description.name(), vendorLicenseModel.getDescription());
    return info;
  }

  public static ElementEntityContext convertVlmToElementContext(VendorLicenseModelEntity
                                                                   vendorLicenseModel) {

    return new ElementEntityContext("GLOBAL_USER", new
        ElementContext(vendorLicenseModel.getId(), vendorLicenseModel.getVersion().toString()));
  }


  private enum InfoPropertyName {
    name,
    description,
    iconRef,
    type,
    vlm
  }

}
