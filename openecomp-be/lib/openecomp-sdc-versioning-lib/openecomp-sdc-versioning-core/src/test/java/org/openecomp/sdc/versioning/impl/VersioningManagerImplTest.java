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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.openecomp.sdc.versioning.dao.types.SynchronizationState.OutOfSync;
import static org.openecomp.sdc.versioning.dao.types.SynchronizationState.UpToDate;
import static org.openecomp.sdc.versioning.dao.types.VersionStatus.Certified;
import static org.openecomp.sdc.versioning.dao.types.VersionStatus.Draft;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.versioning.ItemManager;
import org.openecomp.sdc.versioning.VersionCalculator;
import org.openecomp.sdc.versioning.dao.VersionDao;
import org.openecomp.sdc.versioning.dao.types.Revision;
import org.openecomp.sdc.versioning.dao.types.SynchronizationState;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionState;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.VersionCreationMethod;

public class VersioningManagerImplTest {

    private static final String ITEM_ID = "itemId";
    private static final String VERSION_ID = "versionId";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private VersionDao versionDaoMock;
    @Mock
    private VersionCalculator versionCalculatorMock;
    @Mock
    private ItemManager asdcItemManager;
    @InjectMocks
    private VersioningManagerImpl versioningManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testListWhenNone() {
        doReturn(new ArrayList<>()).when(versionDaoMock).list(ITEM_ID);

        List<Version> versions = versioningManager.list(ITEM_ID);

        Assert.assertTrue(versions.isEmpty());
    }

    @Test
    public void testList() {
        List<Version> returnedVersions = Stream.of(createVersion("1", null, null, false),
            createVersion("2", null, null, false),
            createVersion("3", null, null, false)).collect(Collectors.toList());
        doReturn(returnedVersions).when(versionDaoMock).list(ITEM_ID);

        List<Version> versions = versioningManager.list(ITEM_ID);
        Assert.assertEquals(versions, returnedVersions);
    }

    @Test(expected = Exception.class)
    public void testGetNonExisting() throws Exception {
        Version version = new Version(VERSION_ID);

        doReturn(Optional.empty()).when(versionDaoMock).get(ITEM_ID, version);
        doThrow(new Exception()).when(versionDaoMock).sync(ITEM_ID, version);

        versioningManager.get(ITEM_ID, version);
    }

    @Test
    public void testGetNonExistingForUser() {
        Version requestedVersion = new Version(VERSION_ID);

        Version returnedVersion = createVersion(VERSION_ID, Draft, UpToDate, false);
        doReturn(Optional.empty()).doReturn(Optional.of(returnedVersion))
            .when(versionDaoMock).get(ITEM_ID, requestedVersion);

        Version version = versioningManager.get(ITEM_ID, requestedVersion);
        Assert.assertEquals(version, returnedVersion);

        verify(versionDaoMock, times(2)).get(ITEM_ID, requestedVersion);
        verify(versionDaoMock).sync(ITEM_ID, requestedVersion);
    }

    @Test
    public void testGetOutOfSyncCertified() {
        Version requestedVersion = new Version(VERSION_ID);

        Version returnedVersion = createVersion(VERSION_ID, Certified, UpToDate, false);
        doReturn(Optional.of(createVersion(VERSION_ID, Certified, OutOfSync, false)))
            .doReturn(Optional.of(returnedVersion))
            .when(versionDaoMock).get(ITEM_ID, requestedVersion);

        Version version = versioningManager.get(ITEM_ID, requestedVersion);
        Assert.assertEquals(version, returnedVersion);

        verify(versionDaoMock, times(2)).get(ITEM_ID, requestedVersion);
        verify(versionDaoMock).forceSync(ITEM_ID, requestedVersion);
    }

    @Test
    public void testGet() {
        Version requestedVersion = new Version(VERSION_ID);

        Version returnedVersion = createVersion(VERSION_ID, Draft, OutOfSync, true);
        doReturn(Optional.of(returnedVersion)).when(versionDaoMock).get(ITEM_ID, requestedVersion);

        Version version = versioningManager.get(ITEM_ID, requestedVersion);
        Assert.assertEquals(version, returnedVersion);

        verify(versionDaoMock).get(ITEM_ID, requestedVersion);
        verify(versionDaoMock, never()).sync(any(), any());
        verify(versionDaoMock, never()).forceSync(any(), any());
    }

    @Test
    public void testCreate() {
        Version requestedVersion = new Version();

        String versionName = "versionName";
        doReturn(versionName).when(versionCalculatorMock).calculate(null, VersionCreationMethod.major);

        doReturn(Stream.of(createVersion("1", null, null, false),
            createVersion("2", null, null, false),
            createVersion("3", null, null, false)).collect(Collectors.toList()))
            .when(versionDaoMock).list(ITEM_ID);

        Version version =
            versioningManager.create(ITEM_ID, requestedVersion, VersionCreationMethod.major);
        Assert.assertNotNull(version);
        Assert.assertEquals(version.getName(), versionName);

        verify(versionDaoMock).create(ITEM_ID, requestedVersion);
        verify(asdcItemManager).updateVersionStatus(ITEM_ID, Draft, null);
        verify(versionDaoMock)
            .publish(eq(ITEM_ID), eq(requestedVersion), eq("Create version: versionName"));
    }

    @Test
    public void testCreateBasedOn() {
        Version requestedVersion = new Version();
        requestedVersion.setBaseId("baseVersionId");

        Version baseVersion = createVersion(requestedVersion.getBaseId(), Certified, UpToDate, false);
        // TODO: 12/13/2017 fix to eq(new Version("baseVersionId")) when version.equals will be fixed
        doReturn(Optional.of(baseVersion)).when(versionDaoMock).get(eq(ITEM_ID), any(Version.class));

        String versionName = "4.0";
        doReturn(versionName)
            .when(versionCalculatorMock).calculate(baseVersion.getName(), VersionCreationMethod.major);

        doReturn(Stream.of(createVersion("1", null, null, false),
            createVersion("2", null, null, false),
            createVersion("3", null, null, false)).collect(Collectors.toList()))
            .when(versionDaoMock).list(ITEM_ID);

        Version version =
            versioningManager.create(ITEM_ID, requestedVersion, VersionCreationMethod.major);
        Assert.assertNotNull(version);
        Assert.assertEquals(version.getName(), versionName);

        verify(versionDaoMock).create(ITEM_ID, requestedVersion);
        verify(asdcItemManager).updateVersionStatus(ITEM_ID, Draft, null);
        verify(versionDaoMock).publish(eq(ITEM_ID), eq(requestedVersion), eq("Create version: 4.0"));
    }

    @Test
    public void testCreateWithExistingName() {

        expectedException.expect(CoreException.class);
        expectedException.expectMessage("Item itemId: create version failed, a version with the name 2.0 already exist");

        Version version = new Version();
        version.setBaseId("baseVersionId");

        Version baseVersion = createVersion(version.getBaseId(), Certified, UpToDate, false);
        // TODO: 12/13/2017 fix to eq(new Version("baseVersionId")) when version.equals will be fixed
        doReturn(Optional.of(baseVersion)).when(versionDaoMock).get(eq(ITEM_ID), any(Version.class));

        String versionName = "2.0";
        doReturn(versionName)
            .when(versionCalculatorMock).calculate(baseVersion.getName(), VersionCreationMethod.major);

        doReturn(Stream.of(createVersion("1", null, null, false),
            createVersion("2", null, null, false),
            createVersion("3", null, null, false)).collect(Collectors.toList()))
            .when(versionDaoMock).list(ITEM_ID);

        versioningManager.create(ITEM_ID, version, VersionCreationMethod.major);
    }

    @Test
    public void testSubmitCertified() {

        expectedException.expect(CoreException.class);
        expectedException.expectMessage("Item itemId: submit version failed, version versionId is already Certified");

        Version version = new Version(VERSION_ID);

        Version returnedVersion = createVersion(VERSION_ID, Certified, UpToDate, false);
        doReturn(Optional.of(returnedVersion)).when(versionDaoMock).get(ITEM_ID, version);

        versioningManager.submit(ITEM_ID, version, "Submit message");
    }

    @Test
    public void testSubmit() {
        Version version = new Version(VERSION_ID);

        ArgumentCaptor<Version> versionArgumentCaptor = ArgumentCaptor.forClass(Version.class);

        Version returnedVersion = createVersion(VERSION_ID, Draft, UpToDate, false);
        doReturn(Optional.of(returnedVersion)).when(versionDaoMock).get(ITEM_ID, version);

        String submitDescription = "Submit message";
        versioningManager.submit(ITEM_ID, version, submitDescription);

        verify(versionDaoMock).update(eq(ITEM_ID), versionArgumentCaptor.capture());
        Assert.assertEquals(Certified, versionArgumentCaptor.getValue().getStatus());
        verify(versionDaoMock).publish(ITEM_ID, version, submitDescription);
        verify(asdcItemManager).updateVersionStatus(ITEM_ID, Certified, Draft);
    }

    @Test
    public void testPublish() {
        Version version = new Version(VERSION_ID);
        String publishDescription = "Publish message";

        versioningManager.publish(ITEM_ID, version, publishDescription);

        verify(versionDaoMock).publish(ITEM_ID, version, publishDescription);
    }

    @Test
    public void testSync() {
        Version version = new Version(VERSION_ID);

        versioningManager.sync(ITEM_ID, version);

        verify(versionDaoMock).sync(ITEM_ID, version);
    }

    @Test
    public void testForceSync() {
        Version version = new Version(VERSION_ID);

        versioningManager.forceSync(ITEM_ID, version);

        verify(versionDaoMock).forceSync(ITEM_ID, version);
    }

    @Test
    public void testRevert() {
        Version version = new Version(VERSION_ID);
        String revisionId = "revisionId";

        versioningManager.revert(ITEM_ID, version, revisionId);

        verify(versionDaoMock).revert(ITEM_ID, version, revisionId);
    }

    @Test
    public void testListRevisions() {
        Version version = new Version(VERSION_ID);

        List<Revision> returnedRevisions =
            Stream.of(new Revision(), new Revision()).collect(Collectors.toList());
        doReturn(returnedRevisions)
            .when(versionDaoMock).listRevisions(ITEM_ID, version);

        List<Revision> revisions = versioningManager.listRevisions(ITEM_ID, version);
        Assert.assertEquals(revisions, returnedRevisions);
    }

    private Version createVersion(String id, VersionStatus status,
                                  SynchronizationState syncState, boolean dirty) {
        Version version = new Version(id);
        version.setName(id + ".0");
        version.setDescription(id + " desc");
        version.setStatus(status);

        VersionState state = new VersionState();
        state.setSynchronizationState(syncState);
        state.setDirty(dirty);
        version.setState(state);
        return version;
    }
}
