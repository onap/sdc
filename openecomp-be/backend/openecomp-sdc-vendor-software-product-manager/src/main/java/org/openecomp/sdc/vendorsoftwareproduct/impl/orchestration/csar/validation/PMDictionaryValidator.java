/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 Nokia. All rights reserved.
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
package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.onap.validation.yaml.YamlContentValidator;
import org.onap.validation.yaml.error.YamlDocumentValidationError;

public class PMDictionaryValidator {

    public void validate(Stream<byte[]> pmDictionaryFiles, Consumer<String> errorReporter) {
        pmDictionaryFiles.map(this::validate).flatMap(Collection::stream).forEach(errorReporter);
    }

    private List<String> validate(byte[] fileContent) {
        List<String> errors = new ArrayList<>();
        try {
            List<YamlDocumentValidationError> validationErrors = new YamlContentValidator().validate(fileContent);
            validationErrors.stream().map(this::formatErrorMessage).forEach(errors::add);
        } catch (Exception e) {
            errors.add(e.getMessage());
        }
        return errors;
    }

    private String formatErrorMessage(YamlDocumentValidationError error) {
        return String.format("Document number: %d, Path: %s, Message: %s", error.getYamlDocumentNumber(), error.getPath(), error.getMessage());
    }
}
