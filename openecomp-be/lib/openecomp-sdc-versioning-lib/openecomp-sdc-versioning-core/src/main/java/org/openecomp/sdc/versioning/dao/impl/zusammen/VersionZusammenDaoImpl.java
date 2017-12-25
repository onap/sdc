package org.openecomp.sdc.versioning.dao.impl.zusammen;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.datatypes.item.ItemVersionStatus;
import com.amdocs.zusammen.datatypes.item.SynchronizationStatus;
import com.amdocs.zusammen.datatypes.itemversion.ItemVersionRevisions;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.versioning.dao.VersionDao;
import org.openecomp.sdc.versioning.dao.impl.zusammen.convertor.ItemVersionToVersionConvertor;
import org.openecomp.sdc.versioning.dao.types.Revision;
import org.openecomp.sdc.versioning.dao.types.SynchronizationState;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

public class VersionZusammenDaoImpl implements VersionDao {

  public static final class ZusammenProperty {
    public static final String LABEL = "label";
    public static final String STATUS = "status";

    private ZusammenProperty() {
      throw new IllegalStateException("Constants class");
    }
  }

  private ZusammenAdaptor zusammenAdaptor;

  public VersionZusammenDaoImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public List<Version> list(String itemId) {
    ItemVersionToVersionConvertor convertor = new ItemVersionToVersionConvertor();
    return zusammenAdaptor.listPublicVersions(createSessionContext(), new Id(itemId)).stream()
        .map(convertor::convert)
        .collect(Collectors.toList());
  }

  @Override
  public void create(String itemId, Version version) {
    Id versionId =
        zusammenAdaptor.createVersion(createSessionContext(), new Id(itemId),
            version.getBaseId() == null ? null : new Id(version.getBaseId()),
            mapToZusammenVersion(version));

    version.setId(versionId.getValue());
  }

  @Override
  public void update(String itemId, Version version) {
    zusammenAdaptor.updateVersion(createSessionContext(), new Id(itemId), new Id(version.getId()),
        mapToZusammenVersion(version));
  }

  @Override
  public Optional<Version> get(String itemId, Version version) {
    SessionContext context = createSessionContext();
    Id itemIdObj = new Id(itemId);
    Id versionId = new Id(version.getId());
    ItemVersion itemVersion = zusammenAdaptor.getVersion(context, itemIdObj, versionId);

    if (itemVersion == null) {
      return Optional.empty();
    }

    VersionState versionState =
        convertState(zusammenAdaptor.getVersionStatus(context, itemIdObj, versionId));
    updateVersionStatus(context, itemIdObj, versionId, versionState, itemVersion);

    Version result = new ItemVersionToVersionConvertor().convert(itemVersion);
    result.setState(versionState);
    return Optional.of(result);
  }

  @Override
  public void delete(String itemId, Version version) {
    throw new UnsupportedOperationException("Delete version operation is not yet supported.");
  }

  @Override
  public void publish(String itemId, Version version, String message) {
    zusammenAdaptor
        .publishVersion(createSessionContext(), new Id(itemId), new Id(version.getId()), message);
  }

  @Override
  public void sync(String itemId, Version version) {
    zusammenAdaptor
        .syncVersion(createSessionContext(), new Id(itemId), new Id(version.getId()));
  }

  @Override
  public void forceSync(String itemId, Version version) {
    zusammenAdaptor
        .forceSyncVersion(createSessionContext(), new Id(itemId), new Id(version.getId()));
  }

  @Override
  public void revert(String itemId, Version version, String revisionId) {
    zusammenAdaptor.revert(createSessionContext(), new Id(itemId), new Id(version.getId()),
        new Id(revisionId));
  }

  @Override
  public List<Revision> listRevisions(String itemId, Version version) {
    ItemVersionRevisions itemVersionRevisions = zusammenAdaptor
        .listRevisions(createSessionContext(), new Id(itemId), new Id(version.getId()));

    return itemVersionRevisions == null || itemVersionRevisions.getItemVersionRevisions() == null ||
        itemVersionRevisions.getItemVersionRevisions().isEmpty()
        ? new ArrayList<>()
        : itemVersionRevisions.getItemVersionRevisions().stream()
            .map(this::convertRevision)
            .sorted(this::compareRevisionsTime)
            .collect(Collectors.toList());
  }

  private void updateVersionStatus(SessionContext context, Id itemId, Id versionId,
                                   VersionState versionState, ItemVersion itemVersion) {
    if (versionState.getSynchronizationState() != SynchronizationState.UpToDate) {
      String versionStatus = zusammenAdaptor.getPublicVersion(context, itemId, versionId)
          .getData().getInfo().getProperty(ZusammenProperty.STATUS);
      itemVersion.getData().getInfo().addProperty(ZusammenProperty.STATUS, versionStatus);
    }
  }

  private ItemVersionData mapToZusammenVersion(Version version) {
    Info info = new Info();
    info.addProperty(ZusammenProperty.LABEL, version.toString());
    info.addProperty(ZusammenProperty.STATUS, version.getStatus().name());
    info.setName(version.getName());
    info.setDescription(version.getDescription());

    ItemVersionData itemVersionData = new ItemVersionData();
    itemVersionData.setInfo(info);
    return itemVersionData;
  }

  private VersionState convertState(ItemVersionStatus versionStatus) {
    VersionState state = new VersionState();
    state.setSynchronizationState(getSyncState(versionStatus.getSynchronizationStatus()));
    state.setDirty(versionStatus.isDirty());
    return state;
  }

  private SynchronizationState getSyncState(SynchronizationStatus synchronizationStatus) {
    switch (synchronizationStatus) {
      case UP_TO_DATE:
        return SynchronizationState.UpToDate;
      case OUT_OF_SYNC:
        return SynchronizationState.OutOfSync;
      case MERGING:
        return SynchronizationState.Merging;
      default:
        throw new CoreException(new ErrorCode.ErrorCodeBuilder()
            .withCategory(ErrorCategory.APPLICATION)
            .withId("UNKNOWN_VERSION_STATE")
            .withMessage("Version state is unknown").build());
    }
  }

  private Revision convertRevision(
      com.amdocs.zusammen.datatypes.itemversion.Revision zusammenRevision) {
    Revision revision = new Revision();
    revision.setId(zusammenRevision.getRevisionId().getValue());
    revision.setTime(zusammenRevision.getTime());
    revision.setUser(zusammenRevision.getUser());
    revision.setMessage(zusammenRevision.getMessage());
    return revision;
  }

  private int compareRevisionsTime(Revision revision1, Revision revision2) {
    return revision1.getTime().before(revision2.getTime()) ? 1 : -1;
  }
}
