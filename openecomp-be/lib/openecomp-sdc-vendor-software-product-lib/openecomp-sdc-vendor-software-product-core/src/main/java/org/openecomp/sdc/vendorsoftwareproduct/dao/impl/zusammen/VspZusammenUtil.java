package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;

import java.util.Objects;

class VspZusammenUtil {

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
}
