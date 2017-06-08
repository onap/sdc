package org.openecomp.sdc.vendorlicense.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.datatypes.item.Relation;
import com.amdocs.zusammen.datatypes.item.RelationEdge;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorlicense.errors.VendorLicenseModelNotFoundErrorBuilder;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;

import java.util.Optional;
import java.util.stream.Collectors;

public class VlmZusammenUtil {

  static ItemVersion getFirstVersion(SessionContext context, Id itemId,
                                     ZusammenAdaptor zusammenAdaptor) {
    Optional<ItemVersion> itemVersion = zusammenAdaptor.getFirstVersion(context, itemId);

    if (!itemVersion.isPresent()) {
      throw new CoreException(
          new VendorLicenseModelNotFoundErrorBuilder(itemId.getValue()).build());
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

  static ZusammenElement getZusammenElement(ElementInfo elementInfo) {
    ZusammenElement zusammenElement = new ZusammenElement();
    zusammenElement.setElementId(elementInfo.getId());
    zusammenElement.setInfo(elementInfo.getInfo());
    zusammenElement.setRelations(elementInfo.getRelations());
    zusammenElement.setSubElements(elementInfo.getSubElements().stream()
        .map(VlmZusammenUtil::getZusammenElement)
        .collect(Collectors.toList()));
    return zusammenElement;
  }

  public static Relation createRelation(RelationType type, String to) {
    Relation relation = new Relation();
    relation.setType(type.name());
    RelationEdge edge2 = new RelationEdge();
    edge2.setElementId(new Id(to));
    relation.setEdge2(edge2);
    return relation;
  }
}
