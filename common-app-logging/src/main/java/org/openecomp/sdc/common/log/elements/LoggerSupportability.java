/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2020 Nordix Foundation
 * ================================================================================
 */
package org.openecomp.sdc.common.log.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.openecomp.sdc.common.log.api.ILogConfiguration;
import org.openecomp.sdc.common.log.api.ILogFieldsHandler;
import org.openecomp.sdc.common.log.enums.LogLevel;
import org.openecomp.sdc.common.log.enums.LogMarkers;
import org.openecomp.sdc.common.log.enums.LoggerSupportabilityActions;
import org.openecomp.sdc.common.log.enums.StatusCode;
import org.slf4j.Logger;
import org.slf4j.MarkerFactory;

public class LoggerSupportability extends LoggerBase {

    private static ArrayList<String> mandatoryFields = new ArrayList<>(Arrays.asList(
        ILogConfiguration.MDC_SUPPORTABLITY_ACTION,
        ILogConfiguration.MDC_SUPPORTABLITY_CSAR_UUID,
        ILogConfiguration.MDC_SUPPORTABLITY_CSAR_VERSION,
        ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_NAME,
        ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_UUID,
        ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_VERSION,
        ILogConfiguration.MDC_SUPPORTABLITY_STATUS_CODE));

    public LoggerSupportability(ILogFieldsHandler ecompLogFieldsHandler, Logger logger) {
        super(ecompLogFieldsHandler, MarkerFactory.getMarker(LogMarkers.SUPPORTABILITY_MARKER.getText()),
            logger);
    }

    public static LoggerSupportability getLogger(Class<?> clazz) {
        return LoggerFactory.getMdcLogger(LoggerSupportability.class, org.slf4j.LoggerFactory.getLogger(clazz));
    }

    public static LoggerSupportability getLogger(String className) {
        return LoggerFactory.getMdcLogger(LoggerSupportability.class,
            org.slf4j.LoggerFactory.getLogger(className));
    }

    public void log(LoggerSupportabilityActions action, Map<String, String> componentMetaData, StatusCode statusCode,
                    String message, Object... params) {
        fillFieldsBeforeLogging(action, componentMetaData, statusCode);
        super.log(LogLevel.INFO, message, params);
    }

    public void log(LoggerSupportabilityActions action, StatusCode statusCode, String message, Object... params) {
        log(action, null, statusCode, message, params);
    }

    private void fillFieldsBeforeLogging(LoggerSupportabilityActions action, Map<String, String> componentMetaData,
                                         StatusCode statusCode) {
        clear();
        if (componentMetaData != null) {
            ecompLogFieldsHandler
                .setSupportablityCsarUUID(componentMetaData.get(ILogConfiguration.MDC_SUPPORTABLITY_CSAR_UUID));
            ecompLogFieldsHandler
                .setSupportablityCsarVersion(componentMetaData.get(ILogConfiguration.MDC_SUPPORTABLITY_CSAR_VERSION));
            ecompLogFieldsHandler.setSupportablityComponentName(
                componentMetaData.get(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_NAME));
            ecompLogFieldsHandler.setSupportablityComponentUUID(
                componentMetaData.get(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_UUID));
            ecompLogFieldsHandler.setSupportablityComponentVersion(
                componentMetaData.get(ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_VERSION));
        }
        ecompLogFieldsHandler.setSupportablityAction(action.getName());
        ecompLogFieldsHandler.setSupportablityStatusCode(statusCode.getStatusCode());
    }

    @Override
    public LoggerSupportability clear() {
        LogFieldsMdcHandler.getInstance().removeSupportablityAction();
        LogFieldsMdcHandler.getInstance().removeSupportablityCsarUUID();
        LogFieldsMdcHandler.getInstance().removeSupportablityCsarVersion();
        LogFieldsMdcHandler.getInstance().removeSupportablityComponentName();
        LogFieldsMdcHandler.getInstance().removeSupportablityComponentUUID();
        LogFieldsMdcHandler.getInstance().removeSupportablityComponentVersion();
        LogFieldsMdcHandler.getInstance().removeSupportablityStatusCode();
        return this;
    }


    @Override
    public List<String> getMandatoryFields() {
        return Collections.unmodifiableList(mandatoryFields);
    }

    @Override
    public LoggerSupportability startTimer() {
        return this;
    }

}
