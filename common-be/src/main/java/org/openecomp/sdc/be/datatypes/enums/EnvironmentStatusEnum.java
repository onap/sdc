/*
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.datatypes.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EnvironmentStatusEnum {
    UNKNOWN("unknown"),
    IN_PROGRESS("in_progress"),
    FAILED("failed"),
    COMPLETED("completed");

    private final String name;

    public static EnvironmentStatusEnum getByName(final String name) {
        switch (name) {
            case ("in_progress"):
                return IN_PROGRESS;
            case ("failed"):
                return FAILED;
            case ("completed"):
                return COMPLETED;
            default:
                return UNKNOWN;
        }
    }

}
