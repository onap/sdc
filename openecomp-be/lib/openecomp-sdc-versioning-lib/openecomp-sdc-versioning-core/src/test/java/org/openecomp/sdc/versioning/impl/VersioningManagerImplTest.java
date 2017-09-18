/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.versioning.impl;


import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.versioning.dao.VersionInfoDao;
import org.openecomp.sdc.versioning.dao.VersionInfoDeletedDao;
import org.openecomp.sdc.versioning.dao.types.UserCandidateVersion;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionInfoDeletedEntity;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VersioningManagerImplTest {
  private static final String USR1 = "usr1";
  private static final String USR2 = "usr2";
  private static final String TYPE1 = "Type1";
/*  private static final String TYPE2 = "Type2";*/
  private static final String ID1 = "Id1";
/*  private static final String ID2 = "Id2";
  private static final String ID3 = "Id3";
  private static final String TYPE1_TABLE_NAME = "vendor_license_model";
  private static final String TYPE1_ID_NAME = "vlm_id";
  private static final String TYPE1_VERSION_NAME = "version";
  private static final String TYPE2_TABLE_NAME = "feature_group";
  private static final String TYPE2_ID_NAME = "vlm_id";
  private static final String TYPE2_VERSION_NAME = "version";*/
  private static final Version VERSION0 = new Version(0, 0);
  private static final Version VERSION01 = new Version(0, 1);
  private static final Version VERSION02 = new Version(0, 2);
  private static final Version VERSION10 = new Version(1, 0);
  private static final Version VERSION11 = new Version(1, 1);

  /*  private static final NoSqlDb noSqlDb = NoSqlDbFactory.getInstance().createInterface();

    private static UDTMapper<Version> versionMapper =
        noSqlDb.getMappingManager().udtMapper(Version.class);*/
  @Mock
  private VersionInfoDao versionInfoDaoMock;
  @Mock
  private VersionInfoDeletedDao versionInfoDeletedDaoMock;
  @InjectMocks
  private VersioningManagerImpl versioningManager;

  @Captor
  private ArgumentCaptor<VersionInfoEntity> versionInfoEntityArg;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  /*  @BeforeClass
    private void init() {
      versionInfoDaoMock.delete(new VersionInfoEntity(TYPE1, ID1));
      versionInfoDaoMock.delete(new VersionInfoEntity(TYPE1, ID2));
      versionInfoDaoMock.delete(new VersionInfoEntity(TYPE2, ID3));
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
    }*/

/*  @Test
  public void testRegister() throws Exception {
    VersionableEntityMetadata entityMetadata =
        new VersionableEntityMetadata(TYPE1_TABLE_NAME, TYPE1_ID_NAME, TYPE1_VERSION_NAME);
    versioningManager.register(TYPE1, entityMetadata);

    Map<String, Set<VersionableEntityMetadata>> versionableEntities =
        versionableEntitiesCapture.capture();
    Set<VersionableEntityMetadata> type1Entities = versionableEntities.get(TYPE1);
    Assert.assertNotNull(type1Entities);
    Assert.assertTrue(type1Entities.contains(entityMetadata));
  }*/

  @Test(expectedExceptions = CoreException.class)
  public void testCreateAlreadyExisting() {
    doReturn(new VersionInfoEntity()).when(versionInfoDaoMock).get(anyObject());
    versioningManager.create(TYPE1, ID1, USR1);
  }

  @Test
  public void testCreate() {
    Version version = versioningManager.create(TYPE1, ID1, USR1);
    Assert.assertEquals(version, VERSION01);

/*    createVersionableEntityRecord(TYPE1_TABLE_NAME, TYPE1_ID_NAME, TYPE1_VERSION_NAME, ID1,
        version);*/
    verify(versionInfoDaoMock).create(versionInfoEntityArg.capture());
    VersionInfoEntity versionInfoEntity = versionInfoEntityArg.getValue();
    assretVersionInfoEntity(versionInfoEntity, TYPE1, ID1, new Version(0, 0), VERSION01, USR1,
        VersionStatus.Locked, new HashSet<>(), null);
  }

  @Test(expectedExceptions = CoreException.class)
  public void testDeleteNonExisting() {
    versioningManager.delete(TYPE1, ID1, USR1);
  }

  @Test(expectedExceptions = CoreException.class)
  public void testDeleteLocked() {
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Locked, VERSION0,
        new UserCandidateVersion(USR1, VERSION01), Collections.emptySet(), null);
    versioningManager.delete(TYPE1, ID1, USR1);
  }

  @Test
  public void testDelete() {
    VersionInfoEntity versionInfoEntity = new VersionInfoEntity();
    versionInfoEntity.setStatus(VersionStatus.Available);
    doReturn(versionInfoEntity).when(versionInfoDaoMock).get(anyObject());

    versioningManager.delete(TYPE1, ID1, USR1);

    verify(versionInfoDaoMock).delete(versionInfoEntity);
    ArgumentCaptor<VersionInfoDeletedEntity> versionInfoDeletedEntityArg =
        ArgumentCaptor.forClass(VersionInfoDeletedEntity.class);
    verify(versionInfoDeletedDaoMock).create(versionInfoDeletedEntityArg.capture());
  }

  @Test(expectedExceptions = CoreException.class)
  public void testUndoDeleteNonExisting() {
    versioningManager.undoDelete(TYPE1, ID1, USR1);
  }

  @Test
  public void testUndoDelete() {
    VersionInfoDeletedEntity versionInfoDeletedEntity = new VersionInfoDeletedEntity();
    versionInfoDeletedEntity.setStatus(VersionStatus.Available);
    doReturn(versionInfoDeletedEntity).when(versionInfoDeletedDaoMock).get(anyObject());

    versioningManager.undoDelete(TYPE1, ID1, USR1);

    verify(versionInfoDeletedDaoMock).delete(versionInfoDeletedEntity);
    verify(versionInfoDaoMock).create(versionInfoEntityArg.capture());
/*
    VersionInfoDeletedEntity versionInfoDeletedEntity =
        versionInfoDeletedDaoMock.get(new VersionInfoDeletedEntity(TYPE1, ID1));
    Assert.assertNotNull(versionInfoDeletedEntity);

    Map<String, VersionInfo> entitiesInfoMap =
        versioningManager.listDeletedEntitiesVersionInfo(TYPE1, USR2, null);
    Assert.assertEquals(entitiesInfoMap.size(), 1);
    VersionInfoEntity versionInfoEntity = versionInfoDaoMock.get(new VersionInfoEntity(TYPE1, ID1));
    Assert.assertNull(versionInfoEntity);
    versioningManager.undoDelete(TYPE1, ID1, USR1);
    versionInfoEntity = versionInfoDaoMock.get(new VersionInfoEntity(TYPE1, ID1));
    Assert.assertNotNull(versionInfoEntity);*/
  }

  @Test(expectedExceptions = CoreException.class)
  public void testCheckoutNonExisting() {
    versioningManager.checkout(TYPE1, ID1, USR2);
  }

  @Test(expectedExceptions = CoreException.class)
  public void testCheckoutOnLockedSameUser() {
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Locked, VERSION0,
        new UserCandidateVersion(USR1, VERSION01), Collections.emptySet(), null);
    versioningManager.checkout(TYPE1, ID1, USR1);
  }

  @Test(expectedExceptions = CoreException.class)
  public void testCheckoutOnLockedOtherUser() {
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Locked, VERSION0,
        new UserCandidateVersion(USR2, VERSION01), Collections.emptySet(), null);
    versioningManager.checkout(TYPE1, ID1, USR1);
  }

  @Test
  public void testCheckoutOnFinalized() {
    Set<Version> viewableVersions = new HashSet<>();
    viewableVersions.add(VERSION10);
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Final, VERSION10, null, viewableVersions,
        VERSION10);

    Version version = versioningManager.checkout(TYPE1, ID1, USR1);
    Assert.assertEquals(version, VERSION11);

    VersionInfoEntity versionInfoEntity = versionInfoDaoMock.get(new VersionInfoEntity(TYPE1, ID1));
    assretVersionInfoEntity(versionInfoEntity, TYPE1, ID1, VERSION10, VERSION11, USR1,
        VersionStatus.Locked, viewableVersions, VERSION10);
/*
    ResultSet results =
        loadVersionableEntityRecord(TYPE1_TABLE_NAME, TYPE1_ID_NAME, TYPE1_VERSION_NAME, ID1,
            VERSION11);
    Assert.assertTrue(results.iterator().hasNext());*/
  }

  @Test
  public void testCheckout() {
    Set<Version> viewableVersions = new HashSet<>();
    viewableVersions.add(VERSION01);
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Available, VERSION01, null, viewableVersions,
        null);

    Version version = versioningManager.checkout(TYPE1, ID1, USR1);
    Assert.assertEquals(version, VERSION02);

    verify(versionInfoDaoMock).update(versionInfoEntityArg.capture());
    VersionInfoEntity versionInfoEntity = versionInfoEntityArg.getValue();

    assretVersionInfoEntity(versionInfoEntity, TYPE1, ID1, VERSION01, VERSION02, USR1,
        VersionStatus.Locked, viewableVersions, null);

/*    ResultSet results =
        loadVersionableEntityRecord(TYPE1_TABLE_NAME, TYPE1_ID_NAME, TYPE1_VERSION_NAME, ID1,
            VERSION02);
    Assert.assertTrue(results.iterator().hasNext());*/
  }

  @Test(expectedExceptions = CoreException.class)
  public void testUndoCheckoutNonExisting() {
    versioningManager.undoCheckout(TYPE1, ID1, USR1);
  }

  @Test(expectedExceptions = CoreException.class)
  public void testUndoCheckoutOnAvailable() {
    Set<Version> viewableVersions = new HashSet<>();
    viewableVersions.add(VERSION01);
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Available, VERSION01, null, viewableVersions,
        null);

    versioningManager.undoCheckout(TYPE1, ID1, USR1);
  }

  @Test(expectedExceptions = CoreException.class)
  public void testUndoCheckouOnFinalized() {
    Set<Version> viewableVersions = new HashSet<>();
    viewableVersions.add(VERSION10);
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Final, VERSION10, null, viewableVersions,
        VERSION10);
    versioningManager.undoCheckout(TYPE1, ID1, USR2);
  }

  @Test(expectedExceptions = CoreException.class)
  public void testUndoCheckoutOnLockedOtherUser() {
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Locked, VERSION0,
        new UserCandidateVersion(USR2, VERSION01), Collections.emptySet(), null);

    versioningManager.undoCheckout(TYPE1, ID1, USR1);
  }

  @Test
  public void testUndoCheckout() {
    HashSet<Version> viewableVersions = new HashSet<>();
    viewableVersions.add(VERSION01);
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Locked, VERSION01,
        new UserCandidateVersion(USR1, VERSION02), viewableVersions, null);

    Version version = versioningManager.undoCheckout(TYPE1, ID1, USR1);
    Assert.assertEquals(version, VERSION01);

    VersionInfoEntity versionInfoEntity = versionInfoDaoMock.get(new VersionInfoEntity(TYPE1, ID1));
    assretVersionInfoEntity(versionInfoEntity, TYPE1, ID1, VERSION01, null, null,
        VersionStatus.Available, viewableVersions, null);

/*    ResultSet results =
        loadVersionableEntityRecord(TYPE1_TABLE_NAME, TYPE1_ID_NAME, TYPE1_VERSION_NAME, ID1,
            VERSION02);
    Assert.assertFalse(results.iterator().hasNext());*/
  }

  @Test(expectedExceptions = CoreException.class)
  public void testCheckinNonExisting() {
    versioningManager.checkin(TYPE1, ID1, USR1, "");
  }

  @Test(expectedExceptions = CoreException.class)
  public void testCheckinOnAvailable() {
    Set<Version> viewableVersions = new HashSet<>();
    viewableVersions.add(VERSION01);
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Available, VERSION01, null, viewableVersions,
        null);

    versioningManager.checkin(TYPE1, ID1, USR1, "fail checkin");
  }


  @Test(expectedExceptions = CoreException.class)
  public void testCheckinOnFinalized() {
    Set<Version> viewableVersions = new HashSet<>();
    viewableVersions.add(VERSION10);
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Final, VERSION10, null, viewableVersions,
        VERSION10);

    versioningManager.checkin(TYPE1, ID1, USR1, "failed checkin");
  }

  @Test(expectedExceptions = CoreException.class)
  public void testCheckinOnLockedOtherUser() {
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Locked, VERSION0,
        new UserCandidateVersion(USR2, VERSION01), Collections.emptySet(), null);

    versioningManager.checkin(TYPE1, ID1, USR1, "");
  }

  @Test
  public void testCheckin() {
    HashSet<Version> viewableVersions = new HashSet<>();
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Locked, VERSION0,
        new UserCandidateVersion(USR1, VERSION01), viewableVersions, null);

    Version version = versioningManager.checkin(TYPE1, ID1, USR1, "checkin 0.1");
    Assert.assertEquals(version, VERSION01);

    verify(versionInfoDaoMock).update(versionInfoEntityArg.capture());
    VersionInfoEntity versionInfoEntity = versionInfoEntityArg.getValue();

    viewableVersions.add(VERSION01);
    assretVersionInfoEntity(versionInfoEntity, TYPE1, ID1, VERSION01, null, null,
        VersionStatus.Available, viewableVersions, null);
  }

  @Test(expectedExceptions = CoreException.class)
  public void testSubmitNonExisting() {
    versioningManager.submit(TYPE1, ID1, USR2, "failed submit");
  }

  @Test(expectedExceptions = CoreException.class)
  public void testSubmitOnLocked() {
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Locked, VERSION0,
        new UserCandidateVersion(USR1, VERSION01), Collections.emptySet(), null);
    versioningManager.submit(TYPE1, ID1, USR2, "failed submit");
  }


  @Test(expectedExceptions = CoreException.class)
  public void testSubmitOnFinalized() {
    Set<Version> viewableVersions = new HashSet<>();
    viewableVersions.add(VERSION10);
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Final, VERSION10, null, viewableVersions,
        VERSION10);
    versioningManager.submit(TYPE1, ID1, USR2, "failed submit");
  }

  @Test
  public void testSubmit() {
    Version version32 = new Version(3, 2);
    Version version40 = new Version(4, 0);

    Set<Version> viewableVersions = new HashSet<>();
    viewableVersions.add(VERSION10);
    viewableVersions.add(new Version(2, 0));
    viewableVersions.add(new Version(3, 0));
    viewableVersions.add(new Version(3, 1));
    viewableVersions.add(version32);
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Available, version32, null, viewableVersions,
        new Version(3, 0));

    Version version = versioningManager.submit(TYPE1, ID1, USR1, "submit msg");
    Assert.assertEquals(version, version40);
    viewableVersions.remove(new Version(3, 1));
    viewableVersions.remove(version32);
    viewableVersions.add(version40);

    verify(versionInfoDaoMock).update(versionInfoEntityArg.capture());
    VersionInfoEntity versionInfoEntity = versionInfoEntityArg.getValue();

    assretVersionInfoEntity(versionInfoEntity, TYPE1, ID1, version40, null, null,
        VersionStatus.Final, viewableVersions, version40);

/*    ResultSet results =
        loadVersionableEntityRecord(TYPE1_TABLE_NAME, TYPE1_ID_NAME, TYPE1_VERSION_NAME, ID1,
            VERSION10);
    Assert.assertTrue(results.iterator().hasNext());*/
  }

  @Test(expectedExceptions = CoreException.class)
  public void testGetVersionInfoOnNonExistingEntity() {
    versioningManager.getEntityVersionInfo(TYPE1, ID1, USR1, VersionableEntityAction.Read);
  }

  @Test
  public void testGetVersionInfoForReadOnAvailable() {
    Set<Version> viewableVersions = new HashSet<>();
    viewableVersions.add(VERSION01);
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Available, VERSION01, null, viewableVersions,
        null);

    VersionInfo versionInfo =
        versioningManager.getEntityVersionInfo(TYPE1, ID1, USR1, VersionableEntityAction.Read);
    assertVersionInfo(versionInfo, VERSION01, VersionStatus.Available, null,
        viewableVersions, null);
  }

  @Test(expectedExceptions = CoreException.class)
  public void testGetVersionInfoForWriteOnAvailable() {
    Set<Version> viewableVersions = new HashSet<>();
    viewableVersions.add(VERSION01);
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Available, VERSION01, null, viewableVersions,
        null);

    versioningManager.getEntityVersionInfo(TYPE1, ID1, USR1, VersionableEntityAction.Write);
  }

  @Test
  public void testGetVersionInfoForReadOnLockedSameUser() {
    Set<Version> viewableVersions = new HashSet<>();
    viewableVersions.add(VERSION01);
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Locked, VERSION01,
        new UserCandidateVersion(USR1, VERSION02), viewableVersions, null);

    VersionInfo versionInfo =
        versioningManager.getEntityVersionInfo(TYPE1, ID1, USR1, VersionableEntityAction.Read);
    viewableVersions.add(VERSION02);
    assertVersionInfo(versionInfo, VERSION02, VersionStatus.Locked, USR1, viewableVersions, null);
  }

  @Test
  public void testGetVersionInfoForReadOnLockedOtherUser() {
    Set<Version> viewableVersions = new HashSet<>();
    viewableVersions.add(VERSION01);
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Locked, VERSION01,
        new UserCandidateVersion(USR2, VERSION02), viewableVersions, null);

    VersionInfo versionInfo =
        versioningManager.getEntityVersionInfo(TYPE1, ID1, USR1, VersionableEntityAction.Read);
    Assert.assertEquals(versionInfo.getActiveVersion(), VERSION01);
    assertVersionInfo(versionInfo, VERSION01, VersionStatus.Locked, USR2, viewableVersions, null);
  }

  @Test(expectedExceptions = CoreException.class)
  public void testGetVersionInfoForWriteOnLockedOtherUser() {
    Set<Version> viewableVersions = new HashSet<>();
    viewableVersions.add(VERSION01);
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Locked, VERSION01,
        new UserCandidateVersion(USR2, VERSION02), viewableVersions, null);

    versioningManager.getEntityVersionInfo(TYPE1, ID1, USR1, VersionableEntityAction.Write);
  }

  @Test
  public void testGetVersionInfoForWriteOnLockedSameUser() {
    Set<Version> viewableVersions = new HashSet<>();
    viewableVersions.add(VERSION01);
    mockVersionInfoEntity(TYPE1, ID1, VersionStatus.Locked, VERSION01,
        new UserCandidateVersion(USR1, VERSION02), viewableVersions, null);

    VersionInfo versionInfo =
        versioningManager.getEntityVersionInfo(TYPE1, ID1, USR1, VersionableEntityAction.Write);
    viewableVersions.add(VERSION02);
    assertVersionInfo(versionInfo, VERSION02, VersionStatus.Locked, USR1, viewableVersions, null);
  }

/*  private void createVersionableEntityRecord(String tableName, String idName, String versionName,
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
  }*/


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

  private static void assertVersionInfo(VersionInfo actual, Version activeVersion,
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

  private VersionInfoEntity mockVersionInfoEntity(String entityType, String entityId,
                                                  VersionStatus status, Version activeVersion,
                                                  UserCandidateVersion candidate,
                                                  Set<Version> viewableVersions,
                                                  Version latestFinalVersion) {
    VersionInfoEntity mock = new VersionInfoEntity();
    mock.setEntityType(entityType);
    mock.setEntityId(entityId);
    mock.setStatus(status);
    mock.setActiveVersion(activeVersion);
    mock.setCandidate(candidate);
    mock.setViewableVersions(viewableVersions);
    mock.setLatestFinalVersion(latestFinalVersion);

    doReturn(mock).when(versionInfoDaoMock).get(anyObject());
    return mock;
  }
}
