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
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorlicense.dao.FeatureGroupDao;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.convertor.ElementToFeatureGroupConvertor;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.types.ElementPropertyName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

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
    ZusammenElement featureGroupElement = buildFeatureGroupElement(featureGroup, Action.CREATE);

    ZusammenElement featureGroupsElement =
        buildStructuralElement(ElementType.FeatureGroups, Action.IGNORE);

    featureGroupsElement.addSubElement(featureGroupElement);

    SessionContext context = createSessionContext();
    Element featureGroupsSavedElement = zusammenAdaptor.saveElement(context,
        new ElementContext(featureGroup.getVendorLicenseModelId(),
            featureGroup.getVersion().getId()), featureGroupsElement, "Create feature group");

    featureGroup.setId(
        featureGroupsSavedElement.getSubElements().iterator().next().getElementId().getValue());
  }

  @Override
  public void update(FeatureGroupEntity featureGroup) {
    ZusammenElement featureGroupElement = buildFeatureGroupElement(featureGroup, Action.UPDATE);

    SessionContext context = createSessionContext();
    zusammenAdaptor.saveElement(context, new ElementContext(featureGroup.getVendorLicenseModelId(),
            featureGroup.getVersion().getId()), featureGroupElement,
        String.format("Update feature group with id %s", featureGroup.getId()));
  }

  @Override
  public FeatureGroupEntity get(FeatureGroupEntity featureGroup) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(featureGroup.getVendorLicenseModelId(),
        featureGroup.getVersion().getId());

    return zusammenAdaptor.getElementInfo(context, elementContext, new Id(featureGroup.getId()))
        .map(elementInfo -> {
          FeatureGroupEntity entity = new ElementToFeatureGroupConvertor().convert(elementInfo);
          entity.setVendorLicenseModelId(featureGroup.getVendorLicenseModelId());
          entity.setVersion(featureGroup.getVersion());
          return entity;
        })
        .orElse(null);
  }

  @Override
  public void delete(FeatureGroupEntity featureGroup) {
    ZusammenElement zusammenElement = buildElement(new Id(featureGroup.getId()), Action.DELETE);

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(featureGroup.getVendorLicenseModelId(),
        featureGroup.getVersion().getId());
    zusammenAdaptor.saveElement(context, elementContext, zusammenElement,
        "delete feature group. id:" + featureGroup.getId() + ".");
  }

  @Override
  public Collection<FeatureGroupEntity> list(FeatureGroupEntity featureGroup) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(featureGroup.getVendorLicenseModelId(),
        featureGroup.getVersion().getId());

    ElementToFeatureGroupConvertor convertor = new ElementToFeatureGroupConvertor();
    return zusammenAdaptor
        .listElementsByName(context, elementContext, null, ElementType.FeatureGroups.name())
        .stream().map(elementInfo -> {
          FeatureGroupEntity entity = convertor.convert(
              elementInfo);
          entity.setVendorLicenseModelId(featureGroup.getVendorLicenseModelId());
          entity.setVersion(featureGroup.getVersion());
          return entity;
        })
        .collect(Collectors.toList());
  }

  @Override
  public long count(FeatureGroupEntity featureGroup) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(featureGroup.getVendorLicenseModelId(),
        featureGroup.getVersion().getId());

    return zusammenAdaptor
        .listElementsByName(context, elementContext, null, ElementType.FeatureGroups.name())
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
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(featureGroup.getVendorLicenseModelId(),
        featureGroup.getVersion().getId());

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
  public void updateFeatureGroup(FeatureGroupEntity featureGroup,
                                 Set<String> addedEntitlementPools,
                                 Set<String> removedEntitlementPools,
                                 Set<String> addedLicenseKeyGroups,
                                 Set<String> removedLicenseKeyGroups) {
    ZusammenElement featureGroupElement = buildFeatureGroupElement(featureGroup, Action.UPDATE);
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(featureGroup.getVendorLicenseModelId(),
        featureGroup.getVersion().getId());
    ElementToFeatureGroupConvertor convertor = new ElementToFeatureGroupConvertor();
    Optional<ElementInfo> elementInfo = zusammenAdaptor.getElementInfo(context,
        elementContext, new Id(featureGroup.getId()));
    if (elementInfo.isPresent()) {
      FeatureGroupEntity currentFeatureGroup = convertor.convert(elementInfo.get());
      currentFeatureGroup.setVendorLicenseModelId(featureGroup.getVendorLicenseModelId());
      currentFeatureGroup.setVersion(featureGroup.getVersion());
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

      featureGroupElement.getRelations()
          .addAll(currentFeatureGroup.getLicenseKeyGroupIds().stream()
              .map(relation -> VlmZusammenUtil
                  .createRelation(RelationType.FeatureGroupToLicenseKeyGroup, relation))
              .collect(Collectors.toList()));

      Collection<Relation> LaRelations = elementInfo.get().getRelations().stream().filter
          (rel -> rel.getType()
              .equals(RelationType.FeatureGroupToReferencingLicenseAgreement.name()))
          .map(rel -> VlmZusammenUtil.createRelation(RelationType
              .FeatureGroupToReferencingLicenseAgreement, rel.getEdge2().getElementId().toString()))
          .collect(Collectors.toList());

      featureGroupElement.getRelations().addAll(LaRelations);

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
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(featureGroup.getVendorLicenseModelId(),
        featureGroup.getVersion().getId());

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
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(featureGroup.getVendorLicenseModelId(),
        featureGroup.getVersion().getId());

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
    ZusammenElement featureGroupElement =
        buildElement(featureGroup.getId() == null ? null : new Id(featureGroup.getId()), action);
    Info info = new Info();
    info.setName(featureGroup.getName());
    info.setDescription(featureGroup.getDescription());
    info.addProperty(ElementPropertyName.elementType.name(), ElementType.FeatureGroup);
    info.addProperty("partNumber", featureGroup.getPartNumber());
    info.addProperty("manufacturerReferenceNumber", featureGroup.getManufacturerReferenceNumber());
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
}
