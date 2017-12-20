package org.openecomp.sdc.vendorlicense.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.sdc.vendorlicense.dao.types.LimitEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LimitType;

/**
 * Created by ayalaben on 9/26/2017
 */
public class ElementToLimitConvertor extends ElementConvertor {
  @Override
  public Object convert(Element element) {
    if (element == null) {
      return null;
    }
    return mapElementToLimitEntity(element);
  }

  private LimitEntity mapElementToLimitEntity(Element element) {
    LimitEntity limit = new LimitEntity();
    limit.setId(element.getElementId().getValue());
    limit.setName(element.getInfo().getName());
    limit.setDescription(element.getInfo().getDescription());
    limit.setUnit(element.getInfo().getProperty("unit"));
    limit.setMetric(element.getInfo().getProperty("metric"));
    limit.setValue(element.getInfo().getProperty("value"));
    limit.setTime(element.getInfo().getProperty("time"));
    limit.setAggregationFunction(element.getInfo().getProperty("aggregationFunction"));
    setLimitType(limit,element.getInfo().getProperty("type"));

    return limit;
  }

  private void setLimitType(LimitEntity limit, String type) {
    switch (LimitType.valueOf(type)) {
      case ServiceProvider:
        limit.setType(LimitType.ServiceProvider);
        break;
      case Vendor:
        limit.setType(LimitType.Vendor);
    }
  }
}
