/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 Samsung Intellectual Property. All rights reserved.
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


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.versioning.ActionVersioningManager;
import org.openecomp.sdc.versioning.AsdcItemManager;
import org.openecomp.sdc.versioning.VersionCalculator;
import org.openecomp.sdc.versioning.dao.VersionDao;
import org.openecomp.sdc.versioning.dao.VersionInfoDao;
import org.openecomp.sdc.versioning.dao.VersionInfoDeletedDao;
import org.openecomp.sdc.versioning.dao.types.UserCandidateVersion;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionInfoDeletedEntity;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.VersionCreationMethod;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;

@RunWith(MockitoJUnitRunner.class)
public class ActionVersioningManagerImplTest {

    @Mock
    private VersionInfoDao versionInfoDao;
    @Mock
    private VersionInfoDeletedDao versionInfoDeletedDao;
    @Mock
    private VersionDao versionDao;
    @Mock
    private VersionCalculator versionCalculator;
    @Mock
    private AsdcItemManager asdcItemManager;

    private ActionVersioningManager actionVersioningManager;
    private VersionInfoEntity versionInfoEntity;

    @Before
    public void setUp() {
        actionVersioningManager = createSUT();

        versionInfoEntity = new VersionInfoEntity();
        versionInfoEntity.setActiveVersion(new Version());
        versionInfoEntity.setStatus(VersionStatus.Draft);
        versionInfoEntity.setCandidate(new UserCandidateVersion("mock-user", new Version()));
    }

    private ActionVersioningManager createSUT() {
        return new ActionVersioningManagerImpl(
            versionInfoDao,
            versionInfoDeletedDao,
            versionDao,
            versionCalculator,
            asdcItemManager
        );
    }

    @Test
    public void testCtor() {
        assertThat(actionVersioningManager, isA(ActionVersioningManager.class));
    }

    @Test
    public void testlistDeletedEntitiesVersionInfo() {
        when(versionInfoDeletedDao.list(any(VersionInfoDeletedEntity.class))).thenReturn(new ArrayList<>());

        Map<String, VersionInfo> result = actionVersioningManager.listDeletedEntitiesVersionInfo(
            "mock-type",
            "mock-user",
            VersionableEntityAction.Read
        );

        assertThat(result, notNullValue());
    }

    @Test
    public void testList() {
        when(versionDao.list(anyString())).thenReturn(new ArrayList<>());

        List<Version> result = actionVersioningManager.list("mock-id");

        assertThat(result, notNullValue());
    }

    @Test
    public void testGet() {
        when(versionDao.get(anyString(), any(Version.class))).thenReturn(Optional.of(new Version()));

        Version result = actionVersioningManager.get("mock-id", new Version());

        assertThat(result, isA(Version.class));
    }

    @Test
    public void testListEntitiesVersionInfo() {
        when(versionInfoDao.list(any(VersionInfoEntity.class))).thenReturn(new ArrayList<>());

        Map<String, VersionInfo> result = actionVersioningManager.listEntitiesVersionInfo(
            "mock-type",
            "mock-user",
            VersionableEntityAction.Read
        );

        assertThat(result, notNullValue());
    }

    @Test
    public void testGetEntityVersionInfo() {
        when(versionInfoDao.get(any(VersionInfoEntity.class))).thenReturn(versionInfoEntity);

        VersionInfo result = actionVersioningManager.getEntityVersionInfo(
            "mock-type",
            "mock-id",
            "mock-user",
            VersionableEntityAction.Read
        );

        assertThat(result, notNullValue());
    }

    @Test
    public void testCreate() {
        when(versionInfoDao.get(any(VersionInfoEntity.class))).thenReturn(null);

        Version result = actionVersioningManager.create(
            "mock-type",
            "mock-id",
            "mock-user"
        );

        assertThat(result, notNullValue());
    }

    @Test(expected = CoreException.class)
    public void testCreateFailed() {
        when(versionInfoDao.get(any(VersionInfoEntity.class))).thenReturn(new VersionInfoEntity());

        actionVersioningManager.create(
            "mock-type",
            "mock-id",
            "mock-user"
        );

        fail("Should throw CoreException");
    }

    @Test
    public void testCreateAlt() {
        Version result = actionVersioningManager.create(
            "mock-type",
            new Version(),
            VersionCreationMethod.minor
        );

        assertThat(result, notNullValue());
    }

    @Test
    public void testRegister() {
        final VersionableEntityMetadata entityMetadata = new VersionableEntityMetadata(
            "mock-name",
            "mock-id-name",
            "mock-ver-id-name"
        );
        Map<String, Set<VersionableEntityMetadata>> map = new HashMap<>();
        var action = new ActionVersioningManagerImpl(map);
        action.register("mock-type", entityMetadata);
        assertThat(map, notNullValue());
        assertThat(map.size(), is(1));
    }

    @Test
    public void testDelete() {
        versionInfoEntity.setStatus(VersionStatus.Certified);

        when(versionInfoDao.get(any(VersionInfoEntity.class))).thenReturn(versionInfoEntity);

        actionVersioningManager.delete(
            "moct-type",
            "mock-id",
            "mock-user"
        );

        verify(versionInfoDeletedDao).create(any(VersionInfoDeletedEntity.class));
        verify(versionInfoDao).delete(any(VersionInfoEntity.class));
    }

    @Test(expected = CoreException.class)
    public void testDeleteLocked() {
        versionInfoEntity.setStatus(VersionStatus.Locked);
        UserCandidateVersion userCandidateVersion = new UserCandidateVersion("mock-user", new Version());
        versionInfoEntity.setCandidate(userCandidateVersion);

        when(versionInfoDao.get(any(VersionInfoEntity.class))).thenReturn(versionInfoEntity);

        actionVersioningManager.delete(
            "moct-type",
            "mock-id",
            "mock-user"
        );
        fail("Should throw CoreException");
    }

    @Test
    public void testUndoDelete() {
        when(versionInfoDeletedDao.get(any(VersionInfoDeletedEntity.class))).thenReturn(new VersionInfoDeletedEntity());

        actionVersioningManager.undoDelete(
            "mock-type",
            "mock-id",
            "mock-user"
        );

        verify(versionInfoDao).create(any(VersionInfoEntity.class));
        verify(versionInfoDeletedDao).delete(any(VersionInfoDeletedEntity.class));
    }

    @Test(expected = CoreException.class)
    public void testUndoDeleteFail() {
        when(versionInfoDeletedDao.get(any(VersionInfoDeletedEntity.class))).thenReturn(null);

        actionVersioningManager.undoDelete(
            "mock-type",
            "mock-id",
            "mock-user"
        );

        fail("Should throw CoreException");
    }

    @Test
    public void testCheckout() {
        versionInfoEntity.setStatus(VersionStatus.Certified);

        when(versionInfoDao.get(any(VersionInfoEntity.class))).thenReturn(versionInfoEntity);

        Version result = actionVersioningManager.checkout(
            "moct-type",
            "mock-id",
            "mock-user"
        );

        assertThat(result, notNullValue());
        assertThat(result.getStatus(), is(VersionStatus.Draft));
    }

    @Test(expected = CoreException.class)
    public void testCheckoutFailNotFound() {
        when(versionInfoDao.get(any(VersionInfoEntity.class))).thenReturn(null);

        actionVersioningManager.checkout(
            "moct-type",
            "mock-id",
            "mock-user"
        );

        fail("Should throw CoreException");
    }

    @Test(expected = CoreException.class)
    public void testCheckoutLockedFail() {
        versionInfoEntity.setStatus(VersionStatus.Locked);

        when(versionInfoDao.get(any(VersionInfoEntity.class))).thenReturn(versionInfoEntity);

        actionVersioningManager.checkout(
            "mock-type",
            "mock-id",
            "mock-user"
        );

        fail("Should throw CoreException");
    }

    @Test
    public void testUndoCheckout() {
        versionInfoEntity.setStatus(VersionStatus.Locked);

        when(versionInfoDao.get(any(VersionInfoEntity.class))).thenReturn(versionInfoEntity);

        Version result = actionVersioningManager.undoCheckout(
            "mock-type",
            "mock-id",
            "mock-user"
        );
        assertThat(result, notNullValue(Version.class));
    }

    @Test
    public void testCheckin() {
        versionInfoEntity.setStatus(VersionStatus.Locked);

        when(versionInfoDao.get(any(VersionInfoEntity.class))).thenReturn(versionInfoEntity);

        Version result = actionVersioningManager.checkin(
            "mock-type",
            "mock-id",
            "mock-user",
            "mock-desc"
        );
        assertThat(result, notNullValue(Version.class));
    }

    @Test(expected = CoreException.class)
    public void testCheckinDraft() {
        versionInfoEntity.setStatus(VersionStatus.Draft);

        when(versionInfoDao.get(any(VersionInfoEntity.class))).thenReturn(versionInfoEntity);

        Version result = actionVersioningManager.checkin(
            "mock-type",
            "mock-id",
            "mock-user",
            "mock-desc"
        );
        assertThat(result, notNullValue(Version.class));
    }

    @Test
    public void testForceSync() {
        actionVersioningManager.forceSync("mock-id", new Version());
    }

    @Test
    public void testSubmit() {
        when(versionDao.get(anyString(), any(Version.class))).thenReturn(Optional.of(new Version()));

        actionVersioningManager.submit(
            "mock-type",
            new Version(),
            "mock-desc"
        );
        verify(versionDao).update(anyString(), any(Version.class));
        verify(asdcItemManager).updateVersionStatus(anyString(), any(VersionStatus.class), any(VersionStatus.class));
    }

    @Test
    public void testSubmitAlt() {
        versionInfoEntity.setStatus(VersionStatus.Draft);
        when(versionInfoDao.get(any(VersionInfoEntity.class))).thenReturn(versionInfoEntity);

        Version result = actionVersioningManager.submit(
            "mock-type",
            "mock-id",
            "mock-user",
            "mock-desc"
        );
        assertThat(result, notNullValue(Version.class));
    }

    @Test(expected = CoreException.class)
    public void testSubmitAltFailNotFound() {
        when(versionInfoDao.get(any(VersionInfoEntity.class))).thenReturn(null);

        actionVersioningManager.submit(
            "mock-type",
            "mock-id",
            "mock-user",
            "mock-desc"
        );

        fail("Should throw CoreException");
    }

    @Test(expected = CoreException.class)
    public void testSubmitAltFailCertified() {
        versionInfoEntity.setStatus(VersionStatus.Certified);
        when(versionInfoDao.get(any(VersionInfoEntity.class))).thenReturn(versionInfoEntity);

        actionVersioningManager.submit(
            "mock-type",
            "mock-id",
            "mock-user",
            "mock-desc"
        );

        fail("Should throw CoreException");
    }

    @Test(expected = CoreException.class)
    public void testSubmitAltFailLocked() {
        versionInfoEntity.setStatus(VersionStatus.Locked);
        when(versionInfoDao.get(any(VersionInfoEntity.class))).thenReturn(versionInfoEntity);

        actionVersioningManager.submit(
            "mock-type",
            "mock-id",
            "mock-user",
            "mock-desc"
        );

        fail("Should throw CoreException");
    }

    @Test
    public void testSync() {
        actionVersioningManager.sync("mock-id", new Version());
        verify(versionDao).sync(anyString(), any(Version.class));
    }

    @Test
    public void testRevert() {
        actionVersioningManager.revert("mock-id", new Version(), "mock-rev-id");
        verify(versionDao).revert(anyString(), any(Version.class), anyString());
    }

    @Test
    public void testListRevisions() {
        actionVersioningManager.listRevisions("mock-id", new Version());
        verify(versionDao).listRevisions(anyString(), any(Version.class));
    }
}
