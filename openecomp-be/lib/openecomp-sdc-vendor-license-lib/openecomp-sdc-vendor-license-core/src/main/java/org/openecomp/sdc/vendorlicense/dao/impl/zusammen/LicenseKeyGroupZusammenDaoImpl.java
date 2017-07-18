package org.openecomp.sdc.vendorlicense.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.vendorlicense.dao.LicenseKeyGroupDao;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyType;
import org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther;
import org.openecomp.sdc.vendorlicense.dao.types.OperationalScope;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by ayalaben on 3/30/2017.
 */
public class LicenseKeyGroupZusammenDaoImpl implements LicenseKeyGroupDao {
  private ZusammenAdaptor zusammenAdaptor;

  public LicenseKeyGroupZusammenDaoImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {
    //no need
  }

  @Override
  public void create(LicenseKeyGroupEntity licenseKeyGroup) {
    ZusammenElement licenseKeyGroupElement =
        buildLicenseKeyGroupElement(licenseKeyGroup, Action.CREATE);

    ZusammenElement lkgsElement =
        VlmZusammenUtil.buildStructuralElement(StructureElement.LicenseKeyGroups, null);
    lkgsElement.addSubElement(licenseKeyGroupElement);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(licenseKeyGroup.getVendorLicenseModelId());
    Optional<Element> savedElement = zusammenAdaptor.saveElement(context, new ElementContext(itemId,
            VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor)),
        lkgsElement, "Create license Key Group");

    savedElement.ifPresent(element -> licenseKeyGroup
        .setId(element.getSubElements().iterator().next().getElementId().getValue()));
  }

  @Override
  public void update(LicenseKeyGroupEntity licenseKeyGroup) {
    ZusammenElement licenseKeyGroupElement =
        buildLicenseKeyGroupElement(licenseKeyGroup, Action.UPDATE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(licenseKeyGroup.getVendorLicenseModelId());

    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    Optional<ElementInfo> lkgFromDb = zusammenAdaptor.getElementInfo(context, elementContext,
        new Id(licenseKeyGroup.getId()));

    if(lkgFromDb.isPresent()) {

      if( licenseKeyGroupElement.getRelations() == null) {
         licenseKeyGroupElement.setRelations(new ArrayList<>());
      }

      if (lkgFromDb.get().getRelations() != null && lkgFromDb.get().getRelations().size() > 0) {
        licenseKeyGroupElement.getRelations().addAll(lkgFromDb.get().getRelations());
      }
    }

    zusammenAdaptor.saveElement(context, elementContext,
        licenseKeyGroupElement,
        String.format("Update license key group with id %s", licenseKeyGroup.getId()));
  }

  @Override
  public LicenseKeyGroupEntity get(LicenseKeyGroupEntity licenseKeyGroup) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(licenseKeyGroup.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VlmZusammenUtil.getVersionTag(licenseKeyGroup.getVersion()));

    return zusammenAdaptor.getElementInfo(context, elementContext, new Id(licenseKeyGroup.getId()))
        .map(elementInfo -> mapElementInfoToLicenseKeyGroup(
            licenseKeyGroup.getVendorLicenseModelId(), licenseKeyGroup.getVersion(), elementInfo))
        .orElse(null);
  }

  @Override
  public void delete(LicenseKeyGroupEntity licenseKeyGroup) {
    SessionContext context = ZusammenUtil.createSessionContext();
    ZusammenElement zusammenElement = new ZusammenElement();
    zusammenElement.setAction(Action.DELETE);
    zusammenElement.setElementId(new Id(licenseKeyGroup.getId()));

    Id itemId = new Id(licenseKeyGroup.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    zusammenAdaptor.saveElement(context, elementContext, zusammenElement,
        "delete license key group. id:" + licenseKeyGroup.getId() + ".");
  }

  @Override
  public Collection<LicenseKeyGroupEntity> list(LicenseKeyGroupEntity licenseKeyGroup) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(licenseKeyGroup.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VlmZusammenUtil.getVersionTag(licenseKeyGroup.getVersion()));

    return zusammenAdaptor
        .listElementsByName(context, elementContext, null, StructureElement.LicenseKeyGroups.name())
        .stream().map(elementInfo -> mapElementInfoToLicenseKeyGroup(
            licenseKeyGroup.getVendorLicenseModelId(), licenseKeyGroup.getVersion(), elementInfo))
        .collect(Collectors.toList());
  }

  @Override
  public long count(LicenseKeyGroupEntity licenseKeyGroup) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(licenseKeyGroup.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VlmZusammenUtil.getVersionTag(licenseKeyGroup.getVersion()));

    return zusammenAdaptor
        .listElementsByName(context, elementContext, null, StructureElement.LicenseKeyGroups.name())
        .size();
  }

  @Override
  public void deleteAll(LicenseKeyGroupEntity licenseKeyGroup) {
    //not supported
  }

  @Override
  public void removeReferencingFeatureGroup(LicenseKeyGroupEntity licenseKeyGroup,
                                            String featureGroupId) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(licenseKeyGroup.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    Optional<ElementInfo> elementInfo =
        zusammenAdaptor.getElementInfo(context, elementContext, new Id(licenseKeyGroup.getId()));

    if (elementInfo.isPresent()) {
      ZusammenElement zusammenElement = VlmZusammenUtil.getZusammenElement(elementInfo.get());
      zusammenElement.setAction(Action.UPDATE);
      zusammenElement.setRelations(elementInfo.get().getRelations().stream()
          .filter(relation -> !featureGroupId
              .equals(relation.getEdge2().getElementId().getValue()))
          .collect(Collectors.toList()));

      zusammenAdaptor.saveElement(context, elementContext, zusammenElement,
          "remove referencing feature group");
    }
  }

  @Override
  public void addReferencingFeatureGroup(LicenseKeyGroupEntity licenseKeyGroup,
                                         String featureGroupId) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(licenseKeyGroup.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    Optional<ElementInfo> elementInfo =
        zusammenAdaptor.getElementInfo(context, elementContext, new Id(licenseKeyGroup.getId()));

    if (elementInfo.isPresent()) {
      ZusammenElement zusammenElement = VlmZusammenUtil.getZusammenElement(elementInfo.get());
      zusammenElement.setAction(Action.UPDATE);
      if (zusammenElement.getRelations() == null) {
        zusammenElement.setRelations(new ArrayList<>());
      }
      zusammenElement.getRelations().add(VlmZusammenUtil
          .createRelation(RelationType.LicenseKeyGroupToReferencingFeatureGroup,
              featureGroupId));
      zusammenAdaptor
          .saveElement(context, elementContext, zusammenElement, "add referencing feature group");
    }
  }

  private ZusammenElement buildLicenseKeyGroupElement(LicenseKeyGroupEntity licenseKeyGroup,
                                                      Action action) {

    ZusammenElement lkgElement = new ZusammenElement();
    lkgElement.setAction(action);
    if (licenseKeyGroup.getId() != null) {
      lkgElement.setElementId(new Id(licenseKeyGroup.getId()));
    }
    Info info = new Info();
    info.setName(licenseKeyGroup.getName());
    info.setDescription(licenseKeyGroup.getDescription());
    info.addProperty("LicenseKeyType", licenseKeyGroup.getType());
    info.addProperty("operational_scope", licenseKeyGroup.getOperationalScope());
    lkgElement.setInfo(info);

   if (licenseKeyGroup.getReferencingFeatureGroups() != null
       && licenseKeyGroup.getReferencingFeatureGroups().size() > 0) {
      lkgElement.setRelations(licenseKeyGroup.getReferencingFeatureGroups().stream()
          .map(rel -> VlmZusammenUtil
              .createRelation(RelationType.LicenseKeyGroupToReferencingFeatureGroup, rel))
          .collect(Collectors.toList()));
    }

    return lkgElement;
  }

  private LicenseKeyGroupEntity mapElementInfoToLicenseKeyGroup(String vlmId, Version version,
                                                                ElementInfo elementInfo) {
    LicenseKeyGroupEntity licenseKeyGroup =
        new LicenseKeyGroupEntity(vlmId, version, elementInfo.getId().getValue());
    licenseKeyGroup.setName(elementInfo.getInfo().getName());
    licenseKeyGroup.setDescription(elementInfo.getInfo().getDescription());

    licenseKeyGroup
        .setType(LicenseKeyType.valueOf(elementInfo.getInfo().getProperty("LicenseKeyType")));
    licenseKeyGroup.setOperationalScope(getOperationalScopeMultiChoiceOrOther(
        elementInfo.getInfo().getProperty("operational_scope")));

    if (elementInfo.getRelations() != null && elementInfo.getRelations().size() > 0) {
      licenseKeyGroup
          .setReferencingFeatureGroups(elementInfo.getRelations().stream().map(relation -> relation
              .getEdge2().getElementId().getValue()).collect(Collectors.toSet()));
    }
    return licenseKeyGroup;
  }

  private MultiChoiceOrOther<OperationalScope> getOperationalScopeMultiChoiceOrOther
      (Map<String, Object>
           operationalScope) {
  if(operationalScope != null && !operationalScope.isEmpty()) {
    Set<OperationalScope> choices = new HashSet<>();
    ((List<String>) operationalScope.get("choices"))
        .forEach(choice -> choices.add(OperationalScope.valueOf(choice)));

    return new MultiChoiceOrOther<>(choices, operationalScope.get("other")==null?null:(String) operationalScope.get("other"));
  }
  return null;
  }
}
