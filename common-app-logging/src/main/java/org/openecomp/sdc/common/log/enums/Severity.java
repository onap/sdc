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
 * Created by dd4296 on 12/14/2017.
 */
public enum Severity {
    OK(0),
    WARNING(1),
    CRITICAL(2),
    DOWN(3),
    UNREACHABLE(4);

    int severityType;

    Severity(int serveryType) {
        this.severityType = serveryType;
    }

    public int getSeverityType() {
        return severityType;
    }
}

