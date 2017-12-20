package org.openecomp.core.zusammen.plugin.collaboration.impl;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.Space;
import com.amdocs.zusammen.datatypes.itemversion.ItemVersionRevisions;
import com.amdocs.zusammen.datatypes.itemversion.Revision;
import org.openecomp.core.zusammen.plugin.collaboration.VersionPublicStore;
import org.openecomp.core.zusammen.plugin.dao.VersionDao;
import org.openecomp.core.zusammen.plugin.dao.VersionDaoFactory;
import org.openecomp.core.zusammen.plugin.dao.VersionSynchronizationStateRepository;
import org.openecomp.core.zusammen.plugin.dao.VersionSynchronizationStateRepositoryFactory;
import org.openecomp.core.zusammen.plugin.dao.types.SynchronizationStateEntity;
import org.openecomp.core.zusammen.plugin.dao.types.VersionContext;
import org.openecomp.core.zusammen.plugin.dao.types.VersionEntity;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.openecomp.core.zusammen.plugin.ZusammenPluginUtil.getSpaceName;

public class VersionPublicStoreImpl implements VersionPublicStore {
  @Override
  public Optional<VersionEntity> get(SessionContext context, Id itemId, Id versionId) {
    return getVersionDao(context)
        .get(context, getSpaceName(context, Space.PUBLIC), itemId, versionId);
  }

  @Override
  public Optional<SynchronizationStateEntity> getSynchronizationState(SessionContext context,
                                                                      Id itemId, Id versionId) {
    Id revisionId = getLastItemVersionRevision(context, itemId,
        versionId);
    if (revisionId == null) {
      return Optional.empty();
    }

    return getVersionSyncStateRepository(context)
        .get(context, new VersionContext(getSpaceName(context, Space.PUBLIC), itemId),
            new SynchronizationStateEntity(versionId, revisionId));
  }


  @Override
  public void create(SessionContext context, Id itemId, VersionEntity version, Id revisionId,
                     Map<Id, Id> versionElementIds, Date
                         publishTime, String message) {
    String publicSpace = getSpaceName(context, Space.PUBLIC);

    getVersionDao(context).create(context, publicSpace, itemId, version);

    getVersionDao(context).createVersionElements(context, publicSpace, itemId, version.getId(),
        revisionId, versionElementIds, publishTime,message);

    getVersionSyncStateRepository(context).create(context, new VersionContext(publicSpace,
            itemId),
        new SynchronizationStateEntity(version.getId(), revisionId, publishTime, false));
  }

  @Override
  public void update(SessionContext context, Id itemId, VersionEntity version,
                     Id revisionId, Map<Id, Id> versionElementIds, Date publishTime, String message) {
    String publicSpace = getSpaceName(context, Space.PUBLIC);

    getVersionDao(context).
        createVersionElements(context, publicSpace, itemId, version.getId(),
            revisionId, versionElementIds, publishTime,message);

    getVersionSyncStateRepository(context).
        updatePublishTime(context, new VersionContext(publicSpace, itemId),
            new SynchronizationStateEntity(version.getId(), revisionId, publishTime, false));
  }

  @Override
  public boolean checkHealth(SessionContext context) {
    return getVersionDao(context).checkHealth(context);
  }

  @Override
  public ItemVersionRevisions listItemVersionRevisions(SessionContext context, Id itemId,
                                                       Id versionId) {
    VersionContext entityContext = new VersionContext(getSpaceName(context, Space.PUBLIC), itemId);
    List<SynchronizationStateEntity> versionRevisions = getVersionSyncStateRepository(context)
        .list(context, entityContext, new VersionEntity(versionId));

    if (versionRevisions == null || versionRevisions.size() == 0) {
      return null;
    }

    versionRevisions.sort(new Comparator<SynchronizationStateEntity>() {
      @Override
      public int compare(SynchronizationStateEntity o1, SynchronizationStateEntity o2) {
        if (o1.getPublishTime().after(o2.getPublishTime())) {
          return -1;
        } else {
          return 1;
        }
      }
    });
    ItemVersionRevisions itemVersionRevisions = new ItemVersionRevisions();
    versionRevisions.forEach(synchronizationStateEntity -> itemVersionRevisions.addChange
        (convertSyncState2Revision(synchronizationStateEntity)));
    return itemVersionRevisions;
  }

  private Revision convertSyncState2Revision(
      SynchronizationStateEntity synchronizationStateEntity) {
    Revision revision = new Revision();
    revision.setRevisionId(synchronizationStateEntity.getRevisionId());
    revision.setTime(synchronizationStateEntity.getPublishTime());
    revision.setMessage(synchronizationStateEntity.getMessage());
    revision.setUser(synchronizationStateEntity.getUser());
    return revision;
  }


  private Id getLastItemVersionRevision(SessionContext context, Id itemId, Id versionId) {

    ItemVersionRevisions versionRevisions = listItemVersionRevisions(context, itemId, versionId);
    if(versionRevisions ==null ) return null;
    return versionRevisions.getItemVersionRevisions().get(0).getRevisionId();
  }

  protected VersionDao getVersionDao(SessionContext context) {
    return VersionDaoFactory.getInstance().createInterface(context);
  }

  protected VersionSynchronizationStateRepository getVersionSyncStateRepository(
      SessionContext context) {
    return VersionSynchronizationStateRepositoryFactory.getInstance().createInterface(context);
  }
}
