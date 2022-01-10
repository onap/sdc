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

package org.openecomp.sdcrests.action.rest.services;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.action.ActionConstants.X_OPEN_ECOMP_INSTANCE_ID_HEADER_PARAM;
import static org.openecomp.sdc.action.ActionConstants.X_OPEN_ECOMP_REQUEST_ID_HEADER_PARAM;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import org.apache.cxf.attachment.AttachmentDataSource;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.action.ActionManager;
import org.openecomp.sdc.action.errors.ActionException;
import org.openecomp.sdc.action.types.Action;
import org.openecomp.sdc.action.types.ActionArtifact;
import org.openecomp.sdc.action.types.ActionStatus;
import org.openecomp.sdc.action.types.OpenEcompComponent;

public class ActionsImplTest {

    @Mock
    HttpServletRequest request;
    @Mock
    private ActionManager actionManager;
    @InjectMocks
    ActionsImpl action ;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        when(request.getRemoteUser()).thenReturn("unit-test-user");
        when(request.getHeader(X_OPEN_ECOMP_INSTANCE_ID_HEADER_PARAM)).thenReturn("X-OPEN-ECOMP-InstanceID");
        when(request.getHeader(X_OPEN_ECOMP_REQUEST_ID_HEADER_PARAM)).thenReturn("X-OPEN-ECOMP-RequestID");
    }

    @Test
    public void testGetActionsByActionInvariantUuIdShouldWithEmptyQueryString() {

        when(request.getQueryString()).thenReturn("");
        when(actionManager.getActionsByActionInvariantUuId(anyString())).thenReturn(mockActionsToReturn());

        Response actionsByActionInvariantUuId = action.getActionsByActionInvariantUuId("invariantId", "actionUUID", request);
        Assert.assertEquals(200, actionsByActionInvariantUuId.getStatus());
    }

    @Test
    public void testGetActionsByActionInvariantUuIdShouldWithQueryString() {
        when(request.getQueryString()).thenReturn("queryString");
        when(actionManager.getActionsByActionUuId(anyString())).thenReturn(createAction());

        Response actionsByActionInvariantUuId = action.getActionsByActionInvariantUuId("actionInvariantUuId", "actionUUID", request);
        Assert.assertEquals(200, actionsByActionInvariantUuId.getStatus());
    }

    @Test(expected = ActionException.class)
    public void testGetActionsByActionInvariantUuIdShouldThrowExceptionWhenActionIsEmpty() {
        when(request.getQueryString()).thenReturn("queryString");
        when(actionManager.getActionsByActionUuId(anyString())).thenReturn(new Action());

        action.getActionsByActionInvariantUuId("actionInvariantUuId", "actionUUID", request);
    }

    @Test
    public void testGetOpenEcompComponentsShouldPassForHappyScenario() {
        ArrayList<OpenEcompComponent> ecompComponents = new ArrayList<>();
        ecompComponents.add(new OpenEcompComponent());
        when(actionManager.getOpenEcompComponents()).thenReturn(ecompComponents);
        Assert.assertEquals(200, action.getOpenEcompComponents(request).getStatus());
    }

    @Test(expected = ActionException.class)
    public void testGetOpenEcompComponentsShouldCatchActionException() {
        when(actionManager.getOpenEcompComponents()).thenThrow(new ActionException());
         action.getOpenEcompComponents(request).getStatus();
    }

    @Test
    public void testGetFilteredActionsShouldPassWhenQueryStringIsEmpty() {
        when(actionManager.getFilteredActions(anyString(), anyString())).thenReturn(mockActionsToReturn());
        Response filteredActions = action.getFilteredActions("vendor", "category", "name", "modelID",
                "componentID", request);

        Assert.assertEquals(200, filteredActions.getStatus());
    }

    @Test
    public void testGetFilteredActionsShouldPassWhenQueryStringIsNotEmptyWithVendor() {
        when(request.getQueryString()).thenReturn("queryString");
        when(actionManager.getFilteredActions(anyString(), anyString())).thenReturn(mockActionsToReturn());
        Response filteredActions = action.getFilteredActions("vendor", null, null, null,
                null, request);

        Assert.assertEquals(200, filteredActions.getStatus());
    }

    @Test
    public void testGetFilteredActionsShouldPassWhenQueryStringIsNotEmptyWithCategory() {
        when(request.getQueryString()).thenReturn("queryString");
        when(actionManager.getFilteredActions(anyString(), anyString())).thenReturn(mockActionsToReturn());
        Response filteredActions = action.getFilteredActions(null, "category", null, null,
                null, request);

        Assert.assertEquals(200, filteredActions.getStatus());
    }

    @Test
    public void testGetFilteredActionsShouldPassWhenQueryStringIsNotEmptyWithName() {
        when(request.getQueryString()).thenReturn("queryString");
        when(actionManager.getFilteredActions(anyString(), anyString())).thenReturn(mockActionsToReturn());
        Response filteredActions = action.getFilteredActions(null, null, "name", null,
                null, request);

        Assert.assertEquals(200, filteredActions.getStatus());
    }

    @Test
    public void testGetFilteredActionsShouldWhenQueryStringIsNotEmptyWithModel() {
        when(request.getQueryString()).thenReturn("queryString");
        when(actionManager.getFilteredActions(anyString(), anyString())).thenReturn(mockActionsToReturn());
        Response filteredActions = action.getFilteredActions(null, null, null, "modelId",
                null, request);

        Assert.assertEquals(200, filteredActions.getStatus());
    }

    @Test
    public void testGetFilteredActionsShouldPassWhenQueryStringIsNotEmptyWithComponent() {
        when(request.getQueryString()).thenReturn("queryString");
        when(actionManager.getFilteredActions(anyString(), anyString())).thenReturn(mockActionsToReturn());
        Response filteredActions = action.getFilteredActions(null, null, null, null,
                "componentId", request);

        Assert.assertEquals(200, filteredActions.getStatus());
    }

    @Test(expected = ActionException.class)
    public void testGetFilteredActionsShouldThrowActionExceptionWhenNumberOfFiltersAreZero() {
        when(request.getQueryString()).thenReturn("queryString");
        when(actionManager.getFilteredActions(anyString(), anyString())).thenReturn(mockActionsToReturn());
        action.getFilteredActions(null, null, null, null,
                null, request);

    }

    @Test
    public void testCreateActionShouldPassForHappyScenario() {
    String requestJson = "{actionUuId : actionUuId, actionInvariantUuId : actionInvariantUuId," +
            " name : actionToCreate, version: 2.1 }";
        when(actionManager.createAction(any(Action.class), anyString())).thenReturn(createAction());
        Assert.assertEquals(200, action.createAction( requestJson, request).getStatus());
    }
    @Test(expected = ActionException.class)
    public void testCreateActionShouldFailForInvalidRequestJson() {
        String requestJson = "{actionUuId : actionUuId, actionInvariantUuId : actionInvariantUuId," +
                "  version: 2.1 }";
        when(actionManager.createAction(any(Action.class), anyString())).thenReturn(createAction());
        action.createAction( requestJson, request);
    }

    @Test
    public void testUpdateActionShouldPassForHappyScenario() {
        String requestJson = "{actionUuId : actionUuId, actionInvariantUuId : actionInvariantUuId," +
                " name : actionToUpdate, version: 2.2 }";

        when(request.getRemoteUser()).thenReturn("remoteUser");
        when(actionManager.updateAction(any(Action.class), anyString())).thenReturn(createAction());

        Assert.assertEquals(200, action.updateAction("invariantUUID", requestJson, request).getStatus());
    }

    @Test(expected = ActionException.class)
    public void testUpdateActionShouldThrowActionExceptionWhenActionManagerUpdateFails() {
        String requestJson = "{actionUuId : actionUuId, actionInvariantUuId : actionInvariantUuId," +
                " name : actionToUpdate, version: 2.2 }";

        when(request.getRemoteUser()).thenReturn("remoteUser");
        when(actionManager.updateAction(any(Action.class), anyString())).thenThrow(new ActionException());
        action.updateAction("invariantUUID", requestJson, request);
    }

    @Test
    public void testDeleteActionShouldPassForHappyScenario() {
       Assert.assertEquals(200, action.deleteAction("actionInvariantUUID", request).getStatus());
       Mockito.verify(actionManager, times(1)).deleteAction(anyString(), anyString());
    }

    @Test
    public void testActOnActionShouldPassForStatusCheckout() {
        String requestJson = "{status : Checkout}";
        when(request.getRemoteUser()).thenReturn("remoteUser");
        when(actionManager.checkout(anyString(), anyString())).thenReturn(createAction());

       Assert.assertEquals(200, action.actOnAction("invariantUUID", requestJson, request).getStatus());

    }

    @Test
    public void testActOnActionShouldPassForStatusUndo_Checkout() {
        String requestJson = "{status : Undo_Checkout}";
        when(request.getRemoteUser()).thenReturn("remoteUser");

        Assert.assertEquals(200, action.actOnAction("invariantUUID", requestJson, request).getStatus());
        Mockito.verify(actionManager, times(1)).undoCheckout(anyString(), anyString());
    }

    @Test
    public void testActOnActionShouldPassForStatusCheckin() {
        String requestJson = "{status : Checkin}";
        when(request.getRemoteUser()).thenReturn("remoteUser");
        when(actionManager.checkin(anyString(), anyString())).thenReturn(createAction());
        Assert.assertEquals(200, action.actOnAction("invariantUUID", requestJson, request).getStatus());
    }

    @Test
    public void testActOnActionShouldPassForStatusSubmit() {
        String requestJson = "{status : Submit}";
        when(request.getRemoteUser()).thenReturn("remoteUser");
        when(actionManager.submit(anyString(), anyString())).thenReturn(createAction());
        Assert.assertEquals(200, action.actOnAction("invariantUUID", requestJson, request).getStatus());
    }


    @Test(expected = ActionException.class)
    public void testActOnActionShouldThrowActionExceptionWhenPassingInvalidAction() {
        String requestJson = "{status : Status}";
        when(request.getRemoteUser()).thenReturn("remoteUser");
        action.actOnAction("invariantUUID", requestJson, request);
    }


    @Test
    public void testUploadArtifactShouldPassForHappyScenario() throws IOException {

        Attachment artifactToUpload = new Attachment("id", "mediaType", new Object());
        DataSource dataSource = new AttachmentDataSource("ctParam", new ByteArrayInputStream(new byte[0]));
        DataHandler dataHandler = new DataHandler(dataSource);
        artifactToUpload.setDataHandler(dataHandler);

        when(request.getContentType()).thenReturn("contentType");
        when(actionManager.uploadArtifact(any(ActionArtifact.class), anyString(), anyString())).thenReturn(new ActionArtifact());
        Response response = action.uploadArtifact("actionInvariantUUID", "artifactName", "artifactLabel",
                "artifactCategory", "artifactDescription", "readOnly",
                "d41d8cd98f00b204e9800998ecf8427e",
                artifactToUpload, request);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test(expected = ActionException.class)
    public void testUploadArtifactShouldThrowActionExceptionWhenArtifactToUploadIsNull() throws IOException {
        when(request.getContentType()).thenReturn("contentType");
        action.uploadArtifact("actionInvariantUUID", "artifactName", "artifactLabel",
                "artifactCategory", "artifactDescription", "readOnly",
                "d41d8cd98f00b204e9800998ecf8427e",
                null, request);

    }

    @Test
    public void testDownloadArtifactShouldPassForHappyScenario() {
        ActionArtifact actionArtifact = new ActionArtifact();
        actionArtifact.setArtifactUuId("artifactUUID");
        actionArtifact.setArtifact(new byte[0]);
        actionArtifact.setArtifactName("artifactName");

        when(actionManager.downloadArtifact(anyString(), anyString())).thenReturn(actionArtifact);
        Response response = action.downloadArtifact("actionUUID", "artifactUUID", request);
        Assert.assertEquals(200, response.getStatus());
    }

    @Test(expected = ActionException.class)
    public void testDownloadArtifactShouldThrowActionExceptionWhenReDownloadedArtifactIsEmpty() {

        when(actionManager.downloadArtifact(anyString(), anyString())).thenReturn(new ActionArtifact());
        action.downloadArtifact("actionUUID", "artifactUUID", request);
    }

    @Test
    public void testDeleteArtifactShouldPassForHappyScenario() {
        action.deleteArtifact("actionInvariantUUID", "artifactUUID", request);
        Mockito.verify(actionManager, times(1)).deleteArtifact(anyString(), anyString(), anyString());
    }

    @Test
    public void testUpdateArtifactShouldPassForHappyScenario() throws IOException {
        Attachment artifactToUpdate = new Attachment("id", "mediaType", new Object());
        DataSource dataSource = new AttachmentDataSource("ctParam", new ByteArrayInputStream(new byte[0]));
        DataHandler dataHandler = new DataHandler(dataSource);
        artifactToUpdate.setDataHandler(dataHandler);

        when(request.getContentType()).thenReturn("contentType");

        Response response = action.updateArtifact("actionInvariantUUID", "artifactUUID", "artifactName",
                "artifactLabel",
                "artifactCategory", "artifactDescription", "readWrite",
                "d41d8cd98f00b204e9800998ecf8427e",
                artifactToUpdate, request);

        Mockito.verify(actionManager, times(1)).updateArtifact(any(ActionArtifact.class), anyString(), anyString());
        Assert.assertEquals(200, response.getStatus());
    }


    @Test(expected = ActionException.class)
    public void testUpdateArtifactShouldThrowActionExceptionWhenCheckSumDidNotMatchWithCalculatedCheckSum() throws IOException {

        Attachment artifactToUpdate = new Attachment("id", "mediaType", new Object());
        DataSource dataSource = new AttachmentDataSource("ctParam", new ByteArrayInputStream(new byte[0]));
        DataHandler dataHandler = new DataHandler(dataSource);
        artifactToUpdate.setDataHandler(dataHandler);

        when(request.getContentType()).thenReturn("contentType");
        action.updateArtifact("actionInvariantUUID", "artifactUUID", "artifactName",
                "artifactLabel",
                "artifactCategory", "artifactDescription", "readWrite",
                "checksum",
                artifactToUpdate, request);

    }

    private List<Action> mockActionsToReturn() {
        List<Action> actionList = new ArrayList<>();
        Action action = createAction();

        actionList.add(action);
        return actionList;
    }

    private Action createAction() {
        Action action = new Action();
        action.setActionInvariantUuId("actionInvariantUuId");
        action.setVersion("1.1");
        action.setUser("user");
        action.setStatus(ActionStatus.Available);
        action.setTimestamp(new Date());

        action.setData("{actionUuId : actionUuId, actionInvariantUuId : actionInvariantUuId," +
                " name : actionToupdate,version: 2.1 ," +
                " artifacts : [{artifactUuId: artifactUuId ,artifactName : artifactName," +
                "artifactLabel: artifactLabel, artifactProtection : readWrite, artifactCategory : artifactCategory," +
                "artifactDescription: artifactDescription}] }");
        return action;
    }

}
