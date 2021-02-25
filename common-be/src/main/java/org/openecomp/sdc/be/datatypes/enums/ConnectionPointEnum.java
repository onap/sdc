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

import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConnectionPointEnum {

    CAPABILITY("capability"), REQUIREMENT("requirement");

    private static List<ConnectionPointEnum> connectionPointEnums = Arrays.asList(values());
    private final String data;

    public static ConnectionPointEnum getConnectionPointEnum(String data) {
        return connectionPointEnums.stream().filter(cp -> cp.toString().equals(data)).findAny().orElse(null);
    }

    @Override
    public String toString() {
        return data;
    }
}
