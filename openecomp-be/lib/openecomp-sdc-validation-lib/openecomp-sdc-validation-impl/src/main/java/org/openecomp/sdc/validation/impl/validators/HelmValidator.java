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
package org.openecomp.sdc.validation.impl.validators;

import com.google.gson.Gson;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import org.onap.config.api.ConfigurationManager;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.http.client.api.HttpRequestHandler;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.manifest.FileData.Type;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.validation.Validator;
import org.openecomp.sdc.validation.impl.util.HelmValidatorConfigReader;
import org.openecomp.sdc.validation.impl.util.HelmValidatorHttpClient;
import org.openecomp.sdc.validation.type.helmvalidator.HelmValidatorConfig;
import org.openecomp.sdc.validation.type.helmvalidator.HelmValidatorErrorResponse;
import org.openecomp.sdc.validation.type.helmvalidator.HelmValidatorResponse;

public class HelmValidator implements Validator {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelmValidator.class);
    private static final ErrorMessageCode VALIDATOR_ERROR_CODE = new ErrorMessageCode("HELM VALIDATOR");
    private static final String EXCEPTION_MESSAGE = "Could not execute file %s validation using Helm";

    private final HelmValidatorHttpClient helmValidatorHttpClient;
    private final HelmValidatorConfig helmValidatorConfig;

    public HelmValidator() {
        this(new HelmValidatorHttpClient(HttpRequestHandler.get()),
            new HelmValidatorConfigReader(ConfigurationManager.lookup()).getHelmValidatorConfig());
    }

    HelmValidator(HelmValidatorHttpClient helmValidatorHttpClient, HelmValidatorConfig helmValidatorConfig) {
        this.helmValidatorHttpClient = helmValidatorHttpClient;
        this.helmValidatorConfig = helmValidatorConfig;
    }

    @Override
    public void validate(GlobalValidationContext globalContext) {
        if (helmValidatorConfig.isEnabled()) {
            Set<String> manifestFiles = GlobalContextUtil.findFilesByType(globalContext, Type.HELM);
            manifestFiles.forEach(file -> tryValidateSingleChart(globalContext, file));
        }
    }

    private void tryValidateSingleChart(GlobalValidationContext globalContext, String fileName) {
        Optional<InputStream> fileContent = globalContext.getFileContent(fileName);
        if (fileContent.isPresent()) {
            try {
                validateSingleHelmChart(fileName, fileContent.get().readAllBytes(), globalContext);
            } catch (Exception exception) {
                String validationErrorMessage = String.format(EXCEPTION_MESSAGE, fileName);
                LOGGER.error(validationErrorMessage + " exception: " + exception.getMessage());
                addError(fileName, globalContext, validationErrorMessage, ErrorLevel.WARNING);
            }
        } else {
            LOGGER.debug("File content is not present " + fileName);
        }
    }

    private void validateSingleHelmChart(String fileName, byte[] file, GlobalValidationContext globalContext)
        throws Exception {
        var httpResponse = helmValidatorHttpClient.execute(fileName, file, helmValidatorConfig);
        if (httpResponse.getStatusCode() == 200) {
            var helmValidatorResponse = new Gson()
                .fromJson(httpResponse.getResponse(), HelmValidatorResponse.class);
            helmValidatorResponse.getRenderErrors().forEach(error ->
                addError(fileName, globalContext, error, ErrorLevel.ERROR));
            helmValidatorResponse.getLintError().forEach(lintError ->
                addError(fileName, globalContext, lintError, ErrorLevel.WARNING));
            helmValidatorResponse.getLintWarning().forEach(lintWarning ->
                addError(fileName, globalContext, lintWarning, ErrorLevel.WARNING));
        } else {
            var errorResponse = new Gson().fromJson(httpResponse.getResponse(), HelmValidatorErrorResponse.class);
            addError(fileName, globalContext, errorResponse.getMessage(), ErrorLevel.WARNING);
        }
    }

    private void addError(String fileName, GlobalValidationContext globalContext, String error, ErrorLevel level) {
        globalContext.addMessage(fileName, level, ErrorMessagesFormatBuilder
            .getErrorWithParameters(VALIDATOR_ERROR_CODE, error, fileName));
    }

}
