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
import org.openecomp.sdc.tosca.csar.Manifest;
import org.openecomp.sdc.tosca.csar.SOL004ManifestOnboarding;
import org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.OnboardingPackageContentHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.openecomp.sdc.be.config.NonManoArtifactType.ONAP_PM_DICTIONARY;

class PMDictionaryValidator {

    private String etsiEntryManifestFilePath;
    private OnboardingPackageContentHandler contentHandler;
    private ValidatorUtils validatorUtils;
    private Manifest onboardingManifest;


    PMDictionaryValidator(String etsiEntryManifestFilePath, OnboardingPackageContentHandler contentHandler) {
        this.etsiEntryManifestFilePath = etsiEntryManifestFilePath;
        this.contentHandler = contentHandler;
        validatorUtils = new ValidatorUtils();
    }

    List<String> validateContentAgainstSchema() {
        prepareOnboardingManifest();
        final List<String> filteredPathsToPMDict = getPathsToPmDict();
        return validatePmDict(filteredPathsToPMDict);
    }

    private void prepareOnboardingManifest() {
        onboardingManifest = new SOL004ManifestOnboarding();
        onboardingManifest.parse(contentHandler.getFileContentAsStream(etsiEntryManifestFilePath));
    }

    private List<String> getPathsToPmDict() {
        final Map<String, List<String>> nonManoSources = onboardingManifest.getNonManoSources();
        final List<String> pathsToPMDict = nonManoSources.getOrDefault(ONAP_PM_DICTIONARY.getType(), new ArrayList<>());
        return validatorUtils.filterSources(pathsToPMDict);
    }

    List<String> validatePmDict(List<String> filteredPathsToPMDict) {
        List<String> errors = new ArrayList<>();
        filteredPathsToPMDict.forEach(pmDictPath -> {
            try {
                List<YamlDocumentValidationError> err = new YamlContentValidator().validate(contentHandler.getFileContent(pmDictPath));
                err.forEach(error -> errors.add(error.getMessage()));

            } catch (Exception exception) {
                exception.printStackTrace();
                errors.add(exception.getMessage());
            }

        });
        return errors;
    }

}
