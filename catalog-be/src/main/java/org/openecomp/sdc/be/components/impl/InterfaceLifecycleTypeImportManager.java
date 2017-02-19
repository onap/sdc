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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fj.data.Either;

@Component("interfaceLifecycleTypeImportManager")
public class InterfaceLifecycleTypeImportManager {

	@Resource
	private IInterfaceLifecycleOperation interfaceLifecycleOperation;

	@Resource
	private ComponentsUtils componentsUtils;
	@Resource
	private CommonImportManager commonImportManager;

	private static Logger log = LoggerFactory.getLogger(InterfaceLifecycleTypeImportManager.class.getName());

	public Either<List<InterfaceDefinition>, ResponseFormat> createLifecycleTypes(String interfaceLifecycleTypesYml) {

		Either<List<InterfaceDefinition>, ActionStatus> interfaces = createLifecyclyTypeFromYml(interfaceLifecycleTypesYml);
		if (interfaces.isRight()) {
			ActionStatus status = interfaces.right().value();
			ResponseFormat responseFormat = componentsUtils.getResponseFormatByGroupType(status, null);
			return Either.right(responseFormat);
		}
		return createInterfacesByDao(interfaces.left().value());

	}

	private Either<List<InterfaceDefinition>, ActionStatus> createLifecyclyTypeFromYml(String interfaceLifecycleTypesYml) {
		return commonImportManager.createElementTypesFromYml(interfaceLifecycleTypesYml, (lifecycleTypeName, lifecycleTypeJsonData) -> createLifecycleType(lifecycleTypeName, lifecycleTypeJsonData));

	}

	private Either<List<InterfaceDefinition>, ResponseFormat> createInterfacesByDao(List<InterfaceDefinition> interfacesToCreate) {
		List<InterfaceDefinition> createdInterfaces = new ArrayList<>();
		Either<List<InterfaceDefinition>, ResponseFormat> eitherResult = Either.left(createdInterfaces);
		Iterator<InterfaceDefinition> interfaceItr = interfacesToCreate.iterator();
		boolean stopDao = false;
		while (interfaceItr.hasNext() && !stopDao) {
			InterfaceDefinition interfaceDef = interfaceItr.next();

			log.info("send interfaceDefinition {} to dao for create", interfaceDef.getType());

			Either<InterfaceDefinition, StorageOperationStatus> dataModelResponse = interfaceLifecycleOperation.createInterfaceType(interfaceDef);
			if (dataModelResponse.isRight()) {
				log.info("failed to create interface : {}  error: {}", interfaceDef.getType(), dataModelResponse.right().value().name());
				if (dataModelResponse.right().value() != StorageOperationStatus.SCHEMA_VIOLATION) {
					ResponseFormat responseFormat = componentsUtils.getResponseFormat(componentsUtils.convertFromStorageResponseForLifecycleType(dataModelResponse.right().value()), interfaceDef.getType());
					eitherResult = Either.right(responseFormat);
					stopDao = true;
				}

			} else {
				createdInterfaces.add(dataModelResponse.left().value());
			}
			if (!interfaceItr.hasNext()) {
				log.info("lifecycle types were created successfully!!!");
			}
		}
		return eitherResult;
	}

	private InterfaceDefinition createLifecycleType(String interfaceDefinition, Map<String, Object> toscaJson) {
		InterfaceDefinition interfaceDef = new InterfaceDefinition();
		interfaceDef.setType(interfaceDefinition);

		Map<String, Operation> operations = new HashMap<String, Operation>();

		for (Map.Entry<String, Object> entry : toscaJson.entrySet()) {
			Operation operation = new Operation();
			Map<String, Object> opProp = (Map<String, Object>) entry.getValue();

			operation.setDescription((String) opProp.get("description"));
			operations.put(entry.getKey(), operation);
		}
		interfaceDef.setOperations(operations);
		return interfaceDef;
	}

	public static void setLog(Logger log) {
		InterfaceLifecycleTypeImportManager.log = log;
	}

}
