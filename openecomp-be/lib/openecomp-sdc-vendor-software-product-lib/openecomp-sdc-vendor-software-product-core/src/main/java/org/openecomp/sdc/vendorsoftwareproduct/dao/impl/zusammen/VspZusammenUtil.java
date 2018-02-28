package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

class VspZusammenUtil {

  private VspZusammenUtil(){}

  static ZusammenElement aggregateElements(ZusammenElement... elements) {
    ZusammenElement head = null;
    ZusammenElement father = null;
    for (ZusammenElement element : elements) {
      if (Objects.isNull(head)) {
        head = father = element;
      } else {
        if (father != null) {
          father.getSubElements().add(element);
          father = element;
        }
      }
    }

    return head;
  }

  static boolean hasEmptyData(InputStream elementData) {
    String EMPTY_DATA = "{}";
    byte[] byteElementData;
    try {
      byteElementData = IOUtils.toByteArray(elementData);
    } catch (IOException ex) {
      return false;
    }
    if (Arrays.equals(EMPTY_DATA.getBytes(), byteElementData)) {
      return true;
    }
    return false;
  }
}
