package org.openecomp.core.zusammen.plugin.collaboration.impl;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import org.openecomp.core.zusammen.plugin.collaboration.VersionPrivateStore;
import org.openecomp.core.zusammen.plugin.dao.VersionDao;
import org.openecomp.core.zusammen.plugin.dao.VersionDaoFactory;
import org.openecomp.core.zusammen.plugin.dao.VersionSynchronizationStateRepository;
import org.openecomp.core.zusammen.plugin.dao.VersionSynchronizationStateRepositoryFactory;
import org.openecomp.core.zusammen.plugin.dao.types.SynchronizationStateEntity;
import org.openecomp.core.zusammen.plugin.dao.types.VersionContext;
import org.openecomp.core.zusammen.plugin.dao.types.VersionEntity;

import java.util.Date;
import java.util.Optional;

import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.getPrivateSpaceName;

public class VersionPrivateStoreImpl implements VersionPrivateStore {

  Id revisionId = Id.ZERO;

  @Override
  public Optional<VersionEntity> get(SessionContext context, Id itemId, Id versionId) {
    return getVersionDao(context).get(context, getPrivateSpaceName(context), itemId, versionId);
  }

  @Override
  public Optional<SynchronizationStateEntity> getSynchronizationState(SessionContext context,
                                                                      Id itemId, Id versionId) {

    return getVersionSyncStateRepository(context)
        .get(context, new VersionContext(getPrivateSpaceName(context), itemId),
            new SynchronizationStateEntity(versionId, revisionId));
  }

  @Override
  public void create(SessionContext context, Id itemId, VersionEntity version) {
    String privateSpace = getPrivateSpaceName(context);


    getVersionDao(context).create(context, privateSpace, itemId, version);
    getVersionSyncStateRepository(context).create(context, new VersionContext(privateSpace,
            itemId),
        new SynchronizationStateEntity(version.getId(), revisionId, null, true));
  }

  @Override
  public void update(SessionContext context, Id itemId, VersionEntity version) {

    getVersionDao(context)
        .updateModificationTime(context, getPrivateSpaceName(context), itemId, version.getId(),
            version.getModificationTime());
  }

  @Override
  public void update(SessionContext context, Id itemId, VersionEntity version, Date publishTime,
                     boolean dirty) {
    getVersionSyncStateRepository(context).updatePublishTime(context,
        new VersionContext(getPrivateSpaceName(context), itemId),
        new SynchronizationStateEntity(version.getId(), revisionId, publishTime, dirty));
  }

  @Override
  public void delete(SessionContext context, Id itemId, VersionEntity version) {
    String privateSpace = getPrivateSpaceName(context);

    getVersionDao(context).delete(context, privateSpace, itemId, version.getId());
    getVersionSyncStateRepository(context).delete(context, new VersionContext(privateSpace,
            itemId),
        new SynchronizationStateEntity(version.getId(), revisionId));
  }

  @Override
  public void markAsPublished(SessionContext context, Id itemId, Id versionId, Date publishTime) {
    getVersionSyncStateRepository(context)
        .updatePublishTime(context, new VersionContext(getPrivateSpaceName(context), itemId),
            new SynchronizationStateEntity(versionId, revisionId, publishTime, false));
  }

  @Override
  public void commitStagedCreate(SessionContext context, Id itemId, VersionEntity version,
                                 Date publishTime) {
    String privateSpace = getPrivateSpaceName(context);

    getVersionDao(context).create(context, privateSpace, itemId, version);
    getVersionSyncStateRepository(context).create(context, new VersionContext(privateSpace,
            itemId),
        new SynchronizationStateEntity(version.getId(), revisionId, publishTime, false));
  }

  @Override
  public void commitStagedUpdate(SessionContext context, Id itemId, VersionEntity version,
                                 Date publishTime) {
    update(context, itemId, version, publishTime, false);
  }

  @Override
  public void commitStagedIgnore(SessionContext context, Id itemId, VersionEntity version,
                                 Date publishTime) {
    getVersionSyncStateRepository(context).updatePublishTime(context,
        new VersionContext(getPrivateSpaceName(context), itemId),
        new SynchronizationStateEntity(version.getId(), revisionId, publishTime, false));
  }




  protected VersionDao getVersionDao(SessionContext context) {
    return VersionDaoFactory.getInstance().createInterface(context);
  }

  protected VersionSynchronizationStateRepository getVersionSyncStateRepository(
      SessionContext context) {
    return VersionSynchronizationStateRepositoryFactory.getInstance().createInterface(context);
  }
}
