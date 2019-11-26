/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.common.log.elements;

import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.openecomp.sdc.common.log.api.ILogFieldsHandler;
import org.openecomp.sdc.common.log.enums.LogLevel;
import org.openecomp.sdc.common.log.enums.LogMarkers;
import org.slf4j.Logger;
import org.slf4j.MarkerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoggerDebug extends LoggerBase {

    private static List<String> mandatoryFields = new ArrayList<>(Arrays.asList(ONAPLogConstants.MDCs.REQUEST_ID));

    LoggerDebug(ILogFieldsHandler ecompMdcWrapper, Logger logger) {
        super(ecompMdcWrapper, MarkerFactory.getMarker(LogMarkers.DEBUG_MARKER.text()), logger);
    }

    @Override
    public LoggerDebug clear() {
        //nothing to clean up
        return this;
    }

    @Override
    public void log(LogLevel logLevel, String message, Object...params){
        setKeyRequestIdIfNotSetYet();
        super.log(logLevel, message, params);
    }

    @Override
    public void log(LogLevel logLevel, String message, Throwable throwable){
        setKeyRequestIdIfNotSetYet();
        super.log(logLevel, message, throwable);
    }

    @Override
    public void log(LogLevel logLevel, String message){
        setKeyRequestIdIfNotSetYet();
        super.log(logLevel, message);
    }

    @Override
    public LoggerDebug startTimer() {
        return this;
    }

    @Override
    public List<String> getMandatoryFields() {
        return mandatoryFields;
    }
}
