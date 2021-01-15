/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 Nokia Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.validation.impl.validators;

import io.vavr.control.Option;
import io.vavr.control.Try;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.onap.validation.yaml.YamlContentValidator;
import org.onap.validation.yaml.error.YamlDocumentValidationError;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.heat.services.manifest.ManifestUtil;
import org.openecomp.sdc.validation.Validator;
import org.openecomp.sdc.validation.util.ValidationUtil;

public class PmDictionaryValidator implements Validator {

    private static final ErrorMessageCode PM_DICT_ERROR_CODE = new ErrorMessageCode("PM_DICT");

    @Override
    public void validate(GlobalValidationContext globalContext) {
        Map<String, FileData.Type> filesWithTypes = readAllFilesWithTypes(globalContext);
        Set<String> pmDictionaryFiles = getPmDictionaryFiles(filesWithTypes);
        validatePmDictionaryFiles(globalContext, pmDictionaryFiles);
    }

    private void validatePmDictionaryFiles(GlobalValidationContext globalContext, Set<String> pmDictionaryFiles) {
        pmDictionaryFiles.stream()
            .map(fileName -> new ValidationHelper(globalContext, fileName))
            .forEach(ValidationHelper::validate);
    }

    private Set<String> getPmDictionaryFiles(Map<String, FileData.Type> filesWithTypes) {
        return filesWithTypes.entrySet().stream()
                .filter(isPmDictionaryType())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private Map<String, FileData.Type> readAllFilesWithTypes(GlobalValidationContext globalContext) {
        ManifestContent manifestContent = ValidationUtil.validateManifest(globalContext);
        return ManifestUtil.getFileTypeMap(manifestContent);
    }

    private Predicate<Map.Entry<String, FileData.Type>> isPmDictionaryType() {
        return entry -> entry.getValue()
                .equals(FileData.Type.PM_DICTIONARY);
    }

    private static class ValidationHelper {

        private final GlobalValidationContext globalContext;
        private final String fileName;

        private ValidationHelper(GlobalValidationContext globalContext, String fileName) {
            this.globalContext = globalContext;
            this.fileName = fileName;
        }

        public void validate() {
            Option.ofOptional(globalContext.getFileContent(fileName))
                .peek(this::validateFileContent)
                .onEmpty(() -> addErrorToContext(formatMessage("File is empty")));
        }

        private void validateFileContent(InputStream inputStream) {
            Try.of(inputStream::readAllBytes)
                .mapTry(fileContent -> new YamlContentValidator().validate(fileContent))
                .onSuccess(this::reportValidationErrorsIfPresent)
                .onFailure(e -> addErrorToContext(formatMessage(e.getMessage())));
        }

        private void reportValidationErrorsIfPresent(List<YamlDocumentValidationError> validationErrors) {
            validationErrors.stream()
                .map(this::prepareValidationMessage)
                .forEach(this::addErrorToContext);
        }

        private String prepareValidationMessage(YamlDocumentValidationError error) {
            final String errorMessage = String.format("Document Number: %s, Path: %s, Problem: %s",
                error.getYamlDocumentNumber(),
                error.getPath(),
                error.getMessage()
            );
            return formatMessage(errorMessage);
        }

        private String formatMessage(String message) {
            return ErrorMessagesFormatBuilder
                .getErrorWithParameters(PM_DICT_ERROR_CODE, message);
        }

        private void addErrorToContext(String message) {
            globalContext.addMessage(fileName, ErrorLevel.ERROR, message);
        }
    }
}
