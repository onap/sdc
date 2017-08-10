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
import org.openecomp.sdc.vendorlicense.dao.LimitDao;
import org.openecomp.sdc.vendorlicense.dao.types.AggregationFunction;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementMetric;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementTime;
import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LimitType;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public class LimitZusammenDaoImpl implements LimitDao {

  public static final String LIMT_TYPE = "type";
  public static final String METRIC = "metric";
  public static final String AGGREGATIONFUNCTION = "aggregationfunction";
  public static final String TIME = "time";
  public static final String UNIT = "unit";
  public static final String VALUE = "value";
  private ZusammenAdaptor zusammenAdaptor;

  public LimitZusammenDaoImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void create(LimitEntity limitEntity) {
    ZusammenElement limitElement = limitToZusammen(limitEntity, Action.CREATE);

    ZusammenElement limitsElement =
        VlmZusammenUtil.buildStructuralElement(StructureElement.Limits, null);
    limitsElement.setSubElements(Collections.singletonList(limitElement));

    ZusammenElement epLkgElement =
        buildZusammenElement(new Id(limitEntity.getEpLkgId()), Action.IGNORE);
    epLkgElement.setSubElements(Collections.singletonList(limitsElement));

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(limitEntity.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    Optional<Element> savedElement =
        zusammenAdaptor.saveElement(context, elementContext, epLkgElement, "Create limit");
    savedElement.ifPresent(element ->
        limitEntity.setId(element.getSubElements().iterator().next()
            .getSubElements().iterator().next().getElementId().getValue()));
  }

  @Override
  public boolean isLimitPresent(LimitEntity limitEntity) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(limitEntity.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VlmZusammenUtil.getVersionTag(limitEntity.getVersion()));

    Collection<ElementInfo> elementInfos = zusammenAdaptor.listElementsByName(context,
        elementContext, new Id(limitEntity.getEpLkgId()),StructureElement.Limits.name());

    for (ElementInfo elementInfo : elementInfos) {
      if (elementInfo.getId().getValue().equals(limitEntity.getId())) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Collection<LimitEntity> list(LimitEntity limitEntity) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(limitEntity.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VlmZusammenUtil.getVersionTag(limitEntity.getVersion()));

    return listLimits(context, elementContext, limitEntity);
  }

  private Collection<LimitEntity> listLimits(SessionContext context, ElementContext elementContext,
                                           LimitEntity limitEntity) {
    return zusammenAdaptor
        .listElementsByName(context, elementContext, new Id(limitEntity.getEpLkgId()),
            StructureElement.Limits.name())
        .stream().map(elementInfo -> mapElementInfoToLimit(
            limitEntity.getVendorLicenseModelId(), limitEntity.getVersion(),
            limitEntity.getEpLkgId(), elementInfo))
        .collect(Collectors.toList());
  }

  private LimitEntity mapElementInfoToLimit(String vlmId, Version version,
                                        String epLkgId, ElementInfo elementInfo) {
    LimitEntity limitEntity =
        new LimitEntity(vlmId, version, epLkgId, elementInfo.getId().getValue());

    limitEntity.setName(elementInfo.getInfo().getName());
    limitEntity.setDescription(elementInfo.getInfo().getDescription());
    limitEntity.setType( elementInfo.getInfo().getProperties().get(LIMT_TYPE) != null ?
        LimitType.valueOf((String) elementInfo.getInfo().getProperties().get(LIMT_TYPE)) :
        null);
    limitEntity.setTime((String) elementInfo.getInfo().getProperties().get(TIME) );
    limitEntity.setMetric( (String) elementInfo.getInfo().getProperties().get(METRIC));
    limitEntity.setAggregationFunction( elementInfo.getInfo().getProperties().get
        (AGGREGATIONFUNCTION) != null ?
        AggregationFunction.valueOf((String) elementInfo.getInfo().getProperties()
            .get(AGGREGATIONFUNCTION)) : null);
    Object unit = elementInfo.getInfo().getProperties().get(UNIT);
    limitEntity.setUnit((String) unit);
    Object value = elementInfo.getInfo().getProperties().get(VALUE);
    limitEntity.setValue((String) value);

    return limitEntity;
  }

  @Override
  public void update(LimitEntity entity) {
    ZusammenElement limitElement = limitToZusammen(entity, Action.UPDATE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(entity.getVendorLicenseModelId());
    ElementContext elementContext =  new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    zusammenAdaptor.saveElement(context,elementContext, limitElement,
        String.format("Update limit with id %s", entity.getId()));
  }

  @Override
  public LimitEntity get(LimitEntity limitEntity) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(limitEntity.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VlmZusammenUtil.getVersionTag(limitEntity.getVersion()));

    return zusammenAdaptor.getElementInfo(context, elementContext, new Id(limitEntity.getId()))
        .map(elementInfo -> mapElementInfoToLimit(
            limitEntity.getVendorLicenseModelId(), limitEntity.getVersion(), limitEntity
                .getEpLkgId(), elementInfo))
        .orElse(null);
  }

  @Override
  public void delete(LimitEntity entity) {
    SessionContext context = ZusammenUtil.createSessionContext();
    ZusammenElement zusammenElement = new ZusammenElement();
    zusammenElement.setAction(Action.DELETE);
    zusammenElement.setElementId(new Id(entity.getId()));

    Id itemId = new Id(entity.getVendorLicenseModelId());
    ElementContext elementContext =
            new ElementContext(itemId,
                    VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));
    zusammenAdaptor.saveElement(context, elementContext, zusammenElement,
            "delete limit Id:" + entity.getId() + ".");
  }

  @Override
  public void registerVersioning(String versionableEntityType) {

  }

  private ZusammenElement limitToZusammen(LimitEntity limit,
                                                     Action action) {
    ZusammenElement limitElement = buildLimitElement(limit, action);
    return limitElement;
  }

  private ZusammenElement buildLimitElement(LimitEntity limit,
                                                       Action action) {
    ZusammenElement limitElement = new ZusammenElement();
    limitElement.setAction(action);
    if (limit.getId() != null) {
      limitElement.setElementId(new Id(limit.getId()));
    }

    Info info = new Info();
    info.setName(limit.getName());
    info.setDescription(limit.getDescription());
    info.addProperty(LIMT_TYPE, limit.getType());
    info.addProperty(METRIC, limit.getMetric());
    info.addProperty(AGGREGATIONFUNCTION, limit.getAggregationFunction());
    info.addProperty(TIME, limit.getTime());
    info.addProperty(VALUE, limit.getValue());
    info.addProperty(UNIT, limit.getUnit());
    limitElement.setInfo(info);
    return limitElement;
  }

  private ZusammenElement buildZusammenElement(Id elementId, Action action) {
    ZusammenElement element = new ZusammenElement();
    element.setElementId(elementId);
    element.setAction(action);
    return element;
  }
}
