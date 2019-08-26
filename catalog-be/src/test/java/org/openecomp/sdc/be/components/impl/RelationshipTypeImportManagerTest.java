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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.operations.impl.RelationshipTypeOperation;

@RunWith(MockitoJUnitRunner.class)
public class RelationshipTypeImportManagerTest {

    @Mock
    private RelationshipTypeOperation relationshipTypeOperation;
    @Mock
    private CommonImportManager commonImportManager;
    @Mock
    private ComponentsUtils componentsUtils;

    @Test
    public void shouldInvokeCreateElementTypes() {
        RelationshipTypeImportManager relationshipTypeImportManager =
            new RelationshipTypeImportManager(relationshipTypeOperation, commonImportManager, componentsUtils);
        relationshipTypeImportManager.createRelationshipTypes("anyYaml");
        Mockito.verify(commonImportManager).createElementTypes(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

    }
}