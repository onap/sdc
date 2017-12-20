package org.openecomp.sdc.vendorlicense.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorlicense.dao.VendorLicenseModelDao;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.convertor.ElementToVLMGeneralConvertor;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;
import org.openecomp.sdc.versioning.types.VersionableEntityStoreType;

import java.util.Collection;
import java.util.stream.Collectors;

public class VendorLicenseModelDaoZusammenImpl implements VendorLicenseModelDao {

  private ZusammenAdaptor zusammenAdaptor;

  public VendorLicenseModelDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {
    VersionableEntityMetadata metadata =
        new VersionableEntityMetadata(VersionableEntityStoreType.Zusammen, "VendorLicenseModel",
            null, null);

    VersioningManagerFactory.getInstance().createInterface()
        .register(versionableEntityType, metadata);
  }

  @Override
  public Collection<VendorLicenseModelEntity> list(
      VendorLicenseModelEntity vendorLicenseModelEntity) {

    ElementToVLMGeneralConvertor convertor = new ElementToVLMGeneralConvertor();
    return zusammenAdaptor.listItems(ZusammenUtil.createSessionContext()).stream()
        .filter(item -> "VendorLicenseModel".equals(item.getInfo().getProperty("item_type")))
        .map(item -> {
          VendorLicenseModelEntity entity = convertor.convert(item);
          entity.setId(item.getId().getValue());
          entity.setVersion(null);
          return entity;
        })
        .collect(Collectors.toList());
  }

  @Override
  public void create(VendorLicenseModelEntity vendorLicenseModel) {

    SessionContext context = ZusammenUtil.createSessionContext();

    ElementContext elementContext = new ElementContext(vendorLicenseModel.getId(),
        vendorLicenseModel.getVersion().getId());

    ZusammenElement generalElement = mapVlmToZusammenElement(vendorLicenseModel, Action.CREATE);

    zusammenAdaptor.saveElement(context, elementContext, generalElement,
        "Create VLM General Info Element");

    ZusammenElement licenseAgreementsElement =
        ZusammenUtil.buildStructuralElement(ElementType.LicenseAgreements,  Action.CREATE);

    zusammenAdaptor.saveElement(context, elementContext, licenseAgreementsElement,
        "Create VLM licenseAgreementsElement");

    ZusammenElement featureGroupsElement =
        ZusammenUtil.buildStructuralElement(ElementType.FeatureGroups,  Action.CREATE);

    zusammenAdaptor.saveElement(context, elementContext, featureGroupsElement,
        "Create VLM featureGroupsElement");

    ZusammenElement lkgsElement =
        ZusammenUtil.buildStructuralElement(ElementType.LicenseKeyGroups,  Action.CREATE);

    zusammenAdaptor.saveElement(context, elementContext, lkgsElement,
        "Create VLM lkgsElement");

    ZusammenElement entitlementPoolsElement =
        ZusammenUtil.buildStructuralElement(ElementType.EntitlementPools,  Action.CREATE);

    zusammenAdaptor.saveElement(context, elementContext, entitlementPoolsElement,
        "Create VLM entitlementPoolsElement");
  }

  @Override
  public void update(VendorLicenseModelEntity vendorLicenseModel) {
    ZusammenElement generalElement = mapVlmToZusammenElement(vendorLicenseModel, Action.UPDATE);

    SessionContext context = ZusammenUtil.createSessionContext();
    zusammenAdaptor.saveElement(context,
        new ElementContext(vendorLicenseModel.getId(), vendorLicenseModel.getVersion().getId()),
        generalElement, "Update VSP General Info Element");
  }

  @Override
  public VendorLicenseModelEntity get(VendorLicenseModelEntity vendorLicenseModel) {
    SessionContext context = ZusammenUtil.createSessionContext();
    ElementContext elementContext =
        new ElementContext(vendorLicenseModel.getId(), vendorLicenseModel.getVersion().getId());
    ElementToVLMGeneralConvertor convertor = new ElementToVLMGeneralConvertor();
    return zusammenAdaptor
        .getElementInfoByName(context, elementContext, null, ElementType.VendorLicenseModel.name())
        .map(generalElementInfo -> {
          VendorLicenseModelEntity entity = convertor.convert(generalElementInfo);
          entity.setId(vendorLicenseModel.getId());
          entity.setVersion(vendorLicenseModel.getVersion());
          return entity;
        })
        .orElse(null);
  }

  @Override
  public void delete(VendorLicenseModelEntity entity) {

  }

 /* private Info mapVlmToZusammenItemInfo(VendorLicenseModelEntity vendorLicenseModel) {
    Info info = new Info();
    info.setName(vendorLicenseModel.getVendorName());
    info.setDescription(vendorLicenseModel.getDescription());
    info.addProperty("type", "VendorLicenseModel");
    addVlmToInfo(info, vendorLicenseModel);
    return info;
  }*/

  private ZusammenElement mapVlmToZusammenElement(VendorLicenseModelEntity vendorLicenseModel,
                                                  Action action) {
    ZusammenElement generalElement =
        ZusammenUtil.buildStructuralElement(ElementType.VendorLicenseModel, action);
    addVlmToInfo(generalElement.getInfo(), vendorLicenseModel);
    return generalElement;
  }

  private void addVlmToInfo(Info info, VendorLicenseModelEntity vendorLicenseModel) {
    info.addProperty(InfoPropertyName.name.name(), vendorLicenseModel.getVendorName());
    info.addProperty(InfoPropertyName.description.name(), vendorLicenseModel.getDescription());
    info.addProperty(InfoPropertyName.iconRef.name(), vendorLicenseModel.getIconRef());
    info.addProperty(InfoPropertyName.oldVersion.name(), vendorLicenseModel.getOldVersion());
  }

  /*private VendorLicenseModelEntity mapInfoToVlm(String vlmId, Version version, Info info) {
    VendorLicenseModelEntity vendorLicenseModel = new VendorLicenseModelEntity(vlmId, version);
    vendorLicenseModel.setVendorName(info.getProperty(InfoPropertyName.name.name()));
    vendorLicenseModel.setDescription(info.getProperty(InfoPropertyName.description.name()));
    vendorLicenseModel.setIconRef(info.getProperty(InfoPropertyName.iconRef.name()));
    return vendorLicenseModel;
  }*/

  public enum InfoPropertyName {
    name,
    description,
    iconRef,
    oldVersion
  }
}
