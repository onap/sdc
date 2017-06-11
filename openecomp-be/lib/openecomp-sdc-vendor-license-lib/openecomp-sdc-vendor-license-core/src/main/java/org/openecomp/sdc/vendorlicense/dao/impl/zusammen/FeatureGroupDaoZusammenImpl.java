package org.openecomp.sdc.vendorlicense.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Relation;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.vendorlicense.dao.FeatureGroupDao;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by ayalaben on 3/27/2017.
 */
public class FeatureGroupDaoZusammenImpl implements FeatureGroupDao {

  private ZusammenAdaptor zusammenAdaptor;

  public FeatureGroupDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {
    //no need
  }

  @Override
  public void create(FeatureGroupEntity featureGroup) {
    ZusammenElement featureGroupElement =
        buildFeatureGroupElement(featureGroup, Action.CREATE);

    ZusammenElement featureGroupsElement =
        VlmZusammenUtil.buildStructuralElement(StructureElement.FeatureGroups, null);

    featureGroupsElement.addSubElement(featureGroupElement);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(featureGroup.getVendorLicenseModelId());
    Optional<Element> savedElement = zusammenAdaptor.saveElement(context, new ElementContext(itemId,
            VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor)),
        featureGroupsElement, "Create feature group");

    savedElement.ifPresent(element -> featureGroup
        .setId(element.getSubElements().iterator().next().getElementId().getValue()));
  }

  @Override
  public void update(FeatureGroupEntity featureGroup) {
    ZusammenElement featureGroupElement = buildFeatureGroupElement(featureGroup, Action.UPDATE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(featureGroup.getVendorLicenseModelId());
    zusammenAdaptor.saveElement(context, new ElementContext(itemId,
            VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor)), featureGroupElement,
        String.format("Update feature group with id %s", featureGroup.getId()));
  }

  @Override
  public FeatureGroupEntity get(FeatureGroupEntity featureGroup) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(featureGroup.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VlmZusammenUtil.getVersionTag(featureGroup.getVersion()));

    return zusammenAdaptor.getElementInfo(context, elementContext, new Id(featureGroup.getId()))
        .map(elementInfo -> mapElementInfoToFeatureGroup(
            featureGroup.getVendorLicenseModelId(), featureGroup.getVersion(), elementInfo))
        .orElse(null);
  }

  @Override
  public void delete(FeatureGroupEntity featureGroup) {
    SessionContext context = ZusammenUtil.createSessionContext();
    ZusammenElement zusammenElement = new ZusammenElement();
    zusammenElement.setAction(Action.DELETE);
    zusammenElement.setElementId(new Id(featureGroup.getId()));

    Id itemId = new Id(featureGroup.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));
    zusammenAdaptor.saveElement(context, elementContext, zusammenElement,
        "delete feature group. id:" + featureGroup.getId() + ".");
  }

  @Override
  public Collection<FeatureGroupEntity> list(FeatureGroupEntity featureGroup) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(featureGroup.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VlmZusammenUtil.getVersionTag(featureGroup.getVersion()));

    return zusammenAdaptor
        .listElementsByName(context, elementContext, null, StructureElement.FeatureGroups.name())
        .stream().map(elementInfo -> mapElementInfoToFeatureGroup(
            featureGroup.getVendorLicenseModelId(), featureGroup.getVersion(), elementInfo))
        .collect(Collectors.toList());
  }

  @Override
  public long count(FeatureGroupEntity featureGroup) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(featureGroup.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VlmZusammenUtil.getVersionTag(featureGroup.getVersion()));

    return zusammenAdaptor
        .listElementsByName(context, elementContext, null, StructureElement.FeatureGroups.name())
        .size();
  }

  @Override
  public void removeEntitlementPool(FeatureGroupEntity featureGroup, String entitlementPoolId) {
    removeRelationToContainedEntity(featureGroup, entitlementPoolId, "entitlement pool");
  }

  @Override
  public void removeLicenseKeyGroup(FeatureGroupEntity featureGroup, String licenseKeyGroupId) {
    removeRelationToContainedEntity(featureGroup, licenseKeyGroupId, "license Key Group");
  }

  private void removeRelationToContainedEntity(FeatureGroupEntity featureGroup,
                                               String containedEntityId,
                                               String containedEntityType) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(featureGroup.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    Optional<ElementInfo> elementInfo = zusammenAdaptor.getElementInfo(context,
        elementContext, new Id(featureGroup.getId()));
    if (elementInfo.isPresent()) {
      ZusammenElement zusammenElement = VlmZusammenUtil.getZusammenElement(elementInfo.get());
      zusammenElement.setAction(Action.UPDATE);
      zusammenElement.setRelations(elementInfo.get().getRelations().stream()
          .filter(
              relation -> !containedEntityId.equals(relation.getEdge2().getElementId().getValue()))
          .collect(Collectors.toList()));
      zusammenAdaptor.saveElement(context, elementContext, zusammenElement,
          String.format("remove %s", containedEntityType));
    }
  }

  @Override
  public void updateFeatureGroup(FeatureGroupEntity
                                     featureGroup, Set<String> addedEntitlementPools,
                                 Set<String> removedEntitlementPools,
                                 Set<String> addedLicenseKeyGroups,
                                 Set<String> removedLicenseKeyGroups) {
    ZusammenElement featureGroupElement = buildFeatureGroupElement(featureGroup, Action.UPDATE);
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(featureGroup.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    Optional<ElementInfo> elementInfo = zusammenAdaptor.getElementInfo(context,
        elementContext, new Id(featureGroup.getId()));
    if (elementInfo.isPresent()) {
      FeatureGroupEntity currentFeatureGroup =
          mapElementInfoToFeatureGroup(featureGroup.getId(), featureGroup.getVersion(),
              elementInfo.get());

      if (!(removedEntitlementPools == null)) {
        currentFeatureGroup.getEntitlementPoolIds().removeAll(removedEntitlementPools);
      }
      if (!(addedEntitlementPools == null)) {
        currentFeatureGroup.getEntitlementPoolIds().addAll(addedEntitlementPools);
      }
      if (featureGroupElement.getRelations() == null) {
        featureGroupElement.setRelations(new ArrayList<>());
      }
      featureGroupElement.getRelations()
          .addAll(currentFeatureGroup.getEntitlementPoolIds().stream()
              .map(relation -> VlmZusammenUtil
                  .createRelation(RelationType.FeatureGroupToEntitlmentPool, relation))
              .collect(Collectors.toList()));

      if (!(removedLicenseKeyGroups == null)) {
        currentFeatureGroup.getLicenseKeyGroupIds().removeAll(removedLicenseKeyGroups);
      }
      if (!(addedLicenseKeyGroups == null)) {
        currentFeatureGroup.getLicenseKeyGroupIds().addAll(addedLicenseKeyGroups);
      }
      currentFeatureGroup.getLicenseKeyGroupIds().addAll(addedLicenseKeyGroups);
      featureGroupElement.getRelations()
          .addAll(currentFeatureGroup.getLicenseKeyGroupIds().stream()
              .map(relation -> VlmZusammenUtil
                  .createRelation(RelationType.FeatureGroupToLicenseKeyGroup, relation))
              .collect(Collectors.toList()));

      zusammenAdaptor
          .saveElement(context, elementContext, featureGroupElement, "update feature group");
    }
  }

  @Override
  public void deleteAll(FeatureGroupEntity featureGroup) {
    //not supported
  }


  @Override
  public void addReferencingLicenseAgreement(FeatureGroupEntity featureGroup,
                                             String licenseAgreementId) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(featureGroup.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    Optional<ElementInfo> elementInfo =
        zusammenAdaptor.getElementInfo(context, elementContext, new Id(featureGroup.getId()));
    if (elementInfo.isPresent()) {
      ZusammenElement zusammenElement = VlmZusammenUtil.getZusammenElement(elementInfo.get());
      zusammenElement.setAction(Action.UPDATE);
      zusammenElement.getRelations().add(VlmZusammenUtil
          .createRelation(RelationType.FeatureGroupToReferencingLicenseAgreement,
              licenseAgreementId));
      zusammenAdaptor.saveElement(context, elementContext, zusammenElement,
          "add referencing license agreement");
    }
  }

  @Override
  public void removeReferencingLicenseAgreement(FeatureGroupEntity featureGroup,
                                                String licenseAgreementId) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(featureGroup.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    Optional<ElementInfo> elementInfo =
        zusammenAdaptor.getElementInfo(context, elementContext, new Id(featureGroup.getId()));
    if (elementInfo.isPresent()) {
      ZusammenElement zusammenElement = VlmZusammenUtil.getZusammenElement(elementInfo.get());
      zusammenElement.setAction(Action.UPDATE);
      zusammenElement.setRelations(elementInfo.get().getRelations().stream()
          .filter(
              relation -> !licenseAgreementId.equals(relation.getEdge2().getElementId().getValue()))
          .collect(Collectors.toList()));

      zusammenAdaptor.saveElement(context, elementContext, zusammenElement,
          "remove referencing license agreement");
    }
  }

  private ZusammenElement buildFeatureGroupElement(FeatureGroupEntity featureGroup, Action action) {
    ZusammenElement featureGroupElement = new ZusammenElement();
    featureGroupElement.setAction(action);
    if (featureGroup.getId() != null) {
      featureGroupElement.setElementId(new Id(featureGroup.getId()));
    }
    Info info = new Info();
    info.setName(featureGroup.getName());
    info.setDescription(featureGroup.getDescription());
    info.addProperty("partNumber", featureGroup.getPartNumber());
    featureGroupElement.setInfo(info);

    featureGroupElement.setRelations(new ArrayList<>());

    if (featureGroup.getEntitlementPoolIds() != null &&
        featureGroup.getEntitlementPoolIds().size() > 0) {
      featureGroupElement.getRelations().addAll(featureGroup.getEntitlementPoolIds().stream()
          .map(rel -> VlmZusammenUtil
              .createRelation(RelationType.FeatureGroupToEntitlmentPool, rel))
          .collect(Collectors.toList()));
    }

    if (featureGroup.getLicenseKeyGroupIds() != null &&
        featureGroup.getLicenseKeyGroupIds().size() > 0) {
      featureGroupElement.getRelations()
          .addAll(featureGroup.getLicenseKeyGroupIds().stream()
              .map(rel -> VlmZusammenUtil
                  .createRelation(RelationType.FeatureGroupToLicenseKeyGroup, rel))
              .collect(Collectors.toList()));
    }

    if (featureGroup.getReferencingLicenseAgreements() != null &&
        featureGroup.getReferencingLicenseAgreements().size() > 0) {
      featureGroupElement.getRelations()
          .addAll(featureGroup.getReferencingLicenseAgreements().stream()
              .map(rel -> VlmZusammenUtil
                  .createRelation(RelationType.FeatureGroupToReferencingLicenseAgreement,
                      rel))
              .collect(Collectors.toList()));
    }
    return featureGroupElement;
  }

  private FeatureGroupEntity mapElementInfoToFeatureGroup(String vlmId, Version version,
                                                          ElementInfo elementInfo) {
    FeatureGroupEntity featureGroup =
        new FeatureGroupEntity(vlmId, version, elementInfo.getId().getValue());
    featureGroup.setName(elementInfo.getInfo().getName());
    featureGroup.setDescription(elementInfo.getInfo().getDescription());
    featureGroup.setPartNumber(elementInfo.getInfo().getProperty("partNumber"));

    Set<String> entitlementPoolIds = new HashSet<>();
    Set<String> licenseAgreements = new HashSet<>();
    Set<String> licenseKeyGroupIds = new HashSet<>();

    if (elementInfo.getRelations() != null) {
      for (Relation relation : elementInfo.getRelations()) {
        if (RelationType.FeatureGroupToEntitlmentPool.name().equals(relation.getType())) {
          entitlementPoolIds.add(relation.getEdge2().getElementId().getValue());
        } else if (RelationType.FeatureGroupToLicenseKeyGroup.name().equals(relation.getType())) {
          licenseKeyGroupIds.add(relation.getEdge2().getElementId().getValue());
        } else if (RelationType.FeatureGroupToReferencingLicenseAgreement.name()
            .equals(relation.getType())) {
          licenseAgreements.add(relation.getEdge2().getElementId().getValue());
        }
      }
    }
    featureGroup.setEntitlementPoolIds(entitlementPoolIds);
    featureGroup.setLicenseKeyGroupIds(licenseKeyGroupIds);
    featureGroup.setReferencingLicenseAgreements(licenseAgreements);

    return featureGroup;
  }
}
