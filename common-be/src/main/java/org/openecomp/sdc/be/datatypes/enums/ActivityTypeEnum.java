/*
 *
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.datatypes.enums;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActivityTypeEnum {
    DELEGATE("delegate"),
    INLINE("inline"),
    CALL_OPERATION("call_operation"),
    SET_STATE("set_state");

    private final String value;

    public static Optional<ActivityTypeEnum> getEnum(String name) {
        for (ActivityTypeEnum activityType : values()) {
            if (activityType.getValue().equals(name)) {
                return Optional.of(activityType);
            }
        }
        return Optional.empty();
    }
}
