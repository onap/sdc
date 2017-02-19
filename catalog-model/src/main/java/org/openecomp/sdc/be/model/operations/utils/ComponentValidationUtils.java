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

package org.openecomp.sdc.be.model.operations.utils;

import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.operations.api.IComponentOperation;
import org.openecomp.sdc.be.model.operations.api.IResourceOperation;
import org.openecomp.sdc.be.model.operations.api.IServiceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

public class ComponentValidationUtils {

	private static Logger log = LoggerFactory.getLogger(ComponentValidationUtils.class.getName());

	public static boolean canWorkOnResource(Resource resource, String userId) {
		// verify resource is checked-out
		if (resource.getLifecycleState() != LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT) {
			log.debug("resource is not checked-out");
			return false;
		}
		// verify resource is not deleted
		if ((resource.getIsDeleted() != null) && (resource.getIsDeleted() == true)) {
			log.debug("resource is marked as delete");
			return false;
		}
		// verify resource last update user is the current user
		if (!userId.equals(resource.getLastUpdaterUserId())) {
			log.debug("resource last update is not {}", userId);
			return false;
		}
		return true;
	}

	public static boolean canWorkOnResource(String resourceId, IResourceOperation resourceOperation,
			String userId) {
		Either<Resource, StorageOperationStatus> getResourceResult = resourceOperation.getLightComponent(resourceId,
				false);

		if (getResourceResult.isRight()) {
			log.debug("Failed to retrive resource, resource id {}", resourceId);
			return false;
		}
		Resource resource = getResourceResult.left().value();

		return canWorkOnResource(resource, userId);

	}

	public static boolean canWorkOnService(String serviceId, IServiceOperation serviceOperation, String userId) {
		Either<Service, StorageOperationStatus> getResourceResult = serviceOperation.getLightComponent(serviceId,
				false);

		if (getResourceResult.isRight()) {
			log.debug("Failed to retrieve service, service id {}", serviceId);
			return false;
		}
		Service service = getResourceResult.left().value();

		return canWorkOnComponent(service, userId);

	}

	public static boolean canWorkOnComponent(String componentId, IComponentOperation componentOperation,
			String userId) {
		Either<Component, StorageOperationStatus> getResourceResult = componentOperation.getLightComponent(componentId,
				false);

		if (getResourceResult.isRight()) {
			log.debug("Failed to retrieve component, component id {}", componentId);
			return false;
		}
		Component service = getResourceResult.left().value();

		return canWorkOnComponent(service, userId);
	}

	public static boolean canWorkOnComponent(Component component, String userId) {
		// verify resource is checked-out
		if (component.getLifecycleState() != LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT) {
			log.debug("resource is not checked-out");
			return false;
		}

		// verify userId is not null
		if (userId == null) {
			log.debug("current user userId is null");
			return false;
		}

		// verify resource last update user is the current user
		if (!userId.equals(component.getLastUpdaterUserId())) {
			log.debug("resource last updater userId is not {}", userId);
			return false;
		}
		return true;
	}

}
