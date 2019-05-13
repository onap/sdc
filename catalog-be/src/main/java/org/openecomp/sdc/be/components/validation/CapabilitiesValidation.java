/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.be.components.validation;

import fj.data.Either;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component("capabilitiesValidation")
public class CapabilitiesValidation {
    private static final Logger LOGGER = LoggerFactory.getLogger(CapabilitiesValidation.class);
    private static final String CAPABILITY_NOT_FOUND_IN_COMPONENT = "Capability not found in component {} ";
    private static final Pattern NAME_VALIDATION_REGEX_PATTERN = Pattern.compile("^[a-zA-Z0-9_.]*$");

    public Either<Boolean, ResponseFormat> validateCapabilities(
            Collection<CapabilityDefinition> capabilities,
            org.openecomp.sdc.be.model.Component component, boolean isUpdate) {

        for(CapabilityDefinition capabilityDefinition : capabilities) {
            Either<Boolean, ResponseFormat> validateCapabilityResponse = validateCapability(capabilityDefinition,
                    component, isUpdate);
            if (validateCapabilityResponse.isRight()) {
                return validateCapabilityResponse;
            }
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> validateCapability(CapabilityDefinition capabilityDefinition,
                                                               org.openecomp.sdc.be.model.Component component,
                                                               boolean isUpdate) {
        ResponseFormatManager responseFormatManager = getResponseFormatManager();

        if(isUpdate) {
            Either<Boolean, ResponseFormat> capabilityExistValidationEither
                    = isCapabilityExist(capabilityDefinition, responseFormatManager, component);
            if (capabilityExistValidationEither.isRight()) {
                return Either.right(capabilityExistValidationEither.right().value());
            }
        }
        Either<Boolean, ResponseFormat> capabilityNameValidationResponse
                = validateCapabilityName(capabilityDefinition, responseFormatManager, component, isUpdate);
        if (capabilityNameValidationResponse.isRight()) {
            return Either.right(capabilityNameValidationResponse.right().value());
        }
        Either<Boolean, ResponseFormat> capabilityTypeEmptyEither =
                isCapabilityTypeEmpty(responseFormatManager, capabilityDefinition.getType());
        if (capabilityTypeEmptyEither.isRight()) {
            return Either.right(capabilityTypeEmptyEither.right().value());
        }
        Either<Boolean, ResponseFormat> capabilityOccurrencesValidationEither =
                validateOccurrences(capabilityDefinition, responseFormatManager);
        if (capabilityOccurrencesValidationEither.isRight()) {
            return Either.right(capabilityOccurrencesValidationEither.right().value());
        }
        return Either.left(Boolean.FALSE);
    }

    private Either<Boolean, ResponseFormat> validateOccurrences(CapabilityDefinition capabilityDefinition,
                                                                ResponseFormatManager responseFormatManager) {
        String maxOccurrences = capabilityDefinition.getMaxOccurrences();
        String minOccurrences = capabilityDefinition.getMinOccurrences();
        if(maxOccurrences != null && minOccurrences != null) {
            Either<Boolean, ResponseFormat> capabilityOccurrencesValidationEither =
                    validateOccurrences(responseFormatManager, minOccurrences, maxOccurrences);
            if (capabilityOccurrencesValidationEither.isRight()) {
                return Either.right(capabilityOccurrencesValidationEither.right().value());
            }
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> isCapabilityExist(CapabilityDefinition definition,
                                                              ResponseFormatManager responseFormatManager,
                                                              org.openecomp.sdc.be.model.Component component) {
        Map<String, List<CapabilityDefinition>> componentCapabilities = component.getCapabilities();
        if(MapUtils.isEmpty(componentCapabilities)){
            LOGGER.error(CAPABILITY_NOT_FOUND_IN_COMPONENT, component.getUniqueId());
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus
                    .CAPABILITY_NOT_FOUND, component.getUniqueId());
            return Either.right(errorResponse);
        }

        List<CapabilityDefinition> capabilityDefinitionList = componentCapabilities.values()
                .stream().flatMap(Collection::stream).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(capabilityDefinitionList)){
            LOGGER.error(CAPABILITY_NOT_FOUND_IN_COMPONENT, component.getUniqueId());
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus
                    .CAPABILITY_NOT_FOUND, component.getUniqueId());
            return Either.right(errorResponse);
        }
        boolean isCapabilityExist = capabilityDefinitionList.stream().anyMatch(capabilityDefinition ->
                capabilityDefinition.getUniqueId().equalsIgnoreCase(definition.getUniqueId()));

        if(!isCapabilityExist) {
            LOGGER.error(CAPABILITY_NOT_FOUND_IN_COMPONENT, component.getUniqueId());
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus
                    .CAPABILITY_NOT_FOUND, component.getUniqueId());
            return Either.right(errorResponse);
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> validateCapabilityName(CapabilityDefinition capabilityDefinition,
                                                                   ResponseFormatManager responseFormatManager,
                                                                   org.openecomp.sdc.be.model.Component component,
                                                                   boolean isUpdate) {
        Either<Boolean, ResponseFormat> capabilityNameEmptyEither =
                isCapabilityNameEmpty(responseFormatManager, capabilityDefinition.getName());
        if (capabilityNameEmptyEither.isRight()) {
            return Either.right(capabilityNameEmptyEither.right().value());
        }

        Either<Boolean, ResponseFormat> capabilityNameRegexValidationResponse =
                isCapabilityNameRegexValid(responseFormatManager, capabilityDefinition.getName());
        if (capabilityNameRegexValidationResponse.isRight()) {
            return Either.right(capabilityNameRegexValidationResponse.right().value());
        }

        Either<Boolean, ResponseFormat> operationTypeUniqueResponse
                = validateCapabilityNameUnique(capabilityDefinition, component, isUpdate );
        if(operationTypeUniqueResponse.isRight()) {
            return Either.right(operationTypeUniqueResponse.right().value());
        }
        if (!operationTypeUniqueResponse.left().value()) {
            LOGGER.error("Capability name  {} already in use ", capabilityDefinition.getName());
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus
                    .CAPABILITY_NAME_ALREADY_IN_USE, capabilityDefinition.getName());
            return Either.right(errorResponse);
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> isCapabilityNameEmpty(ResponseFormatManager responseFormatManager,
                                                                  String capabilityName) {
        if (StringUtils.isEmpty(capabilityName)) {
            LOGGER.error("Capability Name is mandatory");
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus.CAPABILITY_NAME_MANDATORY);
            return Either.right(errorResponse);
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> isCapabilityTypeEmpty(ResponseFormatManager responseFormatManager,
                                                                  String capabilityType) {
        if (StringUtils.isEmpty(capabilityType)) {
            LOGGER.error("Capability type is mandatory");
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus.CAPABILITY_TYPE_MANDATORY);
            return Either.right(errorResponse);
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> validateOccurrences	(ResponseFormatManager responseFormatManager,
                                                                    String minOccurrences, String maxOccurrences ) {
        try {
            if (StringUtils.isNotEmpty(maxOccurrences) && "UNBOUNDED".equalsIgnoreCase(maxOccurrences)
                    && Integer.parseInt(minOccurrences) >= 0) {
                return Either.left(Boolean.TRUE);
            } else if (Integer.parseInt(minOccurrences) < 0) {
                LOGGER.debug("Invalid occurrences format.low_bound occurrence negative {}", minOccurrences);
                ResponseFormat responseFormat = responseFormatManager.getResponseFormat(ActionStatus.INVALID_OCCURRENCES);
                return Either.right(responseFormat);
            } else if (Integer.parseInt(maxOccurrences) < Integer.parseInt(minOccurrences)) {
                LOGGER.error("Capability maxOccurrences should be greater than minOccurrences");
                ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus
                        .MAX_OCCURRENCES_SHOULD_BE_GREATER_THAN_MIN_OCCURRENCES);
                return Either.right(errorResponse);
            }
        } catch (NumberFormatException ex) {
            LOGGER.debug("Invalid occurrences. Only Integer allowed");
            ResponseFormat responseFormat = responseFormatManager.getResponseFormat(ActionStatus.INVALID_OCCURRENCES);
            return Either.right(responseFormat);
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> validateCapabilityNameUnique(CapabilityDefinition capabilityDefinition,
                                                                         org.openecomp.sdc.be.model.Component component,
                                                                         boolean isUpdate) {
        boolean isCapabilityNameUnique = false;
        Map<String, List<CapabilityDefinition>> componentCapabilities = component.getCapabilities();
        if(MapUtils.isEmpty(componentCapabilities)){
            return Either.left(true);
        }
        List<CapabilityDefinition> capabilityDefinitionList = componentCapabilities.values()
                .stream().flatMap(Collection::stream).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(capabilityDefinitionList)){
            return Either.left(true);
        }
        Map<String, String> capabilityNameMap = new HashMap<>();
        capabilityDefinitionList.forEach(capability -> capabilityNameMap.put(capability.getUniqueId(), capability.getName()));

        if (!capabilityNameMap.values().contains(capabilityDefinition.getName())){
            isCapabilityNameUnique = true;
        }
        if (!isCapabilityNameUnique && isUpdate){
            List<Map.Entry<String, String>> capNamesEntries = capabilityNameMap.entrySet().stream().filter(entry ->
                    entry.getValue().equalsIgnoreCase(capabilityDefinition.getName())).collect(Collectors.toList());
            if(capNamesEntries.size() == 1 && capNamesEntries.get(0).getKey().equals(capabilityDefinition.getUniqueId())) {
                isCapabilityNameUnique = true;
            }
        }
        return Either.left(isCapabilityNameUnique);
    }

    private Either<Boolean, ResponseFormat> isCapabilityNameRegexValid(ResponseFormatManager responseFormatManager,
                                                                       String capabilityName) {
        if (!isValidCapabilityName(capabilityName)) {
            LOGGER.error("Capability name {} is invalid, Only alphanumeric chars, underscore and dot allowed", capabilityName);
            ResponseFormat errorResponse = responseFormatManager
                    .getResponseFormat(ActionStatus.INVALID_CAPABILITY_NAME, capabilityName);
            return Either.right(errorResponse);
        }
        return Either.left(Boolean.TRUE);
    }

    private boolean isValidCapabilityName(String capabilityName) {
        return NAME_VALIDATION_REGEX_PATTERN.matcher(capabilityName).matches();
    }

    protected ResponseFormatManager getResponseFormatManager() {
        return ResponseFormatManager.getInstance();
    }

}
