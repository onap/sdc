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
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorlicense.dao.EntitlementPoolDao;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.convertor.ElementToEntitlementPoolConvertor;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.types.ElementPropertyName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildElement;

public class EntitlementPoolZusammenDaoImpl implements EntitlementPoolDao {

  private ZusammenAdaptor zusammenAdaptor;

  public EntitlementPoolZusammenDaoImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {
    //no need
  }

  @Override
  public void create(EntitlementPoolEntity entitlementPool) {
    ZusammenElement entitlementPoolElement =
        buildEntitlementPoolElement(entitlementPool, Action.CREATE);

    ZusammenElement entitlementPoolsElement =
        ZusammenUtil.buildStructuralElement(ElementType.EntitlementPools, Action.IGNORE);

    ZusammenElement limitsElement =
        ZusammenUtil.buildStructuralElement(ElementType.Limits, Action.CREATE);

    entitlementPoolElement.addSubElement(limitsElement);
    entitlementPoolsElement.addSubElement(entitlementPoolElement);

    SessionContext context = ZusammenUtil.createSessionContext();
    Element epsSavedElement = zusammenAdaptor.saveElement(context,
        new ElementContext(entitlementPool.getVendorLicenseModelId(),
            entitlementPool.getVersion().getId()),
        entitlementPoolsElement, "Create entitlement pool");

    entitlementPool
        .setId(epsSavedElement.getSubElements().iterator().next().getElementId().getValue());
  }

  @Override
  public void update(EntitlementPoolEntity entitlementPool) {
    ZusammenElement entitlmentpoolElement =
        buildEntitlementPoolElement(entitlementPool, Action.UPDATE);

    SessionContext context = ZusammenUtil.createSessionContext();
    ElementContext elementContext = new ElementContext(entitlementPool.getVendorLicenseModelId(),
        entitlementPool.getVersion().getId());

    Optional<ElementInfo> epFromDb = zusammenAdaptor.getElementInfo(context, elementContext,
        new Id(entitlementPool.getId()));

    if (epFromDb.isPresent()) {
      if (entitlmentpoolElement.getRelations() == null) {
        entitlmentpoolElement.setRelations(new ArrayList<>());
      }
      if (epFromDb.get().getRelations() != null && epFromDb.get().getRelations().size() > 0) {
        entitlmentpoolElement.getRelations().addAll(epFromDb.get().getRelations());
      }
    }

    zusammenAdaptor.saveElement(context, elementContext, entitlmentpoolElement,
        String.format("Update entitlement pool with id %s", entitlementPool.getId()));
  }

  @Override
  public EntitlementPoolEntity get(EntitlementPoolEntity entitlementPool) {
    SessionContext context = ZusammenUtil.createSessionContext();
    ElementContext elementContext = new ElementContext(entitlementPool.getVendorLicenseModelId(),
        entitlementPool.getVersion().getId());
    ElementToEntitlementPoolConvertor convertor = new ElementToEntitlementPoolConvertor();
    return zusammenAdaptor.getElementInfo(context, elementContext, new Id(entitlementPool.getId()))
        .map(elementInfo -> {
          EntitlementPoolEntity entity = convertor.convert(elementInfo);
          entity.setVendorLicenseModelId(entitlementPool.getVendorLicenseModelId());
          entity.setVersion(entitlementPool.getVersion());
          return entity;
        })
        .orElse(null);
  }

  @Override
  public void delete(EntitlementPoolEntity entitlementPool) {
    ZusammenElement zusammenElement = buildElement(new Id(entitlementPool.getId()), Action.DELETE);

    SessionContext context = ZusammenUtil.createSessionContext();
    ElementContext elementContext = new ElementContext(entitlementPool.getVendorLicenseModelId(),
        entitlementPool.getVersion().getId());
    zusammenAdaptor.saveElement(context, elementContext, zusammenElement,
        "delete entitlement pool. id:" + entitlementPool.getId() + ".");
  }

  @Override
  public Collection<EntitlementPoolEntity> list(EntitlementPoolEntity entitlementPool) {
    SessionContext context = ZusammenUtil.createSessionContext();
    ElementContext elementContext = new ElementContext(entitlementPool.getVendorLicenseModelId(),
        entitlementPool.getVersion().getId());
    ElementToEntitlementPoolConvertor convertor = new ElementToEntitlementPoolConvertor();
    return zusammenAdaptor
        .listElementsByName(context, elementContext, null, ElementType.EntitlementPools.name())
        .stream().map(elementInfo -> {
          EntitlementPoolEntity entity = convertor.convert(elementInfo);
          entity.setVendorLicenseModelId(entitlementPool.getVendorLicenseModelId());
          entity.setVersion(entitlementPool.getVersion());
          return entity;
        }).collect(Collectors.toList());
  }

  @Override
  public long count(EntitlementPoolEntity entitlementPool) {
    SessionContext context = ZusammenUtil.createSessionContext();
    ElementContext elementContext = new ElementContext(entitlementPool.getVendorLicenseModelId(),
        entitlementPool.getVersion().getId());

    return zusammenAdaptor
        .listElementsByName(context, elementContext, null, ElementType.EntitlementPools.name())
        .size();
  }

  @Override
  public void removeReferencingFeatureGroup(EntitlementPoolEntity entitlementPool,
                                            String referencingFeatureGroupId) {
    SessionContext context = ZusammenUtil.createSessionContext();
    ElementContext elementContext = new ElementContext(entitlementPool.getVendorLicenseModelId(),
        entitlementPool.getVersion().getId());

    Optional<ElementInfo> elementInfo =
        zusammenAdaptor.getElementInfo(context, elementContext, new Id(entitlementPool.getId()));

    if (elementInfo.isPresent()) {
      ZusammenElement zusammenElement = VlmZusammenUtil.getZusammenElement(elementInfo.get());
      zusammenElement.setAction(Action.UPDATE);
      zusammenElement.setRelations(elementInfo.get().getRelations().stream()
          .filter(relation -> !referencingFeatureGroupId
              .equals(relation.getEdge2().getElementId().getValue()))
          .collect(Collectors.toList()));

      zusammenAdaptor.saveElement(context, elementContext, zusammenElement,
          "remove referencing feature group");
    }
  }

  @Override
  public void addReferencingFeatureGroup(EntitlementPoolEntity entitlementPool,
                                         String referencingFeatureGroupId) {
    SessionContext context = ZusammenUtil.createSessionContext();
    ElementContext elementContext = new ElementContext(entitlementPool.getVendorLicenseModelId(),
        entitlementPool.getVersion().getId());

    Optional<ElementInfo> elementInfo =
        zusammenAdaptor.getElementInfo(context, elementContext, new Id(entitlementPool.getId()));

    if (elementInfo.isPresent()) {
      ZusammenElement zusammenElement = VlmZusammenUtil.getZusammenElement(elementInfo.get());
      zusammenElement.setAction(Action.UPDATE);
      if (zusammenElement.getRelations() == null) {
        zusammenElement.setRelations(new ArrayList<>());
      }
      zusammenElement.getRelations().add(VlmZusammenUtil
          .createRelation(RelationType.EntitlmentPoolToReferencingFeatureGroup,
              referencingFeatureGroupId));
      zusammenAdaptor
          .saveElement(context, elementContext, zusammenElement, "add referencing feature group");
    }
  }

  @Override
  public void deleteAll(EntitlementPoolEntity entitlementPool) {
    //not supported
  }

  @Override
  public String getManufacturerReferenceNumber(EntitlementPoolEntity entitlementPoolEntity) {
    SessionContext context = ZusammenUtil.createSessionContext();
    ElementContext elementContext =
        new ElementContext(entitlementPoolEntity.getVendorLicenseModelId(),
            entitlementPoolEntity.getVersion().getId());

    Optional<ElementInfo> elementInfo1 = zusammenAdaptor
        .getElementInfo(context, elementContext, new Id(entitlementPoolEntity.getId()));
    Map<String, Object> properties = elementInfo1.get().getInfo().getProperties();
    String manufacturerReferenceNumber = null;
    if (properties != null && properties.containsKey("manufacturerReferenceNumber")) {
      manufacturerReferenceNumber = (String) properties.get("manufacturerReferenceNumber");
    }
    return manufacturerReferenceNumber;
  }

  private ZusammenElement buildEntitlementPoolElement(EntitlementPoolEntity entitlementPool,
                                                      Action action) {
    ZusammenElement entitlementPoolElement =
        buildElement(entitlementPool.getId() == null ? null : new Id(entitlementPool.getId()),
            action);
    Info info = new Info();
    info.setName(entitlementPool.getName());
    info.setDescription(entitlementPool.getDescription());
    info.addProperty(ElementPropertyName.elementType.name(), ElementType.EntitlementPool);
    info.addProperty("version_uuid", entitlementPool.getVersionUuId());
    info.addProperty("thresholdValue", entitlementPool.getThresholdValue());
    info.addProperty("threshold_unit", entitlementPool.getThresholdUnit());
    info.addProperty("increments", entitlementPool.getIncrements());
    info.addProperty("operational_scope", entitlementPool.getOperationalScope());
    info.addProperty("startDate", entitlementPool.getStartDate());
    info.addProperty("expiryDate", entitlementPool.getExpiryDate());
    entitlementPoolElement.setInfo(info);

    if (entitlementPool.getReferencingFeatureGroups() != null
        && entitlementPool.getReferencingFeatureGroups().size() > 0) {
      entitlementPoolElement.setRelations(entitlementPool.getReferencingFeatureGroups().stream()
          .map(rel -> VlmZusammenUtil
              .createRelation(RelationType.EntitlmentPoolToReferencingFeatureGroup, rel))
          .collect(Collectors.toList()));
    }
    return entitlementPoolElement;
  }


}
