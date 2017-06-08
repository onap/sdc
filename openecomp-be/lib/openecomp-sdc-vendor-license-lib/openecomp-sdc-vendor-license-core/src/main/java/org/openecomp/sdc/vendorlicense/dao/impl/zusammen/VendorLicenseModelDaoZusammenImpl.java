package org.openecomp.sdc.vendorlicense.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.vendorlicense.dao.VendorLicenseModelDao;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;
import org.openecomp.sdc.versioning.types.VersionableEntityStoreType;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

public class VendorLicenseModelDaoZusammenImpl implements VendorLicenseModelDao {

  private ZusammenAdaptor zusammenAdaptor;

  public VendorLicenseModelDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {
    VersionableEntityMetadata metadata =
        new VersionableEntityMetadata(VersionableEntityStoreType.Zusammen, "vlm", null, null);

    VersioningManagerFactory.getInstance().createInterface()
        .register(versionableEntityType, metadata);
  }

  @Override
  public Collection<VendorLicenseModelEntity> list(VendorLicenseModelEntity entity) {
    return zusammenAdaptor.listItems(ZusammenUtil.createSessionContext()).stream()
        .map(item -> mapInfoToVlm(
            item.getId().getValue(), null, item.getInfo(),
            item.getModificationTime(), item.getCreationTime()))
        .collect(Collectors.toList());
  }

  @Override
  public void create(VendorLicenseModelEntity vendorLicenseModel) {
    SessionContext context = ZusammenUtil.createSessionContext();

    Id itemId = zusammenAdaptor.createItem(context, mapVlmToZusammenItemInfo(vendorLicenseModel));
    Id versionId =
        zusammenAdaptor.createVersion(context, itemId, null, ZusammenUtil.createFirstVersionData());

    ZusammenElement generalElement = mapVlmToZusammenElement(vendorLicenseModel, Action.CREATE);
    zusammenAdaptor.saveElement(context, new ElementContext(itemId, versionId),
        generalElement, "Create VSP General Info Element");

    vendorLicenseModel.setId(itemId.getValue());//set id for caller
  }

  @Override
  public void update(VendorLicenseModelEntity vendorLicenseModel) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vendorLicenseModel.getId());
    Id versionId = VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor);

    zusammenAdaptor.updateItem(context, itemId, mapVlmToZusammenItemInfo(vendorLicenseModel));

    ZusammenElement generalElement = mapVlmToZusammenElement(vendorLicenseModel, Action.UPDATE);
    zusammenAdaptor.saveElement(context, new ElementContext(itemId, versionId),
        generalElement, "Update VSP General Info Element");
  }

  @Override
  public VendorLicenseModelEntity get(VendorLicenseModelEntity vendorLicenseModel) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vendorLicenseModel.getId());
    ItemVersion itemVersion = VlmZusammenUtil.getFirstVersion(context, itemId, zusammenAdaptor);
    ElementContext elementContext = new ElementContext(itemId, itemVersion.getId(),
        VlmZusammenUtil.getVersionTag(vendorLicenseModel.getVersion()));

    return zusammenAdaptor
        .getElementInfoByName(context, elementContext, null, StructureElement.General.name())
        .map(generalElementInfo -> mapInfoToVlm(
            vendorLicenseModel.getId(), vendorLicenseModel.getVersion(),
            generalElementInfo.getInfo(),
            itemVersion.getModificationTime(), itemVersion.getCreationTime()))
        .orElse(null);
  }

  @Override
  public void delete(VendorLicenseModelEntity entity) {

  }

  private Info mapVlmToZusammenItemInfo(VendorLicenseModelEntity vendorLicenseModel) {
    Info info = new Info();
    info.setName(vendorLicenseModel.getVendorName());
    info.setDescription(vendorLicenseModel.getDescription());
    info.addProperty("type", "vlm");
    addVlmToInfo(info, vendorLicenseModel);
    return info;
  }

  private ZusammenElement mapVlmToZusammenElement(VendorLicenseModelEntity vendorLicenseModel,
                                                  Action action) {
    ZusammenElement generalElement =
        VlmZusammenUtil.buildStructuralElement(StructureElement.General, action);
    addVlmToInfo(generalElement.getInfo(), vendorLicenseModel);
    return generalElement;
  }

  private void addVlmToInfo(Info info, VendorLicenseModelEntity vendorLicenseModel) {
    info.addProperty(InfoPropertyName.name.name(), vendorLicenseModel.getVendorName());
    info.addProperty(InfoPropertyName.description.name(), vendorLicenseModel.getDescription());
    info.addProperty(InfoPropertyName.iconRef.name(), vendorLicenseModel.getIconRef());
  }

  private VendorLicenseModelEntity mapInfoToVlm(String vlmId, Version version, Info info,
                                                Date modificationTime, Date creationTime) {
    VendorLicenseModelEntity vendorLicenseModel = new VendorLicenseModelEntity(vlmId, version);
    vendorLicenseModel.setVendorName(info.getProperty(InfoPropertyName.name.name()));
    vendorLicenseModel.setDescription(info.getProperty(InfoPropertyName.description.name()));
    vendorLicenseModel.setIconRef(info.getProperty(InfoPropertyName.iconRef.name()));
    vendorLicenseModel.setWritetimeMicroSeconds(
        modificationTime == null ? creationTime.getTime() : modificationTime.getTime());
    return vendorLicenseModel;
  }

  private enum InfoPropertyName {
    name,
    description,
    iconRef
  }
}
