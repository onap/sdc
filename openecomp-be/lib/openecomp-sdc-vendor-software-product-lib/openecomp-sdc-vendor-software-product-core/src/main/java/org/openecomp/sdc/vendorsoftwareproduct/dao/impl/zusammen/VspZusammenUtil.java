package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.dao.errors.VendorSoftwareProductNotFoundErrorBuilder;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;

import java.util.Objects;
import java.util.Optional;

class VspZusammenUtil {

  static ItemVersion getFirstVersion(SessionContext context, Id itemId, ZusammenAdaptor
      zusammenAdaptor) {

    Optional<ItemVersion> itemVersion = zusammenAdaptor.getFirstVersion(context, itemId);

    if (!itemVersion.isPresent()) {
      throw new CoreException(
          new VendorSoftwareProductNotFoundErrorBuilder(itemId.getValue()).build());
    }
    return itemVersion.get();
  }

  static Id getFirstVersionId(SessionContext context, Id itemId, ZusammenAdaptor zusammenAdaptor) {
    return getFirstVersion(context, itemId, zusammenAdaptor).getId();
  }

  // TODO: 4/25/2017 remove upon working with more than one single version
  static String getVersionTag(Version version) {
    return version.getStatus() == VersionStatus.Locked
        ? null
        : version.toString();
  }

  static ZusammenElement buildStructuralElement(StructureElement structureElement, Action action) {
    return ZusammenUtil.buildStructuralElement(structureElement.name(), action);
  }

  static ZusammenElement aggregateElements(ZusammenElement... elements) {
    ZusammenElement head = null;
    ZusammenElement father = null;
    for (ZusammenElement element : elements) {
      if (Objects.isNull(head)) {
        head = father = element;
      } else {
        father.getSubElements().add(element);
        father = element;
      }
    }

    return head;
  }
}
