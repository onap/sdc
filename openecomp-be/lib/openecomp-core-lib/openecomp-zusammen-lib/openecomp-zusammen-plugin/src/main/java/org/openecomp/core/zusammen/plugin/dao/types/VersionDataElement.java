package org.openecomp.core.zusammen.plugin.dao.types;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import org.openecomp.core.zusammen.plugin.ZusammenPluginConstants;

import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.calculateElementHash;

public class VersionDataElement extends ElementEntity {

  public VersionDataElement() {
    super(ZusammenPluginConstants.ROOT_ELEMENTS_PARENT_ID);
  }

  public VersionDataElement(ItemVersionData itemVersionData) {
    this();
    setInfo(itemVersionData.getInfo());
    setRelations(itemVersionData.getRelations());
    setElementHash(new Id(calculateElementHash(this)));
  }
}
