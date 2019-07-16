/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.common.log.enums;

/**
 * Created by mm288v on 12/27/2017.
 * This enum reflects the Marker text in logback.xml file per each ecomp marker
 */
public enum LogMarkers {
    DEBUG_MARKER("DEBUG_MARKER"),
    ERROR_MARKER("ERROR_MARKER"),
    AUDIT_MARKER("AUDIT_MARKER"),
    METRIC_MARKER("METRICS");

    private String text;

    LogMarkers(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }

}
