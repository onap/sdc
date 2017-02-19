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

package org.openecomp.sdc.be.components.lifecycle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction.LifecycleChanceActionEnum;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.ILifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;

public abstract class LifeCycleTransition {

	protected ConfigurationManager configurationManager;
	protected ILifecycleOperation lifeCycleOperation;
	protected ComponentsUtils componentUtils;

	protected Map<ComponentTypeEnum, List<Role>> authorizedRoles;

	protected LifeCycleTransition(ComponentsUtils componentUtils, ILifecycleOperation lifecycleOperation) {

		// configurationManager = (ConfigurationManager)
		// context.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR);
		// lifeCycleOperation = LifecycleOperation.getInstance();
		this.configurationManager = ConfigurationManager.getConfigurationManager();
		this.lifeCycleOperation = lifecycleOperation;
		this.componentUtils = componentUtils;
		this.authorizedRoles = new HashMap<>();

	}

	public abstract LifeCycleTransitionEnum getName();

	public abstract AuditingActionEnum getAuditingAction();

	public ConfigurationManager getConfigurationManager() {
		return configurationManager;
	}

	public void setConfigurationManager(ConfigurationManager configurationManager) {
		this.configurationManager = configurationManager;
	}

	public ILifecycleOperation getLifeCycleOperation() {
		return lifeCycleOperation;
	}

	public void setLifeCycleOperation(ILifecycleOperation lifeCycleOperation) {
		this.lifeCycleOperation = lifeCycleOperation;
	}

	public List<Role> getAuthorizedRoles(ComponentTypeEnum componentType) {
		return authorizedRoles.get(componentType);
	}

	public void addAuthorizedRoles(ComponentTypeEnum componentType, List<Role> authorizedRoles) {
		this.authorizedRoles.put(componentType, authorizedRoles);
	}

	//
	// public Either<? extends Component, ResponseFormat>
	// changeState(ComponentTypeEnum componentType, Component component,
	// ComponentBusinessLogic componentBl, User modifier, User owner){
	// return changeState(componentType, component, componentBl, modifier,
	// owner, false);
	// }

	public abstract Either<? extends Component, ResponseFormat> changeState(ComponentTypeEnum componentType, Component component, ComponentBusinessLogic componentBl, User modifier, User owner, boolean needLock, boolean inTransaction);

	public abstract Either<Boolean, ResponseFormat> validateBeforeTransition(Component component, ComponentTypeEnum componentType, User modifier, User owner, LifecycleStateEnum oldState, LifecycleChangeInfoWithAction lifecycleChangeInfo);

	public Either<Boolean, ResponseFormat> validateBeforeTransition(Component component, ComponentTypeEnum componentType, User modifier, User owner, LifecycleStateEnum oldState) {

		return this.validateBeforeTransition(component, componentType, modifier, owner, oldState, null);
	}

	/**
	 * getComponentOwner
	 * 
	 * @param resource
	 * @return
	 */
	protected Either<User, ResponseFormat> getComponentOwner(Component component, ComponentTypeEnum componentType) {

		return getComponentOwner(component, componentType, false);
	}

	protected Either<User, ResponseFormat> getComponentOwner(Component component, ComponentTypeEnum componentType, boolean inTransaction) {

		NodeTypeEnum nodeType = componentType.getNodeType();
		Either<User, StorageOperationStatus> resourceOwnerResult = getLifeCycleOperation().getComponentOwner(component.getUniqueId(), nodeType, inTransaction);
		if (resourceOwnerResult.isRight()) {
			ResponseFormat responseFormat = componentUtils.getResponseFormatByComponent(componentUtils.convertFromStorageResponse(resourceOwnerResult.right().value()), component, componentType);
			return Either.right(responseFormat);
		}
		return Either.left(resourceOwnerResult.left().value());
	}

	/**
	 * isUserValidForRequest
	 * 
	 * @param modifier
	 * @param action
	 *            TODO
	 * @return
	 */
	protected Either<Boolean, ResponseFormat> userRoleValidation(User modifier, ComponentTypeEnum componentType, LifecycleChangeInfoWithAction lifecycleChangeInfo) {

		// validate user
		if (getAuthorizedRoles(componentType).contains(Role.valueOf(modifier.getRole()))) {
			return Either.left(true);
		}

		// this is only when creating vfc/cp when import vf from csar - when we
		// create resources from node type, we create need to change the state
		// to certified
		if (lifecycleChangeInfo != null && lifecycleChangeInfo.getAction() != null && lifecycleChangeInfo.getAction() == LifecycleChanceActionEnum.CREATE_FROM_CSAR) {
			return Either.left(true);
		}

		ResponseFormat responseFormat = componentUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
		return Either.right(responseFormat);
	}

}
