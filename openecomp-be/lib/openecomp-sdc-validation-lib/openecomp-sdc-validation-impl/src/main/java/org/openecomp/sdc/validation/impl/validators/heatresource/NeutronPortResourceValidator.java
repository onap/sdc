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
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.validation.ResourceValidator;
import org.openecomp.sdc.validation.ValidationContext;
import org.openecomp.sdc.validation.type.HeatResourceValidationContext;

public class NeutronPortResourceValidator implements ResourceValidator {

    private static final ErrorMessageCode ERROR_HPRODE_HPR1 = new ErrorMessageCode("HPR1");
    private static final ErrorMessageCode ERROR_HPRODE_HPR2 = new ErrorMessageCode("HPR2");
    private static final ErrorMessageCode ERROR_HPRODE_HPR3 = new ErrorMessageCode("HPR3");

    @SuppressWarnings("unchecked")
    private static void validateNovaServerPortBinding(String fileName, Map.Entry<String, Resource> resourceEntry,
                                                      HeatResourceValidationContext heatResourceValidationContext,
                                                      GlobalValidationContext globalContext) {
        Map<String, Map<String, List<String>>> portIdToPointingResources = heatResourceValidationContext.getFileLevelResourceDependencies()
            .get(HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource());
        String portResourceId = resourceEntry.getKey();
        if (MapUtils.isEmpty(portIdToPointingResources)) {
            globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                .getErrorWithParameters(ERROR_HPRODE_HPR1, Messages.PORT_NO_BIND_TO_ANY_NOVA_SERVER.getErrorMessage(), portResourceId));
            return;
        }
        Map<String, List<String>> pointingResourcesToCurrPort = portIdToPointingResources.get(portResourceId);
        checkPortBindingFromMap(fileName, portResourceId, pointingResourcesToCurrPort, globalContext);
    }

    private static void checkPortBindingFromMap(String fileName, String portResourceId, Map<String, List<String>> resourcesPointingToCurrPort,
                                                GlobalValidationContext globalContext) {
        List<String> pointingNovaServers = MapUtils.isEmpty(resourcesPointingToCurrPort) ? new ArrayList<>()
            : resourcesPointingToCurrPort.get(HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource());
        handleErrorEventsForPortBinding(fileName, portResourceId, globalContext, pointingNovaServers);
    }

    private static void handleErrorEventsForPortBinding(String fileName, String portResourceId, GlobalValidationContext globalContext,
                                                        List<String> pointingNovaServers) {
        if (isThereMoreThanOneBindFromNovaToPort(pointingNovaServers)) {
            globalContext.addMessage(fileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                .getErrorWithParameters(ERROR_HPRODE_HPR2, Messages.MORE_THAN_ONE_BIND_FROM_NOVA_TO_PORT.getErrorMessage(), portResourceId));
        }
        if (isNoNovaPointingToPort(pointingNovaServers)) {
            globalContext.addMessage(fileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                .getErrorWithParameters(ERROR_HPRODE_HPR3, Messages.PORT_NO_BIND_TO_ANY_NOVA_SERVER.getErrorMessage(), portResourceId));
        }
    }

    private static boolean isNoNovaPointingToPort(List<String> pointingNovaServers) {
        return CollectionUtils.isEmpty(pointingNovaServers);
    }

    private static boolean isThereMoreThanOneBindFromNovaToPort(List<String> pointingNovaServers) {
        return CollectionUtils.isNotEmpty(pointingNovaServers) && pointingNovaServers.size() > 1;
    }

    @Override
    public void validate(String fileName, Map.Entry<String, Resource> resourceEntry, GlobalValidationContext globalContext,
                         ValidationContext validationContext) {
        validateNovaServerPortBinding(fileName, resourceEntry, (HeatResourceValidationContext) validationContext, globalContext);
    }
}
