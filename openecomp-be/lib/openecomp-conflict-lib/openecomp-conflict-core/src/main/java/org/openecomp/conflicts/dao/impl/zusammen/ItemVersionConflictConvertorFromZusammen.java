package org.openecomp.conflicts.dao.impl.zusammen;


import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementConflictInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ItemVersionConflict;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.datatypes.item.ItemVersionDataConflict;
import org.openecomp.conflicts.types.Conflict;
import org.openecomp.conflicts.types.ConflictInfo;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.versioning.dao.impl.zusammen.convertor.ItemVersionToVersionConvertor;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.types.ElementPropertyName;

import java.util.stream.Collectors;

public class ItemVersionConflictConvertorFromZusammen {
  public org.openecomp.conflicts.types.ItemVersionConflict convert(String itemId, Version version,
                                                                   ItemVersionConflict source) {
    org.openecomp.conflicts.types.ItemVersionConflict target =
        new org.openecomp.conflicts.types.ItemVersionConflict();

    target.setVersionConflict(
        convertVersionDataConflict(itemId, version, source.getVersionDataConflict()));
    target.setElementConflicts(source.getElementConflictInfos().stream()
        .map(this::convertElementConflictInfo)
        .collect(Collectors.toList()));

    return target;
  }


  private Conflict<Version> convertVersionDataConflict(String itemId, Version version,
                                                       ItemVersionDataConflict versionDataConflict) {
    if (versionDataConflict == null) {
      return null;
    }

    Conflict<Version> conflict =
        new Conflict<>(version.getId(), ElementType.itemVersion, null);

    ItemVersionToVersionConvertor convertor = new ItemVersionToVersionConvertor();
    conflict.setYours(convertor.convert(
        getItemVersion(version.getId(), versionDataConflict.getLocalData())));
    conflict.setTheirs(convertor.convert(
        getItemVersion(version.getId(), versionDataConflict.getRemoteData())));
    return conflict;
  }

  private ItemVersion getItemVersion(String versionId, ItemVersionData versionData) {
    if (versionData == null) {
      return null;
    }
    ItemVersion itemVersion = new ItemVersion();
    itemVersion.setId(new Id(versionId));
    itemVersion.setData(versionData);
    return itemVersion;
  }

  private ConflictInfo convertElementConflictInfo(ElementConflictInfo elementConflictInfo) {
    ElementInfo elementInfo = elementConflictInfo.getLocalElementInfo() == null
        ? elementConflictInfo.getRemoteElementInfo()
        : elementConflictInfo.getLocalElementInfo();

    return new ConflictInfo(elementInfo.getId().getValue(),
        ElementType
            .valueOf(elementInfo.getInfo().getProperty(ElementPropertyName.elementType.name())),
        elementInfo.getInfo().getName());
  }
}
