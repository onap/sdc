/*
 * Copyright © 2016-2018 European Support Limited
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

package org.openecomp.sdc.ci.tests.datatypes;

public class CapabilityDetails {
    private String name;
    private String type;
    private String maxOccurrences;
    private String minOccurrences;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMaxOccurrences() {
        return maxOccurrences;
    }

    public void setMaxOccurrences(String maxOccurrences) {
        this.maxOccurrences = maxOccurrences;
    }

    public String getMinOccurrences() {
        return minOccurrences;
    }

    public void setMinOccurrences(String minOccurrences) {
        this.minOccurrences = minOccurrences;
    }
}
