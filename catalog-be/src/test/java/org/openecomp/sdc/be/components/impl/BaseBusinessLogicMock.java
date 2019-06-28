/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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

import org.mockito.Mockito;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ArtifactsOperations;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.InterfaceOperation;
import org.openecomp.sdc.be.model.operations.api.IElementOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupInstanceOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupOperation;
import org.openecomp.sdc.be.model.operations.api.IGroupTypeOperation;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;

public abstract class BaseBusinessLogicMock {
    protected final IElementOperation elementDao = Mockito.mock(IElementOperation.class);
    protected final IGroupOperation groupOperation = Mockito.mock(IGroupOperation.class);
    protected final IGroupInstanceOperation groupInstanceOperation = Mockito.mock(IGroupInstanceOperation.class);
    protected final IGroupTypeOperation groupTypeOperation = Mockito.mock(IGroupTypeOperation.class);
    protected final GroupBusinessLogic groupBusinessLogic = Mockito.mock(GroupBusinessLogic.class);
    protected final InterfaceOperation interfaceOperation = Mockito.mock(InterfaceOperation.class);
    protected final InterfaceLifecycleOperation interfaceLifecycleTypeOperation = Mockito.mock(InterfaceLifecycleOperation.class);
    protected final ArtifactsOperations artifactToscaOperation = Mockito.mock(ArtifactsOperations.class);
}