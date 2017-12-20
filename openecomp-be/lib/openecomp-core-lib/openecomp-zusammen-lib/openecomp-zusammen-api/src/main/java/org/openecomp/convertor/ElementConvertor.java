package org.openecomp.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.item.Item;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.types.ElementPropertyName;

public abstract class ElementConvertor<T> {

  public static ElementType getElementType(Element element) {
    return ElementType
        .valueOf(element.getInfo().getProperty(ElementPropertyName.elementType.name()));
  }

  public static String getElementName(Element element) {
    return element.getInfo().getName();
  }


  abstract public T convert(Element element);

  public T convert( ElementInfo elementInfo) {
    throw new UnsupportedOperationException("convert elementInfo item is not supported ");
  }


  public T convert( Item item) {
    throw new UnsupportedOperationException("convert from item is not supported ");
  }


  public T convert( ItemVersion itemVersion) {
    throw new UnsupportedOperationException("convert from itemVersion is not supported ");
  }
}
