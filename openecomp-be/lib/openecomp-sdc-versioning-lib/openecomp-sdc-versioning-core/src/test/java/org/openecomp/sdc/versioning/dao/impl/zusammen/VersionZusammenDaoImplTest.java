package org.openecomp.sdc.versioning.dao.impl.zusammen;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.datatypes.item.ItemVersionStatus;
import com.amdocs.zusammen.datatypes.item.SynchronizationStatus;
import com.amdocs.zusammen.datatypes.itemversion.ItemVersionRevisions;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.versioning.dao.types.Revision;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.openecomp.sdc.versioning.dao.impl.zusammen.TestUtil.createZusammenContext;

public class VersionZusammenDaoImplTest {

  private static final String USER = "user1";
  @Mock
  private ZusammenAdaptor zusammenAdaptorMock;
  @InjectMocks
  private VersionZusammenDaoImpl versionDao;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    SessionContextProviderFactory.getInstance().createInterface().create(USER);
  }

  @Test
  public void testListWhenNone() throws Exception {
    String itemId = "itemId";

    doReturn(new ArrayList<>()).when(zusammenAdaptorMock)
        .listPublicVersions(eq(createZusammenContext(USER)), eq(new Id(itemId)));

    List<Version> versions = versionDao.list(itemId);

    Assert.assertTrue(versions.isEmpty());
  }

  @Test
  public void testList() throws Exception {
    String itemId = "itemId";
    Id versionId1 = new Id("v1_id");
    Id versionId2 = new Id("v2_id");
    Id versionId3 = new Id("v3_id");

    List<ItemVersion> zusammenVersions = Stream.of(
        createZusammenVersion(versionId1, null, "version desc", "1.0", VersionStatus.Certified),
        createZusammenVersion(versionId2, versionId1, "version desc", "2.0",
            VersionStatus.Certified),
        createZusammenVersion(versionId3, versionId2, "version desc", "3.0", VersionStatus.Draft))
        .collect(Collectors.toList());
    doReturn(zusammenVersions).when(zusammenAdaptorMock)
        .listPublicVersions(eq(createZusammenContext(USER)), eq(new Id(itemId)));

    List<Version> versions = versionDao.list(itemId);
    Assert.assertEquals(versions.size(), 3);

    int zusammenVersionIndex;
    for (Version version : versions) {
      zusammenVersionIndex = versionId1.getValue().equals(version.getId())
          ? 0
          : versionId2.getValue().equals(version.getId())
              ? 1
              : 2;
      assetVersionEquals(version, zusammenVersions.get(zusammenVersionIndex), null);
    }
  }

  @Test
  public void testCreate() throws Exception {
    testCreate(null);
  }

  @Test
  public void testCreateBasedOn() throws Exception {
    testCreate("baseId");
  }

  private void testCreate(String baseId) {
    String itemId = "itemId";
    Version version = new Version(1, 0);
    version.setBaseId(baseId);
    version.setName("version name");
    version.setDescription("version description");
    version.setStatus(VersionStatus.Draft);

    ArgumentCaptor<ItemVersionData> capturedZusammenVersion =
        ArgumentCaptor.forClass(ItemVersionData.class);

    String versionId = "versionId";
    doReturn(new Id(versionId)).when(zusammenAdaptorMock)
        .createVersion(eq(createZusammenContext(USER)), eq(new Id(itemId)),
            baseId == null ? isNull(Id.class) : eq(new Id(baseId)),
            capturedZusammenVersion.capture());


    versionDao.create(itemId, version);

    Assert.assertEquals(version.getId(), versionId);

    Info capturedInfo = capturedZusammenVersion.getValue().getInfo();
    Assert.assertEquals(capturedInfo.getName(), version.getName());
    Assert.assertEquals(capturedInfo.getDescription(), version.getDescription());
    Assert.assertEquals(VersionStatus
            .valueOf(capturedInfo.getProperty(VersionZusammenDaoImpl.ZusammenProperty.STATUS)),
        version.getStatus());
    Assert.assertEquals(capturedInfo.getProperty(VersionZusammenDaoImpl.ZusammenProperty.LABEL),
        version.toString());
  }

  @Test
  public void testUpdate() throws Exception {
    String itemId = "itemId";
    Version version = new Version(1, 0);
    version.setId("versionId");
    version.setBaseId("baseId");
    version.setName("version name");
    version.setDescription("version description");
    version.setStatus(VersionStatus.Certified);

    ArgumentCaptor<ItemVersionData> capturedZusammenVersion =
        ArgumentCaptor.forClass(ItemVersionData.class);

    versionDao.update(itemId, version);

    verify(zusammenAdaptorMock)
        .updateVersion(eq(createZusammenContext(USER)), eq(new Id(itemId)),
            eq(new Id(version.getId())), capturedZusammenVersion.capture());

    Info capturedInfo = capturedZusammenVersion.getValue().getInfo();
    Assert.assertEquals(capturedInfo.getName(), version.getName());
    Assert.assertEquals(capturedInfo.getDescription(), version.getDescription());
    Assert.assertEquals(VersionStatus
            .valueOf(capturedInfo.getProperty(VersionZusammenDaoImpl.ZusammenProperty.STATUS)),
        version.getStatus());
    Assert.assertEquals(capturedInfo.getProperty(VersionZusammenDaoImpl.ZusammenProperty.LABEL),
        version.toString());
  }

  @Test
  public void testGetNonExisting() throws Exception {
    Optional<Version> version = versionDao.get("itemId", new Version("versionId"));

    Assert.assertEquals(version, Optional.empty());
  }

  @Test
  public void testGetSynced() throws Exception {
    String itemId = "itemId";
    String versionId = "versionId";

    SessionContext zusammenContext = createZusammenContext(USER);
    Id itemIdObj = new Id(itemId);
    Id versionIdObj = new Id(versionId);

    ItemVersion zusammenPrivateVersion =
        createZusammenVersion(versionIdObj, new Id("baseId"), "version desc  updated", "2.0",
            VersionStatus.Draft);
    doReturn(zusammenPrivateVersion).when(zusammenAdaptorMock)
        .getVersion(eq(zusammenContext), eq(itemIdObj), eq(versionIdObj));

    ItemVersionStatus zusammenVersionStatus =
        new ItemVersionStatus(SynchronizationStatus.UP_TO_DATE, true);
    doReturn(zusammenVersionStatus).when(zusammenAdaptorMock)
        .getVersionStatus(eq(zusammenContext), eq(itemIdObj), eq(versionIdObj));

    Optional<Version> version = versionDao.get(itemId, new Version(versionId));

    Assert.assertTrue(version.isPresent());
    assetVersionEquals(version.get(), zusammenPrivateVersion, zusammenVersionStatus);
  }

  @Test
  public void testGetOutOfSync() throws Exception {
    String itemId = "itemId";
    String versionId = "versionId";

    SessionContext zusammenContext = createZusammenContext(USER);
    Id itemIdObj = new Id(itemId);
    Id versionIdObj = new Id(versionId);

    ItemVersion zusammenPrivateVersion =
        createZusammenVersion(versionIdObj, new Id("baseId"), "version desc updated", "2.0",
            VersionStatus.Draft);
    doReturn(zusammenPrivateVersion).when(zusammenAdaptorMock)
        .getVersion(eq(zusammenContext), eq(itemIdObj), eq(versionIdObj));

    ItemVersionStatus zusammenVersionStatus =
        new ItemVersionStatus(SynchronizationStatus.OUT_OF_SYNC, true);
    doReturn(zusammenVersionStatus).when(zusammenAdaptorMock)
        .getVersionStatus(eq(zusammenContext), eq(itemIdObj), eq(versionIdObj));

    VersionStatus statusOnPublic = VersionStatus.Certified;
    ItemVersion zusammenPublicVersion =
        createZusammenVersion(versionIdObj, new Id("baseId"), "version desc", "2.0",
            statusOnPublic);
    doReturn(zusammenPublicVersion).when(zusammenAdaptorMock)
        .getPublicVersion(eq(zusammenContext), eq(itemIdObj), eq(versionIdObj));

    Optional<Version> version = versionDao.get(itemId, new Version(versionId));

    Assert.assertTrue(version.isPresent());
    zusammenPrivateVersion.getData().getInfo()
        .addProperty(VersionZusammenDaoImpl.ZusammenProperty.STATUS, statusOnPublic.name());
    assetVersionEquals(version.get(), zusammenPrivateVersion, zusammenVersionStatus);
  }

  @Test
  public void testGetMerging() throws Exception {
    String itemId = "itemId";
    String versionId = "versionId";

    SessionContext zusammenContext = createZusammenContext(USER);
    Id itemIdObj = new Id(itemId);
    Id versionIdObj = new Id(versionId);

    ItemVersion zusammenPrivateVersion =
        createZusammenVersion(versionIdObj, new Id("baseId"), "version desc", "2.0",
            VersionStatus.Draft);
    doReturn(zusammenPrivateVersion).when(zusammenAdaptorMock)
        .getVersion(eq(zusammenContext), eq(itemIdObj), eq(versionIdObj));

    ItemVersionStatus zusammenVersionStatus =
        new ItemVersionStatus(SynchronizationStatus.MERGING, true);
    doReturn(zusammenVersionStatus).when(zusammenAdaptorMock)
        .getVersionStatus(eq(zusammenContext), eq(itemIdObj), eq(versionIdObj));

    ItemVersion zusammenPublicVersion =
        createZusammenVersion(versionIdObj, new Id("baseId"), "version desc", "2.0",
            VersionStatus.Draft);
    doReturn(zusammenPublicVersion).when(zusammenAdaptorMock)
        .getPublicVersion(eq(zusammenContext), eq(itemIdObj), eq(versionIdObj));

    Optional<Version> version = versionDao.get(itemId, new Version(versionId));

    Assert.assertTrue(version.isPresent());
    assetVersionEquals(version.get(), zusammenPrivateVersion, zusammenVersionStatus);
  }

  @Test
  public void testPublish() throws Exception {
    String itemId = "itemId";
    String versionId = "versionId";
    String message = "publish message";

    versionDao.publish(itemId, new Version(versionId), message);

    verify(zusammenAdaptorMock)
        .publishVersion(eq(createZusammenContext(USER)), eq(new Id(itemId)), eq(new Id(versionId)),
            eq(message));
  }

  @Test
  public void testSync() throws Exception {
    String itemId = "itemId";
    String versionId = "versionId";

    versionDao.sync(itemId, new Version(versionId));

    verify(zusammenAdaptorMock)
        .syncVersion(eq(createZusammenContext(USER)), eq(new Id(itemId)), eq(new Id(versionId)));
  }

  @Test
  public void testForceSync() throws Exception {
    String itemId = "itemId";
    String versionId = "versionId";

    versionDao.forceSync(itemId, new Version(versionId));

    verify(zusammenAdaptorMock)
        .forceSyncVersion(eq(createZusammenContext(USER)), eq(new Id(itemId)),
            eq(new Id(versionId)));
  }

  @Test
  public void testRevert() throws Exception {
    String itemId = "itemId";
    String versionId = "versionId";
    String revisionId = "revisionId";

    versionDao.revert(itemId, new Version(versionId), revisionId);

    verify(zusammenAdaptorMock)
        .revert(eq(createZusammenContext(USER)), eq(new Id(itemId)), eq(new Id(versionId)),
            eq(new Id(revisionId)));
  }

  @Test
  public void testListRevisionsWhenNone() throws Exception {
    String itemId = "itemId";
    String versionId = "versionId";

    List<Revision> revisions = versionDao.listRevisions(itemId, new Version(versionId));

    Assert.assertTrue(revisions.isEmpty());
  }

  @Test
  public void testListRevisions() throws Exception {
    String itemId = "itemId";
    String versionId = "versionId";

    long currentTime = System.currentTimeMillis();
    Date rev4time = new Date(currentTime);            // latest
    Date rev3time = new Date(currentTime - 1);
    Date rev2time = new Date(currentTime - 2);
    Date rev1time = new Date(currentTime - 3);  // oldest
    List<com.amdocs.zusammen.datatypes.itemversion.Revision> zusammenRevisions = Stream.of(
        createZusammenRevision("rev4", "forth rev", "user1", rev4time),
        createZusammenRevision("rev1", "first rev", "user2", rev1time),
        createZusammenRevision("rev3", "third rev", "user2", rev3time),
        createZusammenRevision("rev2", "second rev", "user1", rev2time))
        .collect(Collectors.toList());
    ItemVersionRevisions toBeReturned = new ItemVersionRevisions();
    toBeReturned.setItemVersionRevisions(zusammenRevisions);
    doReturn(toBeReturned).when(zusammenAdaptorMock)
        .listRevisions(eq(createZusammenContext(USER)), eq(new Id(itemId)), eq(new Id(versionId)));

    List<Revision> revisions = versionDao.listRevisions(itemId, new Version(versionId));

    Assert.assertEquals(revisions.size(), 4);
    assertRevisionEquals(revisions.get(0), zusammenRevisions.get(0)); // rev4 - latest
    assertRevisionEquals(revisions.get(1), zusammenRevisions.get(2)); // rev3
    assertRevisionEquals(revisions.get(2), zusammenRevisions.get(3)); // rev2
    assertRevisionEquals(revisions.get(3), zusammenRevisions.get(1)); // rev1 - oldest
  }

  private ItemVersion createZusammenVersion(Id id, Id baseId, String description, String label,
                                            VersionStatus status) {
    ItemVersion version = new ItemVersion();
    version.setId(id);
    version.setBaseId(baseId);
    Info info = new Info();
    info.setName(id + "_name");
    info.setDescription(description);
    info.addProperty(VersionZusammenDaoImpl.ZusammenProperty.LABEL, label);
    info.addProperty(VersionZusammenDaoImpl.ZusammenProperty.STATUS, status.name());
    ItemVersionData data = new ItemVersionData();
    data.setInfo(info);
    version.setData(data);
    version.setCreationTime(new Date());
    version.setModificationTime(new Date());
    return version;
  }

  private void assetVersionEquals(Version version, ItemVersion zusammenVersion,
                                  ItemVersionStatus zusammenVersionStatus) {
    Assert.assertEquals(version.getId(), zusammenVersion.getId().getValue());
    Assert.assertEquals(version.getBaseId(),
        zusammenVersion.getBaseId() == null ? null : zusammenVersion.getBaseId().getValue());
    Info info = zusammenVersion.getData().getInfo();
    Assert.assertEquals(version.getName(), info.getName());
    Assert.assertEquals(version.getDescription(), info.getDescription());
    Assert.assertEquals(version.getStatus(),
        VersionStatus.valueOf(info.getProperty(VersionZusammenDaoImpl.ZusammenProperty.STATUS)));
    String label = info.getProperty(VersionZusammenDaoImpl.ZusammenProperty.LABEL).toString();
    Assert
        .assertEquals(version.getMajor(), Integer.parseInt(label.substring(0, label.indexOf('.'))));
    Assert.assertEquals(version.getMinor(),
        Integer.parseInt(label.substring(label.indexOf('.') + 1, label.length())));
    Assert.assertEquals(version.getCreationTime(), zusammenVersion.getCreationTime());
    Assert.assertEquals(version.getModificationTime(), zusammenVersion.getModificationTime());

    if (zusammenVersionStatus != null) {
      Assert.assertEquals(version.getState().isDirty(), zusammenVersionStatus.isDirty());
      Assert.assertEquals(version.getState().getSynchronizationState().toString(),
          zusammenVersionStatus.getSynchronizationStatus().toString());
    }
  }

  private com.amdocs.zusammen.datatypes.itemversion.Revision createZusammenRevision(String id,
                                                                                    String message,
                                                                                    String user,
                                                                                    Date time) {
    com.amdocs.zusammen.datatypes.itemversion.Revision revision = new com.amdocs.zusammen
        .datatypes.itemversion.Revision();
    revision.setRevisionId(new Id(id));
    revision.setMessage(message);
    revision.setUser(user);
    revision.setTime(time);
    return revision;
  }

  private void assertRevisionEquals(
      Revision revision,
      com.amdocs.zusammen.datatypes.itemversion.Revision zusammenRevision) {
    Assert.assertEquals(revision.getId(), zusammenRevision.getRevisionId().getValue());
    Assert.assertEquals(revision.getMessage(), zusammenRevision.getMessage());
    Assert.assertEquals(revision.getUser(), zusammenRevision.getUser());
    Assert.assertEquals(revision.getTime(), zusammenRevision.getTime());
  }
}