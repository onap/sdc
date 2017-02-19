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

package org.openecomp.sdc.be.model.operations.api;

import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.impl.ComponentOperation;
import org.openecomp.sdc.be.model.operations.impl.ResourceOperation;

import fj.data.Either;

public interface ILifecycleOperation {

	public ResourceOperation getResourceOperation();

	public Either<User, StorageOperationStatus> getComponentOwner(String resourceId, NodeTypeEnum nodeType,
			boolean inTransaction);

	public Either<? extends Component, StorageOperationStatus> checkinComponent(NodeTypeEnum nodeType,
			Component component, User modifier, User owner, boolean inTransaction);

	public Either<? extends Component, StorageOperationStatus> requestCertificationComponent(NodeTypeEnum nodeType,
			Component component, User modifier, User owner, boolean inTransaction);

	public Either<? extends Component, StorageOperationStatus> startComponentCertification(NodeTypeEnum nodeType,
			Component component, User modifier, User owner, boolean inTransaction);

	public Either<? extends Component, StorageOperationStatus> checkoutComponent(NodeTypeEnum nodeType,
			Component component, User modifier, User currentOwner, boolean inTransaction);

	public Either<? extends Component, StorageOperationStatus> certifyComponent(NodeTypeEnum nodeType,
			Component component, User modifier, User currentOwner, boolean inTransaction);

	public Either<? extends Component, StorageOperationStatus> cancelOrFailCertification(NodeTypeEnum nodeType,
			Component component, User modifier, User owner, LifecycleStateEnum nextState, boolean b);

	public Either<Boolean, StorageOperationStatus> deleteOldComponentVersions(NodeTypeEnum nodeType,
			String componentName, String uuid, boolean inTransaction);

	public Either<? extends Component, StorageOperationStatus> undoCheckout(NodeTypeEnum nodeType, Component resource,
			User modifier, User currentOwner, boolean inTransaction);

	public ComponentOperation getComponentOperation(NodeTypeEnum componentType);

}
