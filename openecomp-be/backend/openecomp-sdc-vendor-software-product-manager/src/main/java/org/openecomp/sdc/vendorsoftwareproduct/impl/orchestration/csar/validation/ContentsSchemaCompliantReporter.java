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

import org.onap.validation.yaml.YamlContentValidator;
import org.onap.validation.yaml.error.YamlDocumentValidationError;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.OnboardingPackageContentHandler;

import java.util.ArrayList;
import java.util.List;

class ContentsSchemaCompliantReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContentsSchemaCompliantReporter.class);

    List<String> report(List<String> filteredPathsToPMDict, OnboardingPackageContentHandler contentHandler) {
        List<String> errorMessages = new ArrayList<>();
        filteredPathsToPMDict.forEach(pmDictPath -> {
            validate(contentHandler, errorMessages, pmDictPath);
        });
        return errorMessages;
    }

    private void validate(OnboardingPackageContentHandler contentHandler, List<String> errors, String pmDictPath) {
        try {
            List<YamlDocumentValidationError> validationErrors = new YamlContentValidator().validate(contentHandler.getFileContent(pmDictPath));
            validationErrors.stream()
                    .map(error -> error.getMessage())
                    .forEach(errors::add);
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage(), exception);
            errors.add(exception.getMessage());
        }
    }

}
