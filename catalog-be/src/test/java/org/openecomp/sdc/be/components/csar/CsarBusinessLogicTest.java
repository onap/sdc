/*

 * Copyright (c) 2018 Huawei Intellectual Property.

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *     http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */
package org.openecomp.sdc.be.components.csar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.exception.ResponseFormat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CsarBusinessLogicTest {

    @InjectMocks
    private CsarBusinessLogic test;

    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @Mock
    private ComponentsUtils componentsUtils;

    @Mock
    private User user;

    @Test
    public void testValidateCsarBeforeCreate() {
        Resource resource = new Resource();
        String csarUUID = "csarUUID";
        when(toscaOperationFacade.validateCsarUuidUniqueness(csarUUID)).thenReturn(StorageOperationStatus.OK);
        test.validateCsarBeforeCreate(resource, AuditingActionEnum.ARTIFACT_DOWNLOAD, user, "csarUUID");
    }

    @Test
    public void testValidateCsarBeforeCreate_Exists() {
        Resource resource = new Resource();
        String csarUUID = "csarUUID";
        ResponseFormat responseFormat = new ResponseFormat();
        when(toscaOperationFacade.validateCsarUuidUniqueness(csarUUID)).thenReturn(StorageOperationStatus.ENTITY_ALREADY_EXISTS);
        when(componentsUtils.getResponseFormat(ActionStatus.VSP_ALREADY_EXISTS, csarUUID)).thenReturn(responseFormat);
        try {
            test.validateCsarBeforeCreate(resource, AuditingActionEnum.ARTIFACT_DOWNLOAD, user, "csarUUID");
        }
        catch(Exception e){

        }
    }

    @Test
    public void testValidateCsarBeforeCreate_Fail() {
        Resource resource = new Resource();
        String csarUUID = "csarUUID";
        when(toscaOperationFacade.validateCsarUuidUniqueness(csarUUID)).thenReturn(StorageOperationStatus.EXEUCTION_FAILED);
        try {
            test.validateCsarBeforeCreate(resource, AuditingActionEnum.ARTIFACT_DOWNLOAD, user, "csarUUID");
        }
        catch(Exception e){

        }
    }
}
