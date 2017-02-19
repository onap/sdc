package org.openecomp.sdc.versioning;


import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.versioning.dao.VersionInfoDao;
import org.openecomp.sdc.versioning.dao.VersionInfoDaoFactory;
import org.openecomp.sdc.versioning.dao.VersionInfoDeletedDao;
import org.openecomp.sdc.versioning.dao.VersionInfoDeletedDaoFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionInfoDeletedEntity;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;
import org.openecomp.core.nosqldb.api.NoSqlDb;
import org.openecomp.core.nosqldb.factory.NoSqlDbFactory;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.UDTMapper;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VersioningManagerTest {
  private static final VersioningManager versioningManager =
      VersioningManagerFactory.getInstance().createInterface();
  private static final VersionInfoDao versionInfoDao =
      VersionInfoDaoFactory.getInstance().createInterface();
  private static final VersionInfoDeletedDao versionInfoDeletedDao =
      VersionInfoDeletedDaoFactory.getInstance().createInterface();
  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();
  private static final String USR1 = "usr1";
  private static final String USR2 = "usr2";
  private static final String USR3 = "usr3";
  private static final String TYPE1 = "Type1";
  private static final String TYPE2 = "Type2";
  private static final String ID1 = "Id1";
  private static final String ID2 = "Id2";
  private static final String ID3 = "Id3";
  private static final String TYPE1_TABLE_NAME = "vendor_license_model";
  private static final String TYPE1_ID_NAME = "vlm_id";
  private static final String TYPE1_VERSION_NAME = "version";
  private static final String TYPE2_TABLE_NAME = "feature_group";
  private static final String TYPE2_ID_NAME = "vlm_id";
  private static final String TYPE2_VERSION_NAME = "version";
  private static final Version VERSION01 = new Version(0, 1);
  private static final Version VERSION02 = new Version(0, 2);
  private static final Version VERSION10 = new Version(1, 0);
  private static final Version VERSION11 = new Version(1, 1);
  private static UDTMapper<Version> versionMapper =
      noSqlDb.getMappingManager().udtMapper(Version.class);
  private static Set<Version> expectedViewableVersionsType1Id1 = new HashSet<>();

  private static void assretVersionInfoEntity(VersionInfoEntity actual, String entityType,
                                              String entityId, Version activeVersion,
                                              Version candidateVersion, String candidateUser,
                                              VersionStatus status, Set<Version> viewbleVersions,
                                              Version latestFinalVersion) {
    Assert.assertNotNull(actual);
    Assert.assertEquals(actual.getEntityType(), entityType);
    Assert.assertEquals(actual.getEntityId(), entityId);
    Assert.assertEquals(actual.getActiveVersion(), activeVersion);
    if (candidateVersion != null && candidateUser != null) {
      Assert.assertEquals(actual.getCandidate().getVersion(), candidateVersion);
      Assert.assertEquals(actual.getCandidate().getUser(), candidateUser);
    } else {
      Assert.assertNull(actual.getCandidate());
    }
    Assert.assertEquals(actual.getStatus(), status);
    Assert.assertEquals(actual.getViewableVersions().size(), viewbleVersions.size());
    Assert.assertEquals(actual.getViewableVersions(), viewbleVersions);
    Assert.assertEquals(actual.getLatestFinalVersion(), latestFinalVersion);
  }

  private static void assretVersionInfo(VersionInfo actual, Version activeVersion,
                                        VersionStatus status, String lockingUser,
                                        Set<Version> viewableVersions, Version latestFinalVersion) {
    Assert.assertNotNull(actual);
    Assert.assertEquals(actual.getActiveVersion(), activeVersion);
    Assert.assertEquals(actual.getStatus(), status);
    Assert.assertEquals(actual.getLockingUser(), lockingUser);
    Assert.assertEquals(actual.getViewableVersions().size(), viewableVersions.size());
    Assert.assertEquals(actual.getViewableVersions(), viewableVersions);
    Assert.assertEquals(actual.getLatestFinalVersion(), latestFinalVersion);
  }

//  @BeforeClass
  private void init() {
    versionInfoDao.delete(new VersionInfoEntity(TYPE1, ID1));
    versionInfoDao.delete(new VersionInfoEntity(TYPE1, ID2));
    versionInfoDao.delete(new VersionInfoEntity(TYPE2, ID3));
    String deleteFromType1 = String
        .format("delete from %s where %s=? and %s=?", TYPE1_TABLE_NAME, TYPE1_ID_NAME,
            TYPE1_VERSION_NAME);
    noSqlDb.execute(deleteFromType1, ID1, versionMapper.toUDT(VERSION01));
    noSqlDb.execute(deleteFromType1, ID1, versionMapper.toUDT(VERSION02));
    noSqlDb.execute(deleteFromType1, ID1, versionMapper.toUDT(VERSION11));

    versioningManager.register(TYPE1,
        new VersionableEntityMetadata(TYPE1_TABLE_NAME, TYPE1_ID_NAME, TYPE1_VERSION_NAME));
    versioningManager.register(TYPE2,
        new VersionableEntityMetadata(TYPE2_TABLE_NAME, TYPE2_ID_NAME, TYPE2_VERSION_NAME));
  }

//  @Test
  public void createTest() {
    Version version = versioningManager.create(TYPE1, ID1, USR1);
    createVersionableEntityRecord(TYPE1_TABLE_NAME, TYPE1_ID_NAME, TYPE1_VERSION_NAME, ID1,
        version);

    VersionInfoEntity versionInfoEntity = versionInfoDao.get(new VersionInfoEntity(TYPE1, ID1));
    assretVersionInfoEntity(versionInfoEntity, TYPE1, ID1, new Version(0, 0), VERSION01, USR1,
        VersionStatus.Locked, expectedViewableVersionsType1Id1, null);
  }

//  @Test(dependsOnMethods = "createTest")
  public void checkinTest() {
    Version version = versioningManager.checkin(TYPE1, ID1, USR1, "checkin 0.1");
    Assert.assertEquals(version, VERSION01);

    VersionInfoEntity versionInfoEntity = versionInfoDao.get(new VersionInfoEntity(TYPE1, ID1));
    expectedViewableVersionsType1Id1.add(VERSION01);
    assretVersionInfoEntity(versionInfoEntity, TYPE1, ID1, VERSION01, null, null,
        VersionStatus.Available, expectedViewableVersionsType1Id1, null);
  }

//  @Test(dependsOnMethods = "checkinTest")
  public void getVersionInfoForReadOnAvailableTest() {
    VersionInfo versionInfo =
        versioningManager.getEntityVersionInfo(TYPE1, ID1, USR2, VersionableEntityAction.Read);
    assretVersionInfo(versionInfo, VERSION01, VersionStatus.Available, null,
        expectedViewableVersionsType1Id1, null);
  }

//  @Test(dependsOnMethods = "getVersionInfoForReadOnAvailableTest",
//      expectedExceptions = CoreException.class)
  public void getVersionInfoForWriteOnAvailableTest() {
    versioningManager.getEntityVersionInfo(TYPE1, ID1, USR2, VersionableEntityAction.Write);
  }

//  @Test(dependsOnMethods = "getVersionInfoForWriteOnAvailableTest",
//      expectedExceptions = CoreException.class)
  public void checkinOnAvailableTest() {
    versioningManager.checkin(TYPE1, ID1, USR1, "fail checkin");
  }

//  @Test(dependsOnMethods = "checkinOnAvailableTest", expectedExceptions = CoreException.class)
  public void undoCheckoutOnAvailableTest() {
    versioningManager.undoCheckout(TYPE1, ID1, USR1);
  }

//  @Test(dependsOnMethods = "undoCheckoutOnAvailableTest")
  public void checkoutTest() {
    Version version = versioningManager.checkout(TYPE1, ID1, USR2);
    Assert.assertEquals(version, VERSION02);

    VersionInfoEntity versionInfoEntity = versionInfoDao.get(new VersionInfoEntity(TYPE1, ID1));
    assretVersionInfoEntity(versionInfoEntity, TYPE1, ID1, VERSION01, VERSION02, USR2,
        VersionStatus.Locked, expectedViewableVersionsType1Id1, null);

    ResultSet results =
        loadVersionableEntityRecord(TYPE1_TABLE_NAME, TYPE1_ID_NAME, TYPE1_VERSION_NAME, ID1,
            VERSION02);
    Assert.assertTrue(results.iterator().hasNext());
  }

//  @Test(dependsOnMethods = "checkoutTest")
  public void getVersionInfoForReadOnLockedSameUserTest() {
    VersionInfo versionInfo =
        versioningManager.getEntityVersionInfo(TYPE1, ID1, USR2, VersionableEntityAction.Read);
    Set<Version> expectedViewableVersions = new HashSet<>();
    expectedViewableVersions.addAll(expectedViewableVersionsType1Id1);
    expectedViewableVersions.add(VERSION02);
    assretVersionInfo(versionInfo, VERSION02, VersionStatus.Locked, USR2, expectedViewableVersions,
        null);
  }

//  @Test(dependsOnMethods = "getVersionInfoForReadOnLockedSameUserTest")
  public void getVersionInfoForReadOnLockedOtherUserTest() {
    VersionInfo entityVersionInfo =
        versioningManager.getEntityVersionInfo(TYPE1, ID1, USR1, VersionableEntityAction.Read);
    Assert.assertEquals(entityVersionInfo.getActiveVersion(), VERSION01);
  }

//  @Test(dependsOnMethods = "getVersionInfoForReadOnLockedOtherUserTest",
//      expectedExceptions = CoreException.class)
  public void getVersionInfoForWriteOnLockedOtherUserTest() {
    versioningManager.getEntityVersionInfo(TYPE1, ID1, USR1, VersionableEntityAction.Write)
        .getActiveVersion();
  }

//  @Test(dependsOnMethods = "getVersionInfoForWriteOnLockedOtherUserTest")
  public void getVersionInfoForWriteOnLockedSameUserTest() {
    Version activeVersion =
        versioningManager.getEntityVersionInfo(TYPE1, ID1, USR2, VersionableEntityAction.Write)
            .getActiveVersion();
    Assert.assertEquals(activeVersion, VERSION02);
  }

//  @Test(dependsOnMethods = "getVersionInfoForWriteOnLockedSameUserTest",
//      expectedExceptions = CoreException.class)
  public void checkoutOnLockedSameUserTest() {
    versioningManager.checkout(TYPE1, ID1, USR2);
  }

//  @Test(dependsOnMethods = "checkoutOnLockedSameUserTest", expectedExceptions = CoreException.class)
  public void checkoutOnLockedOtherUserTest() {
    versioningManager.checkout(TYPE1, ID1, USR1);
  }

//  @Test(dependsOnMethods = "checkoutOnLockedSameUserTest", expectedExceptions = CoreException.class)
  public void undoCheckoutOnLockedOtherUserTest() {
    versioningManager.undoCheckout(TYPE1, ID1, USR1);
  }

//  @Test(dependsOnMethods = "undoCheckoutOnLockedOtherUserTest",
//      expectedExceptions = CoreException.class)
  public void submitOnLockedTest() {
    versioningManager.submit(TYPE1, ID1, USR2, "failed submit");
  }

//  @Test(dependsOnMethods = "submitOnLockedTest")
  public void undoCheckoutTest() {
    Version version = versioningManager.undoCheckout(TYPE1, ID1, USR2);
    Assert.assertEquals(version, VERSION01);

    VersionInfoEntity versionInfoEntity = versionInfoDao.get(new VersionInfoEntity(TYPE1, ID1));
    assretVersionInfoEntity(versionInfoEntity, TYPE1, ID1, VERSION01, null, null,
        VersionStatus.Available, expectedViewableVersionsType1Id1, null);

    ResultSet results =
        loadVersionableEntityRecord(TYPE1_TABLE_NAME, TYPE1_ID_NAME, TYPE1_VERSION_NAME, ID1,
            VERSION02);
    Assert.assertFalse(results.iterator().hasNext());
  }

//  @Test(dependsOnMethods = "undoCheckoutTest")
  public void submitTest() {
    Version version = versioningManager.submit(TYPE1, ID1, USR3, "submit msg");
    Assert.assertEquals(version, VERSION10);
    expectedViewableVersionsType1Id1 = new HashSet<>();
    expectedViewableVersionsType1Id1.add(version);

    VersionInfoEntity versionInfoEntity = versionInfoDao.get(new VersionInfoEntity(TYPE1, ID1));
    assretVersionInfoEntity(versionInfoEntity, TYPE1, ID1, VERSION10, null, null,
        VersionStatus.Final, expectedViewableVersionsType1Id1, VERSION10);

    ResultSet results =
        loadVersionableEntityRecord(TYPE1_TABLE_NAME, TYPE1_ID_NAME, TYPE1_VERSION_NAME, ID1,
            VERSION10);
    Assert.assertTrue(results.iterator().hasNext());
  }

//  @Test(dependsOnMethods = "submitTest", expectedExceptions = CoreException.class)
  public void checkinOnFinalizedTest() {
    versioningManager.checkin(TYPE1, ID1, USR2, "failed checkin");
  }

//  @Test(dependsOnMethods = "checkinOnFinalizedTest", expectedExceptions = CoreException.class)
  public void undoCheckouOnFinalizedTest() {
    versioningManager.undoCheckout(TYPE1, ID1, USR2);
  }

//  @Test(dependsOnMethods = "undoCheckouOnFinalizedTest", expectedExceptions = CoreException.class)
  public void submitOnFinalizedTest() {
    versioningManager.submit(TYPE1, ID1, USR2, "failed submit");
  }

//  @Test(dependsOnMethods = "submitOnFinalizedTest")
  public void checkoutOnFinalizedTest() {
    Version version = versioningManager.checkout(TYPE1, ID1, USR3);
    Assert.assertEquals(version, VERSION11);

    VersionInfoEntity versionInfoEntity = versionInfoDao.get(new VersionInfoEntity(TYPE1, ID1));
    assretVersionInfoEntity(versionInfoEntity, TYPE1, ID1, VERSION10, VERSION11, USR3,
        VersionStatus.Locked, expectedViewableVersionsType1Id1, VERSION10);

    ResultSet results =
        loadVersionableEntityRecord(TYPE1_TABLE_NAME, TYPE1_ID_NAME, TYPE1_VERSION_NAME, ID1,
            VERSION11);
    Assert.assertTrue(results.iterator().hasNext());
  }

//  @Test(dependsOnMethods = "checkoutOnFinalizedTest")
  public void viewableVersionsTest() {
    versioningManager.checkin(TYPE1, ID1, USR3, "check in 1.1");
    versioningManager.checkout(TYPE1, ID1, USR3);
    versioningManager.checkin(TYPE1, ID1, USR3, "check in 1.2");
    versioningManager.submit(TYPE1, ID1, USR3, "submit in 2.0");
    versioningManager.checkout(TYPE1, ID1, USR3);
    versioningManager.checkin(TYPE1, ID1, USR3, "check in 2.1");
    versioningManager.submit(TYPE1, ID1, USR3, "submit in 3.0");
    versioningManager.checkout(TYPE1, ID1, USR3);
    versioningManager.checkin(TYPE1, ID1, USR3, "check in 3.1");
    versioningManager.checkout(TYPE1, ID1, USR3);
    versioningManager.checkin(TYPE1, ID1, USR3, "check in 3.2");
    versioningManager.checkout(TYPE1, ID1, USR2);

    VersionInfoEntity versionInfoEntity = versionInfoDao.get(new VersionInfoEntity(TYPE1, ID1));
    HashSet<Version> expectedViewableVersions = new HashSet<>();
    expectedViewableVersions.add(VERSION10);
    expectedViewableVersions.add(new Version(2, 0));
    expectedViewableVersions.add(new Version(3, 0));
    expectedViewableVersions.add(new Version(3, 1));
    expectedViewableVersions.add(new Version(3, 2));
    assretVersionInfoEntity(versionInfoEntity, TYPE1, ID1, new Version(3, 2), new Version(3, 3),
        USR2, VersionStatus.Locked, expectedViewableVersions, new Version(3, 0));
  }

//  @Test(dependsOnMethods = "viewableVersionsTest")
  public void listActiveVersionsTest() {
    versioningManager.create(TYPE1, ID2, USR3);
    versioningManager.checkin(TYPE1, ID2, USR3, "check in 0.1");

    versioningManager.create(TYPE2, ID3, USR3);
    versioningManager.checkin(TYPE2, ID3, USR3, "check in 0.1");

    Map<String, VersionInfo> idToVersionInfo =
        versioningManager.listEntitiesVersionInfo(TYPE1, USR2, VersionableEntityAction.Read);
    Assert.assertEquals(idToVersionInfo.size(), 2);
    Assert.assertEquals(idToVersionInfo.get(ID1).getActiveVersion(), new Version(3, 3));
    Assert.assertEquals(idToVersionInfo.get(ID2).getActiveVersion(), VERSION01);
  }

//  @Test(dependsOnMethods = "listActiveVersionsTest")
  public void deleteTest() {
    versioningManager.checkin(TYPE1, ID1, USR2, "check in for delete");
    versioningManager.delete(TYPE1, ID1, USR1);

    VersionInfoDeletedEntity versionInfoDeletedEntity =
        versionInfoDeletedDao.get(new VersionInfoDeletedEntity(TYPE1, ID1));
    Assert.assertNotNull(versionInfoDeletedEntity);

    Map<String, VersionInfo> entitiesInfoMap =
        versioningManager.listDeletedEntitiesVersionInfo(TYPE1, USR2, null);
    Assert.assertEquals(entitiesInfoMap.size(), 1);
    VersionInfoEntity versionInfoEntity = versionInfoDao.get(new VersionInfoEntity(TYPE1, ID1));
    Assert.assertNull(versionInfoEntity);
    versioningManager.undoDelete(TYPE1, ID1, USR1);
    versionInfoEntity = versionInfoDao.get(new VersionInfoEntity(TYPE1, ID1));
    Assert.assertNotNull(versionInfoEntity);


  }

  private void createVersionableEntityRecord(String tableName, String idName, String versionName,
                                             String id, Version version) {
    noSqlDb.execute(
        String.format("insert into %s (%s,%s) values (?,?)", tableName, idName, versionName), id,
        versionMapper.toUDT(version));
  }

  private ResultSet loadVersionableEntityRecord(String tableName, String idName, String versionName,
                                                String id, Version version) {
    return noSqlDb.execute(
        String.format("select * from %s where %s=? and %s=?", tableName, idName, versionName), id,
        versionMapper.toUDT(version));
  }
}
