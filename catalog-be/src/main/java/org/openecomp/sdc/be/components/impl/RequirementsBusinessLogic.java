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

package org.openecomp.sdc.be.components.impl;

import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.api.IResourceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.utils.ComponentValidationUtils;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("requirementsBusinessLogic")
public class RequirementsBusinessLogic {
	private static Logger log = LoggerFactory.getLogger(RequirementsBusinessLogic.class.getName());

	@javax.annotation.Resource
	private ComponentsUtils componentsUtils;

	@javax.annotation.Resource
	private ResourceBusinessLogic resourceBusinessLogic;

	@javax.annotation.Resource
	private IResourceOperation resourceOperation;

	public Either<RequirementDefinition, ResponseFormat> updateRequirement(String resourceId, String requirementId, RequirementDefinition requirementDefinition, String userId) {

		// Get the resource from DB
		Either<Resource, StorageOperationStatus> status = getResource(resourceId);
		if (status.isRight()) {
			log.debug("Couldn't get resource {} from DB", resourceId);
			return Either.right(componentsUtils.getResponseFormatByResource(componentsUtils.convertFromStorageResponse(status.right().value()), ""));
		}
		Resource resource = status.left().value();
		if (resource == null) {
			BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeResourceMissingError, "Requirement Business Logic", resourceId);
			BeEcompErrorManager.getInstance().logBeComponentMissingError("Requirement Business Logic", ComponentTypeEnum.RESOURCE.getValue(), resourceId);
			log.debug("Couldn't get resource {} from DB", resourceId);
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.GENERAL_ERROR));
		}
		// verify that resource is checked-out and the user is the last updater
		if (!ComponentValidationUtils.canWorkOnResource(resource, userId)) {
			return Either.right(componentsUtils.getResponseFormat(ActionStatus.RESTRICTED_OPERATION));
		}

		// TODO
		return null;
	}

	private Either<Resource, StorageOperationStatus> getResource(final String resourceId) {

		log.debug("Get resource with id {}", resourceId);
		Either<Resource, StorageOperationStatus> status = resourceOperation.getResource(resourceId);
		if (status.isRight()) {
			log.debug("Resource with id {} was not found", resourceId);
			return Either.right(status.right().value());
		}

		Resource resource = status.left().value();
		if (resource == null) {
			log.debug("General Error while get resource with id {}", resourceId);
			return Either.right(StorageOperationStatus.GENERAL_ERROR);
		}
		return Either.left(resource);
	}

}
