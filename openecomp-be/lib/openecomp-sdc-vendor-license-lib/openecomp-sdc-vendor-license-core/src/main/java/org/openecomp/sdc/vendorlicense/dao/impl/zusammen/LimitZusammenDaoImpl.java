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
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorlicense.dao.LimitDao;
import org.openecomp.sdc.vendorlicense.dao.types.AggregationFunction;
import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LimitType;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.types.ElementPropertyName;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

public class LimitZusammenDaoImpl implements LimitDao {

  private static final String LIMT_TYPE = "type";
  private static final String METRIC = "metric";
  private static final String AGGREGATIONFUNCTION = "aggregationfunction";
  private static final String TIME = "time";
  private static final String UNIT = "unit";
  private static final String VALUE = "value";
  private ZusammenAdaptor zusammenAdaptor;

  public LimitZusammenDaoImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void create(LimitEntity limitEntity) {
    ZusammenElement limitElement = limitToZusammen(limitEntity, Action.CREATE);

    ZusammenElement limitsElement = buildStructuralElement(ElementType.Limits, null);
    limitsElement.setSubElements(Collections.singletonList(limitElement));

    ZusammenElement epLkgElement = buildElement(new Id(limitEntity.getEpLkgId()), Action.IGNORE);
    epLkgElement.setSubElements(Collections.singletonList(limitsElement));

    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(limitEntity.getVendorLicenseModelId(), limitEntity.getVersion().getId());

    Element savedElement =
        zusammenAdaptor.saveElement(context, elementContext, epLkgElement, "Create limit");
    limitEntity.setId(savedElement.getSubElements().iterator().next()
        .getSubElements().iterator().next().getElementId().getValue());
  }

  @Override
  public boolean isLimitPresent(LimitEntity limitEntity) {
    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(limitEntity.getVendorLicenseModelId(), limitEntity.getVersion().getId());

    Collection<ElementInfo> elementInfos = zusammenAdaptor.listElementsByName(context,
        elementContext, new Id(limitEntity.getEpLkgId()), ElementType.Limits.name());

    for (ElementInfo elementInfo : elementInfos) {
      if (elementInfo.getId().getValue().equals(limitEntity.getId())) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Collection<LimitEntity> list(LimitEntity limitEntity) {
    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(limitEntity.getVendorLicenseModelId(), limitEntity.getVersion().getId());

    return listLimits(context, elementContext, limitEntity);
  }

  private Collection<LimitEntity> listLimits(SessionContext context, ElementContext elementContext,
                                             LimitEntity limitEntity) {
    return zusammenAdaptor
        .listElementsByName(context, elementContext, new Id(limitEntity.getEpLkgId()),
            ElementType.Limits.name())
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
    limitEntity.setType(elementInfo.getInfo().getProperties().get(LIMT_TYPE) != null ?
        LimitType.valueOf((String) elementInfo.getInfo().getProperties().get(LIMT_TYPE)) :
        null);
    limitEntity.setTime((String) elementInfo.getInfo().getProperties().get(TIME));
    limitEntity.setMetric((String) elementInfo.getInfo().getProperties().get(METRIC));
    limitEntity.setAggregationFunction(elementInfo.getInfo().getProperties().get
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
  public void update(LimitEntity limitEntity) {
    ZusammenElement limitElement = limitToZusammen(limitEntity, Action.UPDATE);

    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(limitEntity.getVendorLicenseModelId(), limitEntity.getVersion().getId());

    zusammenAdaptor.saveElement(context, elementContext, limitElement,
        String.format("Update limit with id %s", limitEntity.getId()));
  }

  @Override
  public LimitEntity get(LimitEntity limitEntity) {
    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(limitEntity.getVendorLicenseModelId(), limitEntity.getVersion().getId());

    return zusammenAdaptor.getElementInfo(context, elementContext, new Id(limitEntity.getId()))
        .map(elementInfo -> mapElementInfoToLimit(
            limitEntity.getVendorLicenseModelId(), limitEntity.getVersion(), limitEntity
                .getEpLkgId(), elementInfo))
        .orElse(null);
  }

  @Override
  public void delete(LimitEntity limitEntity) {
    ZusammenElement zusammenElement = buildElement(new Id(limitEntity.getId()), Action.DELETE);

    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(limitEntity.getVendorLicenseModelId(), limitEntity.getVersion().getId());
    zusammenAdaptor.saveElement(context, elementContext, zusammenElement,
        "delete limit Id:" + limitEntity.getId() + ".");
  }

  @Override
  public void registerVersioning(String versionableEntityType) {

  }

  private ZusammenElement limitToZusammen(LimitEntity limit, Action action) {
    ZusammenElement limitElement =
        buildElement(limit.getId() == null ? null : new Id(limit.getId()), action);
    Info info = new Info();
    info.setName(limit.getName());
    info.setDescription(limit.getDescription());
    info.addProperty(ElementPropertyName.elementType.name(), ElementType.Limit);
    info.addProperty(LIMT_TYPE, limit.getType());
    info.addProperty(METRIC, limit.getMetric());
    info.addProperty(AGGREGATIONFUNCTION, limit.getAggregationFunction());
    info.addProperty(TIME, limit.getTime());
    info.addProperty(VALUE, limit.getValue());
    info.addProperty(UNIT, limit.getUnit());
    limitElement.setInfo(info);
    return limitElement;
  }
}
