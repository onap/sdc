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
import org.openecomp.sdc.be.model.RequirementDefinition;
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

@Component("requirementValidation")
public class RequirementValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequirementValidation.class);
    private static final String REQUIREMENT_NOT_FOUND_IN_COMPONENT = "Requirement not found in component {} ";
    private static final Pattern NAME_VALIDATION_REGEX_PATTERN = Pattern.compile("^[a-zA-Z0-9_.]*$");

    public Either<Boolean, ResponseFormat> validateRequirements(
            Collection<RequirementDefinition> requirements,
            org.openecomp.sdc.be.model.Component component, boolean isUpdate) {

        for(RequirementDefinition requirementDefinition : requirements) {
            Either<Boolean, ResponseFormat> requirementValidationResponse
                    = validateRequirement(requirementDefinition, component, isUpdate);
            if (requirementValidationResponse.isRight()) {
                return requirementValidationResponse;
            }
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> validateRequirement(
            RequirementDefinition requirementDefinition,
            org.openecomp.sdc.be.model.Component component,
            boolean isUpdate) {
        ResponseFormatManager responseFormatManager = getResponseFormatManager();
        if(isUpdate) {
            Either<Boolean, ResponseFormat> requirementExistValidationEither
                    = isRequirementExist(requirementDefinition, responseFormatManager, component);
            if (requirementExistValidationEither.isRight()) {
                return Either.right(requirementExistValidationEither.right().value());
            }
        }

        Either<Boolean, ResponseFormat> requirementNameValidationResponse
                = isRequirementNameValid(requirementDefinition, responseFormatManager,
                component, isUpdate);
        if (requirementNameValidationResponse.isRight()) {
            return Either.right(requirementNameValidationResponse.right().value());
        }

        Either<Boolean, ResponseFormat> requirementTypeEmptyEither =
                isRequirementCapabilityEmpty(responseFormatManager,
                        requirementDefinition.getCapability());
        if (requirementTypeEmptyEither.isRight()) {
            return Either.right(requirementTypeEmptyEither.right().value());
        }
        Either<Boolean, ResponseFormat> requirementOccurrencesValidationEither =
                validateOccurrences(requirementDefinition, responseFormatManager);
        if (requirementOccurrencesValidationEither.isRight()) {
            return Either.right(requirementOccurrencesValidationEither.right().value());
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> validateOccurrences(RequirementDefinition requirementDefinition,
                                                                ResponseFormatManager responseFormatManager) {
        String maxOccurrences = requirementDefinition.getMaxOccurrences();
        String minOccurrences = requirementDefinition.getMinOccurrences();
        if(maxOccurrences != null && minOccurrences !=null) {
            Either<Boolean, ResponseFormat> requirementOccurrencesValidationEither =
                    validateOccurrences(responseFormatManager, minOccurrences,
                            maxOccurrences);
            if (requirementOccurrencesValidationEither.isRight()) {
                return Either.right(requirementOccurrencesValidationEither.right().value());
            }
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> isRequirementNameValid(
            RequirementDefinition requirementDefinition,
            ResponseFormatManager responseFormatManager,
            org.openecomp.sdc.be.model.Component component, boolean isUpdate) {
        Either<Boolean, ResponseFormat> requirementNameEmptyEither =
                isRequirementNameEmpty(responseFormatManager, requirementDefinition.getName());
        if (requirementNameEmptyEither.isRight()) {
            return Either.right(requirementNameEmptyEither.right().value());
        }

        Either<Boolean, ResponseFormat> requirementNameRegexValidationResponse =
                isRequirementNameRegexValid(responseFormatManager, requirementDefinition.getName());
        if (requirementNameRegexValidationResponse.isRight()) {
            return Either.right(requirementNameRegexValidationResponse.right().value());
        }

        Either<Boolean, ResponseFormat> requirementNameUniqueResponse
                = validateRequirementNameUnique(requirementDefinition,
                component, isUpdate );
        if(requirementNameUniqueResponse.isRight()) {
            return Either.right(requirementNameUniqueResponse.right().value());
        }
        if (!requirementNameUniqueResponse.left().value()) {
            LOGGER.error("Requirement name  {} already in use ", requirementDefinition.getName());
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus
                    .REQUIREMENT_NAME_ALREADY_IN_USE, requirementDefinition.getName());
            return Either.right(errorResponse);
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> isRequirementNameEmpty(
            ResponseFormatManager responseFormatManager,
            String requirementName) {
        if (StringUtils.isEmpty(requirementName)) {
            LOGGER.error("Requirement Name is mandatory");
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus
                    .REQUIREMENT_NAME_MANDATORY);
            return Either.right(errorResponse);
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> validateOccurrences	(
            ResponseFormatManager responseFormatManager,
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
                LOGGER.error("Requirement maxOccurrences should be greater than minOccurrences");
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
    private Either<Boolean, ResponseFormat> isRequirementCapabilityEmpty(
            ResponseFormatManager responseFormatManager,
            String requirementCapability) {
        if (StringUtils.isEmpty(requirementCapability)) {
            LOGGER.error("Requirement capability is mandatory");
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus
                    .REQUIREMENT_CAPABILITY_MANDATORY);
            return Either.right(errorResponse);
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> validateRequirementNameUnique(
            RequirementDefinition requirementDefinition,
            org.openecomp.sdc.be.model.Component component,
            boolean isUpdate) {
        boolean isRequirementNameUnique = false;

        Map<String, List<RequirementDefinition>> componentRequirements = component.getRequirements();
        if(MapUtils.isEmpty(componentRequirements)){
            return Either.left(true);
        }
        List<RequirementDefinition> requirementDefinitionList = componentRequirements.values()
                .stream().flatMap(Collection::stream).collect(Collectors.toList());

        if(CollectionUtils.isEmpty(requirementDefinitionList)){
            return Either.left(true);
        }

        Map<String, String> requirementNameMap = new HashMap<>();
        requirementDefinitionList.forEach(requirement -> requirementNameMap
                .put(requirement.getUniqueId(), requirement.getName()));

        if (!requirementNameMap.values().contains(requirementDefinition.getName())){
            isRequirementNameUnique = true;
        }
        if (!isRequirementNameUnique && isUpdate){
            List<Map.Entry<String, String>> reqNamesEntry = requirementNameMap.entrySet()
                    .stream().filter(entry -> entry.getValue().equalsIgnoreCase(requirementDefinition.getName()))
                    .collect(Collectors.toList());
            if(reqNamesEntry.size() == 1 && reqNamesEntry.get(0).getKey()
                    .equals(requirementDefinition.getUniqueId())) {
                isRequirementNameUnique = true;
            }
        }
        return Either.left(isRequirementNameUnique);
    }

    private Either<Boolean, ResponseFormat> isRequirementExist(
            RequirementDefinition definition,
            ResponseFormatManager responseFormatManager,
            org.openecomp.sdc.be.model.Component component) {
        Map<String, List<RequirementDefinition>> componentRequirements = component.getRequirements();
        if(MapUtils.isEmpty(componentRequirements)){
            LOGGER.error(REQUIREMENT_NOT_FOUND_IN_COMPONENT, component.getUniqueId());
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus
                    .REQUIREMENT_NOT_FOUND, component.getUniqueId());
            return Either.right(errorResponse);
        }

        List<RequirementDefinition> requirementDefinitionList = componentRequirements.values()
                .stream().flatMap(Collection::stream).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(requirementDefinitionList)){
            LOGGER.error(REQUIREMENT_NOT_FOUND_IN_COMPONENT, component.getUniqueId());
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus
                    .REQUIREMENT_NOT_FOUND, component.getUniqueId());
            return Either.right(errorResponse);
        }
        boolean isRequirementExist = requirementDefinitionList.stream()
                .anyMatch(requirementDefinition -> requirementDefinition.getUniqueId()
                        .equalsIgnoreCase(definition.getUniqueId()));

        if(!isRequirementExist) {
            LOGGER.error(REQUIREMENT_NOT_FOUND_IN_COMPONENT, component.getUniqueId());
            ResponseFormat errorResponse = responseFormatManager.getResponseFormat(ActionStatus
                    .REQUIREMENT_NOT_FOUND, component.getUniqueId());
            return Either.right(errorResponse);
        }
        return Either.left(Boolean.TRUE);
    }

    private Either<Boolean, ResponseFormat> isRequirementNameRegexValid(ResponseFormatManager responseFormatManager,
                                                                        String requirementName) {
        if (!isValidRequirementName(requirementName)) {
            LOGGER.error("Requirement name {} is invalid, Only alphanumeric chars, underscore and dot allowed",
                    requirementName);
            ResponseFormat errorResponse = responseFormatManager
                    .getResponseFormat(ActionStatus.INVALID_REQUIREMENT_NAME, requirementName);
            return Either.right(errorResponse);
        }
        return Either.left(Boolean.TRUE);
    }

    private boolean isValidRequirementName(String requirementName) {
        return NAME_VALIDATION_REGEX_PATTERN.matcher(requirementName).matches();
    }

    protected ResponseFormatManager getResponseFormatManager() {
        return ResponseFormatManager.getInstance();
    }

}

