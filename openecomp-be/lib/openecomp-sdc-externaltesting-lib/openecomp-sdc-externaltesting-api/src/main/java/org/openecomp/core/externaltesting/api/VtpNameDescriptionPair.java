/*
 * Copyright © 2019 iconectiv
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
package org.openecomp.core.externaltesting.api;

import lombok.Data;

/**
 * Scenarios and Test Suites are simple name/description pairs.
 */
@Data
public class VtpNameDescriptionPair {

    private String name;
    private String description;

    VtpNameDescriptionPair() {
    }

    public VtpNameDescriptionPair(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }
}
