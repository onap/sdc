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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Resource;

import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.CapabilityTypeOperation;
import org.openecomp.sdc.common.config.EcompErrorName;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("capabilityTypeImportManager")
public class CapabilityTypeImportManager {

	private static Logger log = LoggerFactory.getLogger(CapabilityTypeImportManager.class.getName());
	@Resource
	private CapabilityTypeOperation capabilityTypeOperation;
	@Resource
	private ComponentsUtils componentsUtils;
	@Resource
	private CommonImportManager commonImportManager;

	public Either<List<CapabilityTypeDefinition>, ResponseFormat> createCapabilityTypes(String capabilityYml) {
		Either<List<CapabilityTypeDefinition>, ActionStatus> capabilityTypes = createCapabilityTypesFromYml(capabilityYml);
		if (capabilityTypes.isRight()) {
			ActionStatus status = capabilityTypes.right().value();
			ResponseFormat responseFormat = componentsUtils.getResponseFormatByCapabilityType(status, null);
			return Either.right(responseFormat);
		}
		return createCapabilityTypesByDao(capabilityTypes.left().value());

	}

	private Either<List<CapabilityTypeDefinition>, ActionStatus> createCapabilityTypesFromYml(String capabilityYml) {
		return commonImportManager.createElementTypesFromYml(capabilityYml, (capTypeName, capTypeJsonData) -> createCapabilityType(capTypeName, capTypeJsonData));

	}

	private Either<List<CapabilityTypeDefinition>, ResponseFormat> createCapabilityTypesByDao(List<CapabilityTypeDefinition> capabilityTypesToCreate) {
		List<CapabilityTypeDefinition> createdCapabilities = new ArrayList<>();
		Either<List<CapabilityTypeDefinition>, ResponseFormat> eitherResult = Either.left(createdCapabilities);
		Iterator<CapabilityTypeDefinition> capTypeItr = capabilityTypesToCreate.iterator();
		boolean stopDao = false;
		while (capTypeItr.hasNext() && !stopDao) {
			CapabilityTypeDefinition capabilityType = capTypeItr.next();

			log.info("send capabilityType {} to dao for create", capabilityType.getType());
			Either<CapabilityTypeDefinition, StorageOperationStatus> dataModelResponse = capabilityTypeOperation.addCapabilityType(capabilityType);
			if (dataModelResponse.isRight()) {
				BeEcompErrorManager.getInstance().processEcompError(EcompErrorName.BeFailedAddingCapabilityTypeError, "Create CapabilityTypes");
				BeEcompErrorManager.getInstance().logBeFailedAddingNodeTypeError("Create CapabilityTypes", "capability type");
				log.debug("failed to create capabilityType: {}", capabilityType.getType());
				if (dataModelResponse.right().value() != StorageOperationStatus.SCHEMA_VIOLATION) {
					ResponseFormat responseFormat = componentsUtils.getResponseFormatByCapabilityType(componentsUtils.convertFromStorageResponseForCapabilityType(dataModelResponse.right().value()), capabilityType);
					eitherResult = Either.right(responseFormat);
					stopDao = true;
				}

			} else {
				createdCapabilities.add(capabilityType);
			}
			if (!capTypeItr.hasNext()) {
				log.info("capabilityTypes were created successfully!!!");
			}

		}

		return eitherResult;

	}

	private CapabilityTypeDefinition createCapabilityType(String capabilityTypeName, Map<String, Object> toscaJson) {
		CapabilityTypeDefinition capabilityType = new CapabilityTypeDefinition();

		capabilityType.setType(capabilityTypeName);

		// Description
		final Consumer<String> descriptionSetter = description -> capabilityType.setDescription(description);
		commonImportManager.setField(toscaJson, ToscaTagNamesEnum.DESCRIPTION.getElementName(), descriptionSetter);
		// Derived From
		final Consumer<String> derivedFromSetter = derivedFrom -> capabilityType.setDerivedFrom(derivedFrom);
		commonImportManager.setField(toscaJson, ToscaTagNamesEnum.DERIVED_FROM.getElementName(), derivedFromSetter);
		// Properties
		commonImportManager.setPropertiesMap(toscaJson, (values) -> capabilityType.setProperties(values));

		return capabilityType;
	}

}
