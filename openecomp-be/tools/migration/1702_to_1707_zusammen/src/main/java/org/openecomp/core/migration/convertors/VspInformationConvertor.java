package org.openecomp.core.migration.convertors;

import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.amdocs.zusammen.sdk.collaboration.types.CollaborationElement;
import org.openecomp.core.migration.loaders.VspInformation;
import org.openecomp.core.migration.store.ElementHandler;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.StructureElement;

import java.util.List;

public class VspInformationConvertor {

  public static ItemVersionData getItemVersionData(
      VspInformation vspInformation) {
    Info info = new Info();
    info.setName("main version");
    ItemVersionData itemVersionData = new ItemVersionData();
    itemVersionData.setInfo(info);
    return itemVersionData;
  }

  public static Info getVspInfo(VspInformation vspInformation) {

    Info info = new Info();
    info.setName(vspInformation.getName());
    info.setDescription(vspInformation.getDescription());
    info.addProperty("type", "vsp");
    addVspDetailsToInfo(info, vspInformation);
    return info;
  }

  private static List<String> getVspNamespace(VendorLicenseModelEntity vlmEntity) {
    return ElementHandler.getElementPath("");
  }

  private static void addVspDetailsToInfo(Info info, VspInformation vspInformation) {
    info.addProperty("name", vspInformation.getName());
    info.addProperty("description", vspInformation.getDescription());
    info.addProperty("category", vspInformation.getCategory());
    info.addProperty("subCategory", vspInformation.getSubCategory());
    info.addProperty("vendorId", vspInformation.getVendorId());
    info.addProperty("vendorName", vspInformation.getVendorName());
    if (vspInformation.getVlmVersion() != null) {
      info.addProperty("vendorVersion", vspInformation.getVlmVersion().toString());
    }
    info.addProperty("featureGroups", vspInformation.getFeatureGroups());
    info.addProperty("licenseAgreement", vspInformation.getLicenseAgreement());
    String oldVersion = vspInformation.getIsOldVersion() == null? "1702":"1610";
    info.addProperty("oldVersion", oldVersion);
  }

  public static CollaborationElement[] convertVspToElement(VspInformation vspInformation) {

    CollaborationElement[] vspElements = new CollaborationElement[4];
    List<String> vspNamespace = getVspNamespace(vspInformation);


    String vspEntityId = StructureElement.General.name();

    vspElements[0] = ElementHandler.getElementEntity(
        vspInformation.getId(), vspInformation.getVersion().toString(),
        vspEntityId,
        vspNamespace,
        getVspGeneralInfo(vspInformation),
        null,
        null,
        null);

    String vspOrchestrationTemplateEntityId = StructureElement.OrchestrationTemplate.name();
    vspElements[1] = ElementHandler.getElementEntity(
        vspInformation.getId(), vspInformation.getVersion().toString(),
        vspOrchestrationTemplateEntityId,
        vspNamespace,
        ElementHandler.getStructuralElementInfo(vspOrchestrationTemplateEntityId),
        null,
        null,
        null);


    vspNamespace.add(vspOrchestrationTemplateEntityId);

    String vspOrchestrationTemplateValidationDataEntityId = StructureElement.OrchestrationTemplateValidationData.name();
    vspElements[2] = ElementHandler.getElementEntity(
        vspInformation.getId(), vspInformation.getVersion().toString(),
        vspOrchestrationTemplateValidationDataEntityId,
        vspNamespace,
        ElementHandler.getStructuralElementInfo(vspOrchestrationTemplateValidationDataEntityId),
        null,
        null,
        vspInformation.getValidationData()!= null?vspInformation.getValidationData().getBytes()
            :null);

    String vspOrchestrationTemplateContentEntityId = StructureElement.OrchestrationTemplateContent.name();
    vspElements[3] = ElementHandler.getElementEntity(
        vspInformation.getId(), vspInformation.getVersion().toString(),
        vspOrchestrationTemplateContentEntityId,
        vspNamespace,
        ElementHandler.getStructuralElementInfo(vspOrchestrationTemplateContentEntityId),
        null,
        null,
        vspInformation.getContentData()!= null?vspInformation.getContentData().array()
            :null);

    return vspElements;
  }

  private static Info getVspGeneralInfo(VspInformation vspInformation) {


    Info info = new Info();
    info.setName(StructureElement.General.name());
    info.addProperty("name", vspInformation.getName());
    info.addProperty("description", vspInformation.getDescription());
    info.addProperty("category", vspInformation.getCategory());
    info.addProperty("subCategory", vspInformation.getSubCategory());
    info.addProperty("vendorId", vspInformation.getVendorId());
    info.addProperty("vendorName", vspInformation.getVendorName());
    if (vspInformation.getVlmVersion() != null) {
      info.addProperty("vendorVersion", vspInformation.getVlmVersion().toString());
    }
    info.addProperty("featureGroups", vspInformation.getFeatureGroups());
    info.addProperty("licenseAgreement", vspInformation.getLicenseAgreement());
    String oldVersion = vspInformation.getIsOldVersion() == null? "1702":"1610";
    info.addProperty("oldVersion", oldVersion);
    return info;
  }

  private static List<String> getVspNamespace(VspInformation vspEntity) {
    return ElementHandler.getElementPath("");
  }

  public static ElementEntityContext convertVspToElementContext(VspInformation vspEntity) {

    return new ElementEntityContext("GLOBAL_USER", new
        ElementContext(vspEntity.getId(), vspEntity.getVersion().toString()));
  }
}
