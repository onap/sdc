package org.openecomp.sdc.versioning.dao.impl;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.datatypes.itemversion.Tag;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.versioning.dao.VersionableEntityDao;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;

import java.util.Optional;

public class VersionableEntityDaoZusammenImpl implements VersionableEntityDao {

  private ZusammenAdaptor zusammenAdaptor;

  public VersionableEntityDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void initVersion(VersionableEntityMetadata versionableTableMetadata, String entityId,
                          Version baseVersion, Version newVersion) {
    // redundant in zusammen impl.
  }

  @Override
  public void deleteVersion(VersionableEntityMetadata versionableTableMetadata, String entityId,
                            Version versionToDelete, Version backToVersion) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(entityId);
    Id versionId = getItemVersionId(itemId, context);
    zusammenAdaptor.resetVersionHistory(context, itemId, versionId, backToVersion.toString());
  }

  @Override
  public void closeVersion(VersionableEntityMetadata versionableTableMetadata, String entityId,
                           Version versionToClose) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(entityId);
    Id versionId = getItemVersionId(itemId, context);
    zusammenAdaptor
        .tagVersion(context, itemId, versionId, new Tag(versionToClose.toString(), null));
  }

  // TODO: 3/19/2017 move to a common util
  private Id getItemVersionId(Id itemId, SessionContext context) {
    Optional<ItemVersion> itemVersionOptional = zusammenAdaptor.getFirstVersion(context, itemId);
    ItemVersion itemVersion = itemVersionOptional.orElseThrow(() ->
        new RuntimeException(String.format("No version was found for item %s.", itemId)));
    return itemVersion.getId();
  }
}
