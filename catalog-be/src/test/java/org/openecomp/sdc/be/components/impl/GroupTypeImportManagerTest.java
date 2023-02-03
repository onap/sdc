/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia. All rights reserved.
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

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.impl.model.ToscaTypeImportData;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.normatives.ElementTypeEnum;
import org.openecomp.sdc.be.model.operations.impl.GroupTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;

import fj.data.Either;

@RunWith(MockitoJUnitRunner.class)
public class GroupTypeImportManagerTest {

    @Mock
    GroupTypeOperation groupTypeOperation;
    @Mock
    ComponentsUtils componentsUtils;
    @Mock
    ToscaOperationFacade toscaOperationFacade;
    @Mock
    CommonImportManager commonImportManager;
    @Mock
    ModelOperation modelOperation;
    @Mock
    private ToscaTypeImportData data;

    @Test
    public void shouldInvokeCreateElementTypes() {
        GroupTypeImportManager groupTypeImportManager = new GroupTypeImportManager(groupTypeOperation, componentsUtils,
            toscaOperationFacade, commonImportManager, modelOperation);
        
        when(commonImportManager.createElementTypes(any(ToscaTypeImportData.class), any(), any(), any())).thenReturn(Either.left(Collections.emptyList()));
                
        groupTypeImportManager.createGroupTypes(data, "test model", true);
        verify(commonImportManager).createElementTypes(any(ToscaTypeImportData.class), any(), any(), any());
        verify(commonImportManager).addTypesToDefaultImports(any(ElementTypeEnum.class), any(), any());
    }
    
    @Test
    public void shouldInvokeCreateElementTypes_Error() {
        GroupTypeImportManager groupTypeImportManager = new GroupTypeImportManager(groupTypeOperation, componentsUtils,
            toscaOperationFacade, commonImportManager, modelOperation);
        
        when(commonImportManager.createElementTypes(any(ToscaTypeImportData.class), any(), any(), any())).thenReturn(Either.right(null));
                
        groupTypeImportManager.createGroupTypes(data, "test model", true);
        verify(commonImportManager).createElementTypes(any(ToscaTypeImportData.class), any(), any(), any());
        verify(commonImportManager, never()).addTypesToDefaultImports(any(ElementTypeEnum.class), any(), any());
    }
}