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

import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Operation;
import org.openecomp.sdc.be.model.operations.api.IInterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.stereotype.Component;

@Component("interfaceLifecycleTypeImportManager")
public class InterfaceLifecycleTypeImportManager {

    @Resource
    private IInterfaceLifecycleOperation interfaceLifecycleOperation;

    @Resource
    private ComponentsUtils componentsUtils;
    @Resource
    private CommonImportManager commonImportManager;

    private static final Logger log = Logger.getLogger(InterfaceLifecycleTypeImportManager.class);

    public Either<List<InterfaceDefinition>, ResponseFormat> createLifecycleTypes(String interfaceLifecycleTypesYml) {

        Either<List<InterfaceDefinition>, ActionStatus> interfaces = createInterfaceTypeFromYml(interfaceLifecycleTypesYml);
        if (interfaces.isRight()) {
            ActionStatus status = interfaces.right().value();
            ResponseFormat responseFormat = componentsUtils.getResponseFormatByGroupType(status, null);
            return Either.right(responseFormat);
        }
        return createInterfacesByDao(interfaces.left().value());

    }

    private Either<List<InterfaceDefinition>, ActionStatus> createInterfaceTypeFromYml(final String interfaceTypesYml) {
        return commonImportManager.createElementTypesFromYml(interfaceTypesYml, this::createInterfaceDefinition);

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

    private InterfaceDefinition createInterfaceDefinition(final String interfaceDefinition,
                                                          final Map<String, Object> toscaJson) {
        final InterfaceDefinition interfaceDef = new InterfaceDefinition();
        interfaceDef.setType(interfaceDefinition);
        final Object descriptionObj = toscaJson.get(ToscaTagNamesEnum.DESCRIPTION.getElementName());
        if (descriptionObj instanceof String) {
            interfaceDef.setDescription((String) descriptionObj);
        }
        final Object derivedFromObj = toscaJson.get(ToscaTagNamesEnum.DERIVED_FROM.getElementName());
        if (derivedFromObj instanceof String) {
            interfaceDef.setDerivedFrom((String) derivedFromObj);
        }
        final Object versionObj = toscaJson.get(ToscaTagNamesEnum.VERSION.getElementName());
        if (versionObj instanceof String) {
            interfaceDef.setVersion((String) versionObj);
        }

        final Object metadataObj = toscaJson.get(ToscaTagNamesEnum.METADATA.getElementName());
        if (metadataObj instanceof Map) {
            interfaceDef.setToscaPresentationValue(JsonPresentationFields.METADATA, metadataObj);
        }

        final Map<String, Object> operationsMap;
        if (toscaJson.containsKey(ToscaTagNamesEnum.OPERATIONS.getElementName())) {
            operationsMap = (Map<String, Object>) toscaJson.get(ToscaTagNamesEnum.OPERATIONS.getElementName());
        } else {
            final List<String> entitySchemaEntryList = Arrays
                .asList(ToscaTagNamesEnum.DERIVED_FROM.getElementName(),
                    ToscaTagNamesEnum.DESCRIPTION.getElementName(),
                    ToscaTagNamesEnum.VERSION.getElementName(),
                    ToscaTagNamesEnum.METADATA.getElementName(),
                    ToscaTagNamesEnum.INPUTS.getElementName(),
                    ToscaTagNamesEnum.NOTIFICATIONS.getElementName());
            operationsMap = toscaJson.entrySet().stream()
                .filter(interfaceEntry -> !entitySchemaEntryList.contains(interfaceEntry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        }
        interfaceDef.setOperationsMap(handleOperations(operationsMap));
        return interfaceDef;
    }

    private Map<String, Operation> handleOperations(final Map<String, Object> operationsToscaEntry) {
        if (MapUtils.isEmpty(operationsToscaEntry)) {
            return Collections.emptyMap();
        }
        return operationsToscaEntry.entrySet().stream()
            .collect(Collectors.toMap(Entry::getKey,
                operationEntry -> createOperation((Map<String, Object>) operationEntry.getValue())));
    }

    private Operation createOperation(final Map<String, Object> toscaOperationMap) {
        final Operation operation = new Operation();
        operation.setDescription((String) toscaOperationMap.get(ToscaTagNamesEnum.DESCRIPTION.getElementName()));
        return operation;
    }
}
