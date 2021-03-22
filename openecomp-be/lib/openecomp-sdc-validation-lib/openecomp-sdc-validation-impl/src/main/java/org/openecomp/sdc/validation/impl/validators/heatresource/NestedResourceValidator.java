/*
 * Copyright © 2016-2017 European Support Limited
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
package org.openecomp.sdc.validation.impl.validators.heatresource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.validation.ResourceValidator;
import org.openecomp.sdc.validation.ValidationContext;
import org.openecomp.sdc.validation.impl.util.HeatValidationService;

/**
 * Created by TALIO on 2/22/2017.
 */
public class NestedResourceValidator implements ResourceValidator {

    private static final ErrorMessageCode ERROR_CODE_HNR1 = new ErrorMessageCode("HNR1");
    private static final ErrorMessageCode ERROR_CODE_HNR2 = new ErrorMessageCode("HNR2");
    private static final ErrorMessageCode ERROR_CODE_HNR3 = new ErrorMessageCode("HNR3");
    private static final ErrorMessageCode ERROR_CODE_HNR4 = new ErrorMessageCode("HNR4");

    private static void handleNestedResourceType(String fileName, String resourceName, Resource resource, Optional<String> indexVarValue,
                                                 GlobalValidationContext globalContext) {
        validateAllPropertiesMatchNestedParameters(fileName, resourceName, resource, indexVarValue, globalContext);
        validateLoopsOfNestingFromFile(fileName, resource.getType(), globalContext);
    }

    public static void validateAllPropertiesMatchNestedParameters(String fileName, String resourceName, Resource resource,
                                                                  Optional<String> indexVarValue, GlobalValidationContext globalContext) {
        String resourceType = resource.getType();
        if (globalContext.getFileContextMap().containsKey(resourceType)) {
            Set<String> propertiesNames = resource.getProperties() == null ? null : resource.getProperties().keySet();
            if (CollectionUtils.isNotEmpty(propertiesNames)) {
                globalContext.setMessageCode(ERROR_CODE_HNR3);
                HeatValidationService
                    .checkNestedParametersNoMissingParameterInNested(fileName, resourceType, resourceName, propertiesNames, globalContext);
                globalContext.setMessageCode(ERROR_CODE_HNR4);
                HeatValidationService
                    .checkNestedInputValuesAlignWithType(fileName, resourceType, resourceName, resource, indexVarValue, globalContext);
            }
        } else {
            globalContext.addMessage(resourceType, ErrorLevel.ERROR,
                ErrorMessagesFormatBuilder.getErrorWithParameters(ERROR_CODE_HNR1, Messages.MISSING_NESTED_FILE.getErrorMessage(), resourceType));
        }
    }

    public static void validateLoopsOfNestingFromFile(String fileName, String resourceType, GlobalValidationContext globalContext) {
        List<String> filesInLoop = new ArrayList<>(Collections.singletonList(fileName));
        if (HeatValidationService.isNestedLoopExistInFile(fileName, resourceType, filesInLoop, globalContext)) {
            globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                .getErrorWithParameters(ERROR_CODE_HNR2, Messages.NESTED_LOOP.getErrorMessage(), HeatValidationService.drawFilesLoop(filesInLoop)));
        }
    }

    @Override
    public void validate(String fileName, Map.Entry<String, Resource> resourceEntry, GlobalValidationContext globalContext,
                         ValidationContext validationContext) {
        handleNestedResourceType(fileName, resourceEntry.getKey(), resourceEntry.getValue(), Optional.empty(), globalContext);
    }
}
