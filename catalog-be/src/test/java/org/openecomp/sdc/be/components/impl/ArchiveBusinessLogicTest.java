/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.impl;

import com.google.common.collect.ImmutableList;
import fj.data.Either;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArchiveOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ArchiveBusinessLogicTest {
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;
    @Mock
    private User user;
    @Mock
    private ResponseFormat responseFormat;
    @Mock
    private Component component;

    @InjectMocks
    private ArchiveBusinessLogic archiveBusinessLogic;

    @Test
    public void auditLastComponentVersionOnlyAndIgnorePreviousVersions() {
        List<String> archivedCompIds = Arrays.asList("1", "2", "3", "4", "5");
        when(toscaOperationFacade.getToscaElement(any(String.class), any(ComponentParametersView.class)))
                .thenReturn(Either.left(component));
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        when(component.getUUID())
                .thenReturn("1")
                .thenReturn("1")
                .thenReturn("2")
                .thenReturn("3")
                .thenReturn("2");

        archiveBusinessLogic.auditAction(ArchiveOperation.Action.ARCHIVE, archivedCompIds, user, ComponentTypeEnum.RESOURCE_PARAM_NAME);
        verify(componentsUtils, times(3)).auditComponentAdmin(eq(responseFormat), eq(user), eq(component),
                eq(AuditingActionEnum.ARCHIVE_COMPONENT), eq(ComponentTypeEnum.RESOURCE), any(String.class));
    }


    @Test
    public void auditLastComponentVersionOnly() {
        List<String> archivedCompIds = Arrays.asList("1", "2", "3", "4", "5");
        when(toscaOperationFacade.getToscaElement(any(String.class), any(ComponentParametersView.class)))
                .thenReturn(Either.left(component));
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(responseFormat);
        when(component.getUUID())
                .thenReturn("1")
                .thenReturn("2")
                .thenReturn("4")
                .thenReturn("5")
                .thenReturn("3");

        archiveBusinessLogic.auditAction(ArchiveOperation.Action.RESTORE, archivedCompIds, user, ComponentTypeEnum.RESOURCE_PARAM_NAME);
        verify(componentsUtils, times(5)).auditComponentAdmin(eq(responseFormat), eq(user), eq(component),
                eq(AuditingActionEnum.RESTORE_COMPONENT), eq(ComponentTypeEnum.RESOURCE), any(String.class));
    }


    @Test
    public void noAuditDoneForEmptyList() {
        List<String> archivedCompIds = ImmutableList.of();
        archiveBusinessLogic.auditAction(ArchiveOperation.Action.RESTORE, archivedCompIds, user, ComponentTypeEnum.RESOURCE_PARAM_NAME);
        verify(componentsUtils, times(0)).auditComponentAdmin(any(ResponseFormat.class), any(User.class), any(Component.class),
                any(AuditingActionEnum.class), any(ComponentTypeEnum.class), any(String.class));
    }


    @Test
    public void noAuditOnErrorGetElementResponse() {
        List<String> archivedCompIds = Arrays.asList("1", "2", "3", "4", "5");
        when(toscaOperationFacade.getToscaElement(any(String.class), any(ComponentParametersView.class)))
                .thenReturn(Either.right(StorageOperationStatus.NOT_FOUND));
        archiveBusinessLogic.auditAction(ArchiveOperation.Action.RESTORE, archivedCompIds, user, ComponentTypeEnum.RESOURCE_PARAM_NAME);
        verify(componentsUtils, times(0)).auditComponentAdmin(any(ResponseFormat.class), any(User.class), any(Component.class),
                any(AuditingActionEnum.class), any(ComponentTypeEnum.class), any(String.class));
    }



}
