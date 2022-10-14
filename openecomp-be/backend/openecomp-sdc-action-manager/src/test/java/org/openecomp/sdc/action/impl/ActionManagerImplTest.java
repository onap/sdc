/*
 *
 *  Copyright © 2017-2018 European Support Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.openecomp.sdc.action.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.openecomp.core.dao.UniqueValueDao;
import org.openecomp.sdc.action.ActionConstants;
import org.openecomp.sdc.action.dao.ActionArtifactDao;
import org.openecomp.sdc.action.dao.ActionDao;
import org.openecomp.sdc.action.dao.types.ActionArtifactEntity;
import org.openecomp.sdc.action.dao.types.ActionEntity;
import org.openecomp.sdc.action.errors.ActionException;
import org.openecomp.sdc.action.types.Action;
import org.openecomp.sdc.action.types.ActionArtifact;
import org.openecomp.sdc.action.types.ActionArtifactProtection;
import org.openecomp.sdc.action.types.ActionStatus;
import org.openecomp.sdc.action.types.OpenEcompComponent;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.versioning.ActionVersioningManager;
import org.openecomp.sdc.versioning.dao.VersionInfoDao;
import org.openecomp.sdc.versioning.dao.types.UserCandidateVersion;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;
import org.openecomp.sdc.versioning.errors.VersioningErrorCodes;
import org.openecomp.sdc.versioning.types.VersionInfo;

@RunWith(MockitoJUnitRunner.class)
public class ActionManagerImplTest {

    @Mock
    private ActionDao actionDao;
    @Mock
    private ActionVersioningManager versioningManager;
    @Mock
    private ActionArtifactDao actionArtifactDao;
    @Mock
    private VersionInfoDao versionInfoDao;
    @Mock
    private UniqueValueDao uniqueValueDao;

    private ActionManagerImpl actionManager;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        actionManager = new ActionManagerImpl(actionDao, versioningManager, actionArtifactDao,
            versionInfoDao, uniqueValueDao);
    }

    @Test
    public void testCreateAction() {
        Action action = createAction();
        Version version = createVersion();
        doReturn(version).when(versioningManager).create(anyString(), anyString(), anyString());
        doReturn(action).when(actionDao).createAction(any());
        actionManager.createAction(action, "USER");
        Mockito.verify(actionDao, times(1)).createAction(any());
    }

    @Test
    public void testGetActionsByActionInvariantUuIdShouldPass() {
        String invariantId = "invariantId";
        Mockito.when(actionDao.getActionsByActionInvariantUuId(invariantId.toUpperCase())).thenReturn(mockActionsToReturn());
        List<Action> actions = actionManager.getActionsByActionInvariantUuId(invariantId);
        Assert.assertEquals(1, actions.size());
    }

    @Test(expected = ActionException.class)
    public void testGetActionsByActionInvariantUuIdShouldThrowExceptionWhenReturnedActionsOrEmptyOrNull() {
        String invariantId = "invariantId";
        Mockito.when(actionDao.getActionsByActionInvariantUuId(invariantId.toUpperCase())).thenReturn(new ArrayList<>());
        actionManager.getActionsByActionInvariantUuId(invariantId);

    }

    @Test
    public void testGetFilteredActionsShouldPassForFilterTypeName() {
        Mockito.when(actionDao.getFilteredActions(Matchers.anyString(), Matchers.anyString()))
            .thenReturn(mockActionsToReturn());
        List<Action> actions = actionManager.getFilteredActions("NAME", ActionConstants.FILTER_TYPE_NAME);
        Assert.assertEquals(1, actions.size());
    }

    @Test(expected = ActionException.class)
    public void testGetFilteredActionsShouldThrowExceptionForFilterTypeNameWhenReturnedActionsOrEmptyOrNull() {
        Mockito.when(actionDao.getFilteredActions(Matchers.anyString(), Matchers.anyString()))
            .thenReturn(new ArrayList<>());
        actionManager.getFilteredActions("NAME", ActionConstants.FILTER_TYPE_NAME);
    }

    @Test
    public void testGetFilteredActionsByMajorMinorVersionShouldPassWithActiveVersion() {
        Mockito.when(actionDao.getFilteredActions(Matchers.anyString(), Matchers.anyString()))
            .thenReturn(mockActionsToReturn());

        Map<String, VersionInfo> actionVersionMap = new HashMap<>();
        VersionInfo versionInfo = createVersionInfo();
        actionVersionMap.put("uuid", versionInfo);

        Mockito.when(versioningManager.listEntitiesVersionInfo(Matchers.anyString(),
            Matchers.anyString(), Matchers.any())).thenReturn(actionVersionMap);
        List<Action> actions = actionManager.getFilteredActions("type", ActionConstants.FILTER_TYPE_NAME);
        Assert.assertEquals(1, actions.size());
    }

    @Test
    public void testGetFilteredActionsByMajorMinorVersionShouldPassWithLatestFinalVersion() {
        Mockito.when(actionDao.getFilteredActions(Matchers.anyString(), Matchers.anyString()))
            .thenReturn(mockActionsToReturn());

        Map<String, VersionInfo> actionVersionMap = new HashMap<>();
        VersionInfo versionInfo = createVersionInfo();
        actionVersionMap.put("uuid", versionInfo);

        Mockito.when(versioningManager.listEntitiesVersionInfo(Matchers.anyString(),
            Matchers.anyString(), Matchers.any())).thenReturn(actionVersionMap);
        List<Action> actions = actionManager.getFilteredActions("type", ActionConstants.FILTER_TYPE_NAME);
        Assert.assertEquals(1, actions.size());
    }

    @Test
    public void testGetActionsByActionUuIdShouldPassIfReturnedActionsAreNotNull() {
        String actionUuId = "actionUuId";
        Mockito.when(actionDao.getActionsByActionUuId(actionUuId.toUpperCase()))
            .thenReturn(new Action());
        Assert.assertNotNull(actionManager.getActionsByActionUuId(actionUuId));
    }

    @Test(expected = ActionException.class)
    public void testGetActionsByActionUuIdShouldThrowExceptionIfReturnedActionsAreNull() {
        String actionUuId = "actionUuId";
        Mockito.when(actionDao.getActionsByActionUuId(actionUuId.toUpperCase()))
            .thenReturn(null);
        actionManager.getActionsByActionUuId(actionUuId);
    }

    @Test
    public void testGetOpenEcompComponents() {
        ArrayList<OpenEcompComponent> ecompComponents = new ArrayList<>();
        ecompComponents.add(new OpenEcompComponent());
        Mockito.when(actionDao.getOpenEcompComponents())
            .thenReturn(ecompComponents);
        Assert.assertEquals(1, actionManager.getOpenEcompComponents().size());
    }

    @Test
    public void testDeleteActionShouldPassIfDeleteMethodGeCallsOneTime() {
        String actionInvariantUuId = "actionInvariantUuId";
        String user = "user";

        actionManager.deleteAction(actionInvariantUuId, user);

        Mockito.verify(versioningManager, times(1)).delete(anyString(), anyString(), anyString());
        Mockito.verify(actionDao, times(1)).deleteAction(anyString());
    }

    @Test
    public void testUpdateActionShouldUpdateActionSuccessfully() {
        Action action = new Action();
        action.setActionInvariantUuId("actionInvId");
        action.setName("actionToupdate");
        action.setData("{actionInvariantUuId : actionInvariantUuId, name : actionToupdate}");
        VersionInfo versionInfo = createVersionInfo();
        Version activeVersion = new Version("2.1");

        versionInfo.setActiveVersion(activeVersion);
        when(versioningManager.getEntityVersionInfo(anyString(), anyString(), anyString(), any()))
            .thenReturn(versionInfo);

        ActionEntity actionEntity = createActionEntity();

        when(actionDao.get(any())).thenReturn(actionEntity);
        actionManager.updateAction(action, "user");
        Mockito.verify(actionDao, times(1)).updateAction(any());
    }

    @Test(expected = ActionException.class)
    public void testUpdateActionShouldThrowExceptionIfToUpdateAndExistingActionNameIsNotSame() {
        Action action = createAction();
        VersionInfo versionInfo = createVersionInfo();
        Version activeVersion = new Version("2.1");

        versionInfo.setActiveVersion(activeVersion);
        when(versioningManager.getEntityVersionInfo(anyString(), anyString(), anyString(), any()))
            .thenReturn(versionInfo);
        actionManager.updateAction(action, "user");
    }

    @Test
    public void testCheckoutShouldPassSuccessFully() {
        String invariantUuId = "invariantUuId";
        ActionEntity actionEntity = createActionEntity();
        when(actionDao.get(any())).thenReturn(actionEntity);
        Mockito.when(versioningManager.checkout(anyString(), anyString(), anyString())).thenReturn(createVersion());
        Action action = actionManager.checkout(invariantUuId, "user");
        Assert.assertNotNull(action);
        Mockito.verify(actionDao, times(1)).update(Matchers.any(ActionEntity.class));

    }

    @Test(expected = ActionException.class)
    public void testCheckoutShouldFailInCaseOfException() {
        String invariantUuId = "invariantUuId";
        Mockito.when(versioningManager.checkout(anyString(), anyString(), anyString()))
            .thenThrow(new CoreException(new ErrorCode.ErrorCodeBuilder()
                .withId(VersioningErrorCodes.CHECKOT_ON_LOCKED_ENTITY).build()));
        VersionInfoEntity versionInfoEntity = createVersionInfoEntity();
        when(versionInfoDao.get(any(VersionInfoEntity.class))).thenReturn(versionInfoEntity);
        actionManager.checkout(invariantUuId, "user");

    }

    @Test
    public void testUndoCheckoutShouldPass() {
        VersionInfoEntity versionInfoEntity = createVersionInfoEntity();
        when(versionInfoDao.get(any(VersionInfoEntity.class))).thenReturn(versionInfoEntity);
        when(versioningManager.undoCheckout(anyString(), anyString(), anyString())).thenReturn(createVersion());
        ActionEntity actionEntity = createActionEntity();

        when(actionDao.get(any(ActionEntity.class))).thenReturn(actionEntity);

        actionManager.undoCheckout("invariantUuid", "user");
        Mockito.verify(actionArtifactDao, times(1)).delete(any(ActionArtifactEntity.class));
    }

    @Test(expected = ActionException.class)
    public void testUndoCheckoutShouldThrowExceptionIfVersionInfoEntityIsNull() {
        when(versionInfoDao.get(any(VersionInfoEntity.class))).thenReturn(null);
        actionManager.undoCheckout("invariantUuid", "user");

    }

    @Test
    public void testCheckinShouldPassForHappyScenario() {
        when(versioningManager.checkin(anyString(), anyString(), anyString(), Matchers.any()))
            .thenReturn(createVersion());
        when(actionDao.get(any(ActionEntity.class))).thenReturn(createActionEntity());
        Assert.assertNotNull(actionManager.checkin("invariantUuid", "user"));
        Mockito.verify(actionDao, times(1)).update(Matchers.any(ActionEntity.class));
    }

    @Test(expected = ActionException.class)
    public void testCheckinShouldShouldThrowExceptionInCaseOfAnyException() {
        when(versioningManager.checkin(anyString(), anyString(), anyString(), Matchers.any()))
            .thenThrow((new CoreException(new ErrorCode.ErrorCodeBuilder()
                .withId(VersioningErrorCodes.CHECKIN_ON_UNLOCKED_ENTITY).build())));
        actionManager.checkin("invariantUuid", "user");
    }

    @Test
    public void testSubmitShouldPassForHappyScenario() {
        when(versioningManager.submit(anyString(), anyString(), anyString(), Matchers.any()))
            .thenReturn(createVersion());
        when(actionDao.get(any(ActionEntity.class))).thenReturn(createActionEntity());

        Assert.assertNotNull(actionManager.submit("invariantUuid", "user"));

        Mockito.verify(actionDao, times(1)).update(Matchers.any(ActionEntity.class));
    }

    @Test(expected = ActionException.class)
    public void testSubmitShouldThrowExceptionForAnyException() {
        when(versioningManager.submit(anyString(), anyString(), anyString(), Matchers.any()))
            .thenThrow((new CoreException(new ErrorCode.ErrorCodeBuilder()
                .withId(VersioningErrorCodes.SUBMIT_FINALIZED_ENTITY_NOT_ALLOWED).build())));
        actionManager.submit("invariantUuid", "user");
    }


    @Test
    public void testDownloadArtifactShouldPassForHappyScenario() {
        Action action = createAction();
        action.setVersion("2.1");
        when(actionDao.getActionsByActionUuId(anyString())).thenReturn(action);
        when(actionArtifactDao.downloadArtifact(anyInt(), anyString())).thenReturn(new ActionArtifact());
        Assert.assertNotNull(actionManager.downloadArtifact("actionUuId", "artifactUuId"));
    }

    @Test(expected = ActionException.class)
    public void testDownloadArtifactShouldThrowExceptionIfActionIsNull() {

        when(actionDao.getActionsByActionUuId(anyString())).thenReturn(null);
        actionManager.downloadArtifact("actionUuId", "artifactUuId");
    }

    @Test
    public void testUploadArtifactShouldPassForHappyScenario() {
        ActionArtifact artifact = createActionArtifact();
        artifact.setArtifactName("artifactNameToUpload");
        VersionInfo versionInfo = createVersionInfo();
        Version activeVersion = new Version("2.1");
        versionInfo.setActiveVersion(activeVersion);
        when(versioningManager.getEntityVersionInfo(anyString(), anyString(), anyString(), any()))
            .thenReturn(versionInfo);
        when(actionDao.get(any())).thenReturn(createActionEntity());
        Assert.assertNotNull(actionManager.uploadArtifact(artifact, "actionInvariantUuId", "user"));

        Mockito.verify(actionArtifactDao, times(1)).uploadArtifact(any(ActionArtifact.class));
        Mockito.verify(actionDao, times(1)).updateAction(any(Action.class));

    }

    @Test(expected = ActionException.class)
    public void testUploadArtifactShouldThrowExceptionIfArtifactAlreadyExist() {
        ActionArtifact artifact = createActionArtifact();
        VersionInfo versionInfo = createVersionInfo();
        Version activeVersion = new Version("2.1");
        versionInfo.setActiveVersion(activeVersion);
        when(versioningManager.getEntityVersionInfo(anyString(), anyString(), anyString(), any()))
            .thenReturn(versionInfo);
        when(actionDao.get(any())).thenReturn(createActionEntity());
        actionManager.uploadArtifact(artifact, "actionInvariantUuId", "user");

    }

    @Test
    public void testDeleteArtifactShouldPassForHappyScenario() {
        Action action = createAction();
        action.setVersion("2.1");
        action.getArtifacts().forEach(actionArtifact -> {
            actionArtifact.setArtifactUuId("86B2B1049CC13B4E9275414DBB29485C");
            actionArtifact.setArtifactProtection(ActionArtifactProtection.readWrite.name());
        });
        when(actionDao.getLockedAction(anyString(), anyString())).thenReturn(action);
        actionManager.deleteArtifact("actionInvariantUuId", "86B2B1049CC13B4E9275414DBB29485C", "user");
        Mockito.verify(actionDao, times(1)).update(any(ActionEntity.class));
        Mockito.verify(actionArtifactDao, times(1)).delete(any(ActionArtifactEntity.class));
    }

    @Test(expected = ActionException.class)
    public void testDeleteArtifactShouldThrowExceptionIfArtifactMetaDataIsNull() {
        when(actionDao.getLockedAction(anyString(), anyString())).thenReturn(createAction());
        actionManager.deleteArtifact("actionInvariantUuId", "86B2B1049CC13B4E9275414DBB29485C", "user");
    }

    @Test
    public void testUpdateArtifactShouldPassForHappyScenario() {
        ActionArtifact artifact = createActionArtifact();
        VersionInfo versionInfo = createVersionInfo();
        Version activeVersion = new Version("2.1");
        versionInfo.setActiveVersion(activeVersion);
        when(versioningManager.getEntityVersionInfo(anyString(), anyString(), anyString(), any()))
            .thenReturn(versionInfo);
        when(actionDao.get(any())).thenReturn(createActionEntity());
        actionManager.updateArtifact(artifact, "actionInvariantUuId", "user");
        Mockito.verify(actionArtifactDao, times(1)).updateArtifact(any(ActionArtifact.class));
    }

    @Test(expected = ActionException.class)
    public void testUpdateArtifactShouldThrowExceptionIfArtifactNotExist() {
        ActionArtifact artifact = createActionArtifact();
        artifact.setArtifactUuId("Uuid");
        VersionInfo versionInfo = createVersionInfo();
        Version activeVersion = new Version("2.1");
        versionInfo.setActiveVersion(activeVersion);
        when(actionDao.get(any())).thenReturn(createActionEntity());
        when(versioningManager.getEntityVersionInfo(anyString(), anyString(), anyString(), any()))
            .thenReturn(versionInfo);
        actionManager.updateArtifact(artifact, "actionInvariantUuId", "user");
    }

    private ActionArtifact createActionArtifact() {
        ActionArtifact artifact = new ActionArtifact();
        artifact.setArtifactUuId("artifactUuId");
        artifact.setArtifactName("artifactName");
        artifact.setArtifact(new byte[0]);
        return artifact;
    }

    private List<Action> mockActionsToReturn() {
        List<Action> actionList = new ArrayList<>();
        Action action = new Action();
        action.setActionInvariantUuId("uuid");
        action.setVersion("1.1");
        action.setStatus(ActionStatus.Available);
        action.setData("action data");
        actionList.add(action);
        return actionList;
    }

    private VersionInfo createVersionInfo() {
        VersionInfo versionInfo = new VersionInfo();
        Version version = createVersion();
        versionInfo.setActiveVersion(version);
        versionInfo.setLatestFinalVersion(version);
        return versionInfo;
    }

    private Version createVersion() {
        Version version = new Version();
        version.setMajor(1);
        version.setMinor(1);
        return version;
    }

    private ActionEntity createActionEntity() {
        ActionEntity actionEntity = new ActionEntity();
        actionEntity.setData("{actionUuId : actionUuId, actionInvariantUuId : actionInvariantUuId," +
            " name : actionToupdate,version: 2.1 ," +
            " artifacts : [{artifactUuId: artifactUuId ,artifactName : artifactName," +
            "artifactLabel: artifactLabel, artifactProtection : readWrite, artifactCategory : artifactCategory," +
            "artifactDescription: artifactDescription}] }");
        actionEntity.setUser("user");
        actionEntity.setTimestamp(new Date());
        return actionEntity;
    }

    private Action createAction() {
        Action action = new Action();
        action.setActionUuId("uid");
        action.setActionInvariantUuId("uuid");
        action.setName("actionToupdate2");
        action.setData("{actionInvariantUuId : actionInvariantUuId," +
            " name : actionToupdate, artifacts : [{artifactName : artifactName}] }");

        List<ActionArtifact> actionArtifacts = new ArrayList<>();
        ActionArtifact actionArtifact = new ActionArtifact();
        actionArtifact.setArtifactName("artifactName");
        actionArtifact.setArtifactUuId("artifactUuId");
        actionArtifacts.add(actionArtifact);
        action.setArtifacts(actionArtifacts);

        return action;
    }

    private VersionInfoEntity createVersionInfoEntity() {
        VersionInfoEntity versionInfoEntity = new VersionInfoEntity();
        UserCandidateVersion userCandidateVersion = new UserCandidateVersion();
        userCandidateVersion.setUser("user");
        userCandidateVersion.setVersion(createVersion());
        versionInfoEntity.setCandidate(userCandidateVersion);
        return versionInfoEntity;
    }

}
