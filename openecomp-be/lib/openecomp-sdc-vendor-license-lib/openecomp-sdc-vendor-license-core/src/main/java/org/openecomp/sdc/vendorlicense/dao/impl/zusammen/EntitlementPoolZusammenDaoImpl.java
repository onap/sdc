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
import org.openecomp.sdc.vendorlicense.dao.EntitlementPoolDao;
import org.openecomp.sdc.vendorlicense.dao.types.AggregationFunction;
import org.openecomp.sdc.vendorlicense.dao.types.ChoiceOrOther;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementMetric;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementTime;
import org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther;
import org.openecomp.sdc.vendorlicense.dao.types.OperationalScope;
import org.openecomp.sdc.vendorlicense.dao.types.ThresholdUnit;
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
 * Created by ayalaben on 3/28/2017.
 */
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
        VlmZusammenUtil.buildStructuralElement(StructureElement.EntitlementPools, null);

    entitlementPoolsElement.addSubElement(entitlementPoolElement);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(entitlementPool.getVendorLicenseModelId());
    Optional<Element> savedElement = zusammenAdaptor.saveElement(context, new ElementContext(itemId,
            VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor)),
        entitlementPoolsElement, "Create entitlement pool");

    savedElement.ifPresent(element -> entitlementPool
        .setId(element.getSubElements().iterator().next().getElementId().getValue()));
  }

  @Override
  public void update(EntitlementPoolEntity entitlementPool) {
    ZusammenElement entitlmentpoolElement =
        buildEntitlementPoolElement(entitlementPool, Action.UPDATE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(entitlementPool.getVendorLicenseModelId());
    ElementContext elementContext =  new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

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

    zusammenAdaptor.saveElement(context,elementContext, entitlmentpoolElement,
        String.format("Update entitlement pool with id %s", entitlementPool.getId()));
  }

  @Override
  public EntitlementPoolEntity get(EntitlementPoolEntity entitlementPool) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(entitlementPool.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VlmZusammenUtil.getVersionTag(entitlementPool.getVersion()));

    return zusammenAdaptor.getElementInfo(context, elementContext, new Id(entitlementPool.getId()))
        .map(elementInfo -> mapElementInfoToEntitlementPool(
            entitlementPool.getVendorLicenseModelId(), entitlementPool.getVersion(), elementInfo))
        .orElse(null);
  }

  @Override
  public void delete(EntitlementPoolEntity entitlementPool) {
    SessionContext context = ZusammenUtil.createSessionContext();
    ZusammenElement zusammenElement = new ZusammenElement();
    zusammenElement.setAction(Action.DELETE);
    zusammenElement.setElementId(new Id(entitlementPool.getId()));

    Id itemId = new Id(entitlementPool.getVendorLicenseModelId());
    ElementContext elementContext =
        new ElementContext(itemId,
            VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));
    zusammenAdaptor.saveElement(context, elementContext, zusammenElement,
        "delete entitlement pool. id:" + entitlementPool.getId() + ".");
  }

  @Override
  public Collection<EntitlementPoolEntity> list(EntitlementPoolEntity entitlementPool) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(entitlementPool.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VlmZusammenUtil.getVersionTag(entitlementPool.getVersion()));

    return zusammenAdaptor
        .listElementsByName(context, elementContext, null, StructureElement.EntitlementPools.name())
        .stream().map(elementInfo -> mapElementInfoToEntitlementPool(
            entitlementPool.getVendorLicenseModelId(), entitlementPool.getVersion(), elementInfo))
        .collect(Collectors.toList());
  }

  @Override
  public long count(EntitlementPoolEntity entitlementPool) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(entitlementPool.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VlmZusammenUtil.getVersionTag(entitlementPool.getVersion()));

    return zusammenAdaptor
        .listElementsByName(context, elementContext, null, StructureElement.EntitlementPools.name())
        .size();
  }

  @Override
  public void removeReferencingFeatureGroup(EntitlementPoolEntity entitlementPool,
                                            String referencingFeatureGroupId) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(entitlementPool.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

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
    Id itemId = new Id(entitlementPool.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

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

  private ZusammenElement buildEntitlementPoolElement(EntitlementPoolEntity entitlementPool,
                                                      Action action) {

    ZusammenElement entitlementPoolElement = new ZusammenElement();
    entitlementPoolElement.setAction(action);
    if (entitlementPool.getId() != null) {
      entitlementPoolElement.setElementId(new Id(entitlementPool.getId()));
    }
    Info info = new Info();
    info.setName(entitlementPool.getName());
    info.setDescription(entitlementPool.getDescription());
    info.addProperty("thresholdValue", entitlementPool.getThresholdValue());
    info.addProperty("threshold_unit", entitlementPool.getThresholdUnit());
    info.addProperty("entitlement_metric", entitlementPool.getEntitlementMetric());
    info.addProperty("increments", entitlementPool.getIncrements());
    info.addProperty("aggregation_func", entitlementPool.getAggregationFunction());
    info.addProperty("operational_scope", entitlementPool.getOperationalScope());
    info.addProperty("EntitlementTime", entitlementPool.getTime());
    info.addProperty("manufacturerReferenceNumber",
        entitlementPool.getManufacturerReferenceNumber());
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

  private EntitlementPoolEntity mapElementInfoToEntitlementPool(String vlmId, Version version,
                                                                ElementInfo elementInfo) {
    EntitlementPoolEntity entitlmentPool =
        new EntitlementPoolEntity(vlmId, version, elementInfo.getId().getValue());
    entitlmentPool.setName(elementInfo.getInfo().getName());
    entitlmentPool.setDescription(elementInfo.getInfo().getDescription());
    entitlmentPool
        .setThresholdValue(elementInfo.getInfo().getProperty("thresholdValue") != null
                ? VlmZusammenUtil.toInteger(elementInfo.getInfo().getProperty("thresholdValue")) : null);

    Object threshold_unit = elementInfo.getInfo().getProperty("threshold_unit");
    entitlmentPool.setThresholdUnit( threshold_unit != null ?
        ThresholdUnit.valueOf(elementInfo.getInfo().getProperty("threshold_unit")) : null);
    entitlmentPool.setEntitlementMetric(
        getEntitlementMetricCoiceOrOther(elementInfo.getInfo().getProperty("entitlement_metric")));
    entitlmentPool.setIncrements(elementInfo.getInfo().getProperty("increments"));
    entitlmentPool.setAggregationFunction(
        getAggregationFuncCoiceOrOther(elementInfo.getInfo().getProperty("aggregation_func")));
    entitlmentPool.setOperationalScope(getOperationalScopeMultiChoiceOrOther(
        elementInfo.getInfo().getProperty("operational_scope")));
    entitlmentPool.setTime(
        getEntitlementTimeCoiceOrOther(elementInfo.getInfo().getProperty("EntitlementTime")));
    entitlmentPool.setManufacturerReferenceNumber(
        elementInfo.getInfo().getProperty("manufacturerReferenceNumber"));
    entitlmentPool.setStartDate(elementInfo.getInfo().getProperty("startDate"));
    entitlmentPool.setExpiryDate(elementInfo.getInfo().getProperty("expiryDate"));

    if (elementInfo.getRelations() != null && elementInfo.getRelations().size() > 0) {
      entitlmentPool
          .setReferencingFeatureGroups(elementInfo.getRelations().stream().map(relation -> relation
              .getEdge2().getElementId().getValue()).collect(Collectors.toSet()));
    }
    return entitlmentPool;
  }

  private ChoiceOrOther<AggregationFunction> getAggregationFuncCoiceOrOther(
      Map aggregationFunction) {
    return new ChoiceOrOther<>
        (AggregationFunction.valueOf((String) aggregationFunction.get("choice")),
            (String) aggregationFunction.get("other"));
  }

  private ChoiceOrOther<EntitlementMetric> getEntitlementMetricCoiceOrOther(Map entitlementMetric) {
    return new ChoiceOrOther<>(EntitlementMetric.valueOf((String) entitlementMetric.get("choice")
    ), (String) entitlementMetric.get("other"));
  }

  private ChoiceOrOther<EntitlementTime> getEntitlementTimeCoiceOrOther(Map entitlementTime) {
    return new ChoiceOrOther<>(EntitlementTime.valueOf((String) entitlementTime.get("choice")),
        (String) entitlementTime.get("other"));
  }

  private MultiChoiceOrOther<OperationalScope> getOperationalScopeMultiChoiceOrOther
      (Map<String, Object>
           operationalScope) {
    if(operationalScope != null && !operationalScope.isEmpty()) {
      Set<OperationalScope> choices = new HashSet<>();
      ((List<String>) operationalScope.get("choices")).
          forEach(choice -> choices.add(OperationalScope.valueOf(choice)));

      return new MultiChoiceOrOther<>(choices, operationalScope.get("other")==null?null:
          (String) operationalScope.get("other"));
    }
    return null;
  }

  @Override
  public String getManufacturerReferenceNumber(EntitlementPoolEntity entitlementPoolEntity){
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(entitlementPoolEntity.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
            VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
            VlmZusammenUtil.getVersionTag(entitlementPoolEntity.getVersion()));
    Optional<ElementInfo> elementInfo1 = zusammenAdaptor.getElementInfo(context, elementContext, new Id(entitlementPoolEntity.getId()));
    Map<String, Object> properties = elementInfo1.get().getInfo().getProperties();
    String manufacturerReferenceNumber = null;
    if(properties != null && properties.containsKey("manufacturerReferenceNumber") ) {
      manufacturerReferenceNumber = (String)properties.get("manufacturerReferenceNumber");
    }
    return manufacturerReferenceNumber;
  }

}
