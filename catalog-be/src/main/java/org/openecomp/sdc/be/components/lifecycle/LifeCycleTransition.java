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

import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction.LifecycleChanceActionEnum;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaElementLifecycleOperation;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;

import fj.data.Either;

public abstract class LifeCycleTransition {

	protected ConfigurationManager configurationManager;
	@Autowired
	protected ToscaElementLifecycleOperation lifeCycleOperation;
	@Autowired
	protected TitanDao titanDao;
	protected ComponentsUtils componentUtils;

	protected Map<ComponentTypeEnum, List<Role>> authorizedRoles;
	protected Map<ResourceTypeEnum, List<Role>> resourceAuthorizedRoles;
	
	ToscaOperationFacade toscaOperationFacade;

	protected LifeCycleTransition(ComponentsUtils componentUtils, ToscaElementLifecycleOperation lifecycleOperation2, ToscaOperationFacade toscaOperationFacade, TitanDao titanDao) {

		this.configurationManager = ConfigurationManager.getConfigurationManager();
		this.lifeCycleOperation = lifecycleOperation2;
		this.componentUtils = componentUtils;
		this.authorizedRoles = new HashMap<>();
		this.resourceAuthorizedRoles = new HashMap<>();
		this.toscaOperationFacade = toscaOperationFacade;
		this.titanDao = titanDao;
	}

	public abstract LifeCycleTransitionEnum getName();

	public abstract AuditingActionEnum getAuditingAction();

	public ConfigurationManager getConfigurationManager() {
		return configurationManager;
	}

	public void setConfigurationManager(ConfigurationManager configurationManager) {
		this.configurationManager = configurationManager;
	}

	public ToscaElementLifecycleOperation getLifeCycleOperation() {
		return lifeCycleOperation;
	}

	public void setLifeCycleOperation(ToscaElementLifecycleOperation lifeCycleOperation) {
		this.lifeCycleOperation = lifeCycleOperation;
	}

	public List<Role> getAuthorizedRoles(ComponentTypeEnum componentType) {
		return authorizedRoles.get(componentType);
	}

	public void addAuthorizedRoles(ComponentTypeEnum componentType, List<Role> authorizedRoles) {
		this.authorizedRoles.put(componentType, authorizedRoles);
	}
	
	public List<Role> getResourceAuthorizedRoles(ResourceTypeEnum resourceType) {
		return resourceAuthorizedRoles.get(resourceType);
	}

	public void addResouceAuthorizedRoles(ResourceTypeEnum resourceType, List<Role> authorizedRoles) {
		this.resourceAuthorizedRoles.put(resourceType, authorizedRoles);
	}

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

		Either<User, StorageOperationStatus> resourceOwnerResult = getLifeCycleOperation().getToscaElementOwner(component.getUniqueId());
		if (resourceOwnerResult.isRight()) {
			ResponseFormat responseFormat = componentUtils.getResponseFormatByComponent(componentUtils.convertFromStorageResponse(resourceOwnerResult.right().value()), component, componentType);
			return Either.right(responseFormat);
		}
		return Either.left(resourceOwnerResult.left().value());
	}

	protected Either<Boolean, ResponseFormat> userRoleValidation(User modifier,Component component, ComponentTypeEnum componentType, LifecycleChangeInfoWithAction lifecycleChangeInfo) {

		// validate user
		//first check the user for the component and then for the resource
		if (getAuthorizedRoles(componentType).contains(Role.valueOf(modifier.getRole())) || userResourceRoleValidation(component,componentType,modifier)) {
			return Either.left(true);
		}
		// this is only used in 2 cases
		//1. when creating vfc/cp when import vf from csar - when we
		// create resources from node type, we create need to change the state
		// to certified
		//2. certification flow upno upgrade migration
		if (lifecycleChangeInfo != null && lifecycleChangeInfo.getAction() != null && (lifecycleChangeInfo.getAction() == LifecycleChanceActionEnum.CREATE_FROM_CSAR|| lifecycleChangeInfo.getAction() == LifecycleChanceActionEnum.UPGRADE_MIGRATION)) {
			return Either.left(true);
		}

		ResponseFormat responseFormat = componentUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION);
		return Either.right(responseFormat);
	}
	
	protected boolean userResourceRoleValidation(Component component, ComponentTypeEnum componentType, User modifier) {
		if (componentType.equals(ComponentTypeEnum.RESOURCE)){
			ResourceTypeEnum resourceType = ((ResourceMetadataDataDefinition)component.getComponentMetadataDefinition().getMetadataDataDefinition()).getResourceType();
			if (getResourceAuthorizedRoles(resourceType)!=null && getResourceAuthorizedRoles(resourceType).contains(Role.valueOf(modifier.getRole()))) {
				return true;
			}
		} else {
			return false;
		}
		
		return false;
	}

}
