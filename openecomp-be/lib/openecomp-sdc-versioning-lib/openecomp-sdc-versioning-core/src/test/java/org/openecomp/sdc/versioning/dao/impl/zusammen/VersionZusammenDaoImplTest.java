package org.openecomp.sdc.versioning.dao.impl.zusammen;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.UserInfo;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.datatypes.item.ItemVersionStatus;
import com.amdocs.zusammen.datatypes.item.SynchronizationStatus;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

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
  public void testList() throws Exception {
    String itemId = "itemId";
    Id versionId1 = new Id("v1_id");
    Id versionId2 = new Id("v2_id");
    Id versionId3 = new Id("v3_id");

    List<ItemVersion> zusammenVersions = Stream.of(
        createZusammenVersion(versionId1, null, "version desc", "1.0", VersionStatus.Certified),
        createZusammenVersion(versionId2, versionId1, "version desc", "2.0", VersionStatus.Certified
        ),
        createZusammenVersion(versionId3, versionId2, "version desc", "3.0", VersionStatus.Draft))
        .collect(Collectors.toList());
    doReturn(zusammenVersions).when(zusammenAdaptorMock)
        .listPublicVersions(eq(createZusammenContext()), eq(new Id(itemId)));

    List<Version> versions = versionDao.list(itemId);
    Assert.assertEquals(versions.size(), 3);

    int zusammenVersionIndex;
    for (Version version : versions) {
      zusammenVersionIndex = versionId1.getValue().equals(version.getId())
          ? 0
          : versionId2.getValue().equals(version.getId())
              ? 1
              : 2;
      assetVersionEquals(version, zusammenVersions.get(zusammenVersionIndex));
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

    doReturn(new Id("versionId")).when(zusammenAdaptorMock)
        .createVersion(eq(createZusammenContext()), eq(new Id(itemId)),
            baseId == null ? isNull(Id.class) : eq(new Id(baseId)), any(ItemVersionData.class));

    ArgumentCaptor<ItemVersionData> capturedZusammenVersion =
        ArgumentCaptor.forClass(ItemVersionData.class);

    versionDao.create(itemId, version);

    verify(zusammenAdaptorMock)
        .createVersion(eq(createZusammenContext()), eq(new Id(itemId)),
            baseId == null ? isNull(Id.class) : eq(new Id(baseId)),
            capturedZusammenVersion.capture());

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
        .updateVersion(eq(createZusammenContext()), eq(new Id(itemId)), eq(new Id(version.getId())),
            capturedZusammenVersion.capture());

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
  public void testGet() throws Exception {
    String itemId = "itemId";
    String versionId = "versionId";

    SessionContext zusammenContext = createZusammenContext();
    Id itemIdObj = new Id(itemId);
    Id versionIdObj = new Id(versionId);

    ItemVersion zusammenPrivateVersion =
        createZusammenVersion(versionIdObj, new Id("baseId"), "version desc  updated", "2.0",
            VersionStatus.Draft);
    doReturn(zusammenPrivateVersion).when(zusammenAdaptorMock)
        .getVersion(eq(zusammenContext), eq(itemIdObj), eq(versionIdObj));

    doReturn(new ItemVersionStatus(SynchronizationStatus.UP_TO_DATE, true))
        .when(zusammenAdaptorMock)
        .getVersionStatus(eq(zusammenContext), eq(itemIdObj), eq(versionIdObj));

    ItemVersion zusammenPublicVersion =
        createZusammenVersion(versionIdObj, new Id("baseId"), "version desc", "2.0",
            VersionStatus.Certified);
    doReturn(zusammenPublicVersion).when(zusammenAdaptorMock)
        .getPublicVersion(eq(zusammenContext), eq(itemIdObj), eq(versionIdObj));

    Optional<Version> version = versionDao.get(itemId, new Version(versionId));

    Assert.assertTrue(version.isPresent());
    zusammenPrivateVersion.getData().getInfo()
        .addProperty(VersionZusammenDaoImpl.ZusammenProperty.STATUS,
            VersionStatus.Certified.name());
    assetVersionEquals(version.get(), zusammenPrivateVersion);
  }

  // TODO: 12/20/2017 complete tests
 /* @Test
  public void testDelete() throws Exception {

  }

  @Test
  public void testPublish() throws Exception {

  }

  @Test
  public void testSync() throws Exception {

  }

  @Test
  public void testForceSync() throws Exception {

  }

  @Test
  public void testRevert() throws Exception {

  }

  @Test
  public void testListRevisions() throws Exception {

  }*/

  private void assetVersionEquals(Version version, ItemVersion zusammenVersion) {
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

  private SessionContext createZusammenContext() {
    SessionContext sessionContext = new SessionContext();
    sessionContext.setUser(new UserInfo(USER));
    sessionContext.setTenant("dox");
    return sessionContext;
  }

}