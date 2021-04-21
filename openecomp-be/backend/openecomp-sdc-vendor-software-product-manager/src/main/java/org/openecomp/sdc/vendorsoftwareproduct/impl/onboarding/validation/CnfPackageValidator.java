/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nokia
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.validation;

import static org.openecomp.sdc.common.errors.Messages.MANIFEST_VALIDATION_HELM_IS_BASE_MISSING;
import static org.openecomp.sdc.common.errors.Messages.MANIFEST_VALIDATION_HELM_IS_BASE_NOT_SET;
import static org.openecomp.sdc.common.errors.Messages.MANIFEST_VALIDATION_HELM_IS_BASE_NOT_UNIQUE;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.vendorsoftwareproduct.types.helmvalidator.HelmValidatorConfig;
import org.openecomp.sdc.vendorsoftwareproduct.types.helmvalidator.HelmValidatorErrorResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.helmvalidator.HelmValidatorResponse;

public class CnfPackageValidator {

    private final HelmValidatorHttpClient helmValidatorHttpClient;
    private final HelmValidatorConfig helmValidatorConfig;

    public CnfPackageValidator(HelmValidatorHttpClient helmValidationHttpClient,
        HelmValidatorConfig helmValidatorConfig) {
        this.helmValidatorHttpClient = helmValidationHttpClient;
        this.helmValidatorConfig = helmValidatorConfig;
    }

    public CnfValidatorResult validateHelmPackage(List<FileData> modules, FileContentHandler packageContent) {
        CnfValidatorResult validatorResult = new CnfValidatorResult();

        List<String> manifestErrorMessages = validateManifest(modules);
        validatorResult.addErrorMessages(manifestErrorMessages);

        if (helmValidatorConfig.isEnabled()) {
            var files = getHelmChartFiles(modules, packageContent);
            files.forEach((name, file) -> validateSingleHelmChart(name, file, validatorResult));
        }

        return validatorResult;
    }

    private void validateSingleHelmChart(String fileName, byte[] file, CnfValidatorResult validatorResult) {
        try {
            var httpResponse = helmValidatorHttpClient.execute(fileName, file, helmValidatorConfig);
            if (httpResponse.getStatusCode() == 200) {
                var helmValidatorResponse = new Gson().fromJson(httpResponse.getResponse(), HelmValidatorResponse.class);
                validatorResult.addErrorMessages(helmValidatorResponse.getRenderErrors());
                validatorResult.addWarningMessages(helmValidatorResponse.getLintError());
                validatorResult.addWarningMessages(helmValidatorResponse.getLintWarning());
                validatorResult.setDeployable(helmValidatorResponse.getIsDeployable());
            } else {
                var errorResponse = new Gson().fromJson(httpResponse.getResponse(), HelmValidatorErrorResponse.class);
                validatorResult.addWarning(errorResponse.getMessage());
            }
        }
        catch (Exception exception){
            validatorResult.addWarning(String.format("Could not execute file %s validation using Helm", fileName));
        }
    }

    private Map<String, byte[]> getHelmChartFiles(List<FileData> modules, FileContentHandler packageContent) {
        return modules.stream()
            .filter(fd -> Objects.nonNull(packageContent.getFileContent(fd.getFile())))
            .collect(Collectors.toMap(FileData::getFile, fd -> packageContent.getFileContent(fd.getFile())));
    }

    private List<String> validateManifest(List<FileData> modules) {
        List<String> messages = new ArrayList<>();
        if (modules != null && !modules.isEmpty()) {
            Stats stats = calculateStats(modules);
            messages.addAll(createErrorMessages(stats));
        }
        return messages;
    }

    private Stats calculateStats(List<FileData> modules) {
        Stats stats = new Stats();
        for (FileData mod : modules) {
            if (mod.getBase() == null) {
                stats.withoutBase++;
            } else if (mod.getBase()) {
                stats.base++;
            }
        }
        return stats;
    }

    private List<String> createErrorMessages(Stats result) {
        List<String> messages = new ArrayList<>();
        if (result.withoutBase > 0) {
            messages.add(MANIFEST_VALIDATION_HELM_IS_BASE_MISSING.formatMessage(result.withoutBase));
        }
        if (result.base == 0) {
            messages.add(MANIFEST_VALIDATION_HELM_IS_BASE_NOT_SET.getErrorMessage());
        } else if (result.base > 1) {
            messages.add(MANIFEST_VALIDATION_HELM_IS_BASE_NOT_UNIQUE.getErrorMessage());
        }
        return messages;
    }

    private static class Stats {

        private int base = 0;
        private int withoutBase = 0;
    }

}
