package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.utils.fileutils.FileUtils;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;
import org.openecomp.sdc.versioning.types.VersionableEntityStoreType;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

public class VendorSoftwareProductInfoDaoZusammenImpl implements VendorSoftwareProductInfoDao {
  private ZusammenAdaptor zusammenAdaptor;

  public VendorSoftwareProductInfoDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {
    VersionableEntityMetadata metadata =
        new VersionableEntityMetadata(VersionableEntityStoreType.Zusammen, "vsp", null, null);

    VersioningManagerFactory.getInstance().createInterface()
        .register(versionableEntityType, metadata);
  }

  @Override
  public Collection<VspDetails> list(VspDetails entity) {
    return zusammenAdaptor.listItems(ZusammenUtil.createSessionContext()).stream().filter
        (vspEntity-> "vsp".equals(vspEntity.getInfo().getProperty("type")))
        .map(item -> mapInfoToVspDetails(
            item.getId().getValue(), null, item.getInfo(),
            item.getModificationTime(), item.getCreationTime()))
        .collect(Collectors.toList());
  }

  @Override
  public void create(VspDetails vspDetails) {
    SessionContext context = ZusammenUtil.createSessionContext();

    Id itemId = zusammenAdaptor.createItem(context, mapVspDetailsToZusammenItemInfo(vspDetails));
    Id versionId =
        zusammenAdaptor.createVersion(context, itemId, null, ZusammenUtil.createFirstVersionData());

    ZusammenElement generalElement = mapVspDetailsToZusammenElement(vspDetails, Action.CREATE);
    zusammenAdaptor.saveElement(context, new ElementContext(itemId, versionId),
        generalElement, "Create VSP General Info Element");

    vspDetails.setId(itemId.getValue());//set id for caller
  }

  @Override
  public void update(VspDetails vspDetails) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspDetails.getId());
    Id versionId = VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor);

    zusammenAdaptor.updateItem(context, itemId, mapVspDetailsToZusammenItemInfo(vspDetails));

    ZusammenElement generalElement = mapVspDetailsToZusammenElement(vspDetails, Action.UPDATE);
    zusammenAdaptor.saveElement(context, new ElementContext(itemId, versionId),
        generalElement, "Update VSP General Info Element");
  }

  @Override
  public VspDetails get(VspDetails vspDetails) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspDetails.getId());
    ItemVersion itemVersion = VspZusammenUtil.getFirstVersion(context, itemId, zusammenAdaptor);
    ElementContext elementContext = new ElementContext(itemId, itemVersion.getId(),
        VspZusammenUtil.getVersionTag(vspDetails.getVersion()));

    return zusammenAdaptor
        .getElementInfoByName(context, elementContext, null, StructureElement.General.name())
        .map(generalElementInfo -> mapInfoToVspDetails(
            vspDetails.getId(), vspDetails.getVersion(), generalElementInfo.getInfo(),
            itemVersion.getModificationTime(), itemVersion.getCreationTime()))
        .orElse(null);
  }


  @Override
  public void delete(VspDetails entity) {

  }

  @Override
  public void updateOldVersionIndication(VspDetails vspDetails) {
    VspDetails retrieved = get(vspDetails);
    if (retrieved != null) {
      retrieved.setOldVersion(vspDetails.getOldVersion());
      update(retrieved);
    }
  }

  @Override
  public void updateQuestionnaireData(String vspId, Version version, String questionnaireData) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    Id versionId = VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor);

    ZusammenElement questionnaireElement = mapQuestionnaireToZusammenElement(questionnaireData);
    zusammenAdaptor.saveElement(context, new ElementContext(itemId, versionId),
        questionnaireElement, "Update VSP Questionnaire");
  }


  @Override
  public String getQuestionnaireData(String vspId, Version version) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    Id versionId = VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor);

    return zusammenAdaptor.getElementByName(context,
        new ElementContext(itemId, versionId, VspZusammenUtil.getVersionTag(version)), null,
        StructureElement.Questionnaire.name())
        .map(questionnaireElement ->
            new String(FileUtils.toByteArray(questionnaireElement.getData())))
        .orElse(null);
  }

  @Override
  public VspQuestionnaireEntity getQuestionnaire(String vspId, Version version) {
    VspQuestionnaireEntity entity = new VspQuestionnaireEntity();
    entity.setId(vspId);
    entity.setVersion(version);
    entity.setQuestionnaireData(getQuestionnaireData(vspId, version));
    return entity;
  }

  @Override
  public void deleteAll(String vspId, Version version) {

  }

  private Info mapVspDetailsToZusammenItemInfo(VspDetails vspDetails) {
    Info info = new Info();
    info.setName(vspDetails.getName());
    info.setDescription(vspDetails.getDescription());
    info.addProperty("type", "vsp");
    addVspDetailsToInfo(info, vspDetails);
    return info;
  }

  private ZusammenElement mapVspDetailsToZusammenElement(VspDetails vspDetails, Action action) {
    ZusammenElement generalElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.General, action);
    addVspDetailsToInfo(generalElement.getInfo(), vspDetails);
    return generalElement;
  }

  private ZusammenElement mapQuestionnaireToZusammenElement(String questionnaireData) {
    ZusammenElement questionnaireElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.Questionnaire, Action.UPDATE);
    questionnaireElement.setData(new ByteArrayInputStream(questionnaireData.getBytes()));
    return questionnaireElement;
  }

  private void addVspDetailsToInfo(Info info, VspDetails vspDetails) {
    info.addProperty(InfoPropertyName.name.name(), vspDetails.getName());
    info.addProperty(InfoPropertyName.description.name(), vspDetails.getDescription());
    info.addProperty(InfoPropertyName.icon.name(), vspDetails.getIcon());
    info.addProperty(InfoPropertyName.category.name(), vspDetails.getCategory());
    info.addProperty(InfoPropertyName.subCategory.name(), vspDetails.getSubCategory());
    info.addProperty(InfoPropertyName.vendorId.name(), vspDetails.getVendorId());
    info.addProperty(InfoPropertyName.vendorName.name(), vspDetails.getVendorName());
    if (vspDetails.getVlmVersion() != null) {
      info.addProperty(
          InfoPropertyName.vendorVersion.name(), vspDetails.getVlmVersion().toString());
    }
    info.addProperty(InfoPropertyName.licenseAgreement.name(), vspDetails.getLicenseAgreement());
    info.addProperty(InfoPropertyName.featureGroups.name(), vspDetails.getFeatureGroups());
    info.addProperty(InfoPropertyName.oldVersion.name(), vspDetails.getOldVersion());
  }

  private VspDetails mapInfoToVspDetails(String vspId, Version version, Info info,
                                         Date modificationTime, Date creationTime) {
    VspDetails vspDetails = new VspDetails(vspId, version);
    vspDetails.setName(info.getProperty(InfoPropertyName.name.name()));
    vspDetails.setDescription(info.getProperty(InfoPropertyName.description.name()));
    vspDetails.setCategory(info.getProperty(InfoPropertyName.category.name()));
    vspDetails.setSubCategory(info.getProperty(InfoPropertyName.subCategory.name()));
    vspDetails.setVendorId(info.getProperty(InfoPropertyName.vendorId.name()));
    vspDetails.setVendorName(info.getProperty(InfoPropertyName.vendorName.name()));
    vspDetails.setVlmVersion(
        Version.valueOf(info.getProperty(InfoPropertyName.vendorVersion.name())));
    vspDetails.setLicenseAgreement(info.getProperty(InfoPropertyName.licenseAgreement.name()));
    vspDetails.setFeatureGroups(info.getProperty(InfoPropertyName.featureGroups.name()));
    vspDetails.setWritetimeMicroSeconds(
        modificationTime == null ? creationTime.getTime() : modificationTime.getTime());
    vspDetails.setVersion(version);
    String oldVersion = info.getProperty(InfoPropertyName.oldVersion.name());

    //Boolean oldVersion = ind == null || "true".equals( ind.toLowerCase());
    vspDetails.setOldVersion(oldVersion);
    return vspDetails;
  }

  private enum InfoPropertyName {
    name,
    description,
    icon,
    category,
    subCategory,
    vendorId,
    vendorName,
    vendorVersion,
    licenseAgreement,
    featureGroups,
    oldVersion
  }

}
