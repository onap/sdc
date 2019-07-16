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

package org.openecomp.sdc.be.config;

public class DmeConfiguration {
    private String dme2Search = "DME2SEARCH";
    private String dme2Resolve = "DME2RESOLVE";

    public String getDme2Search() {
        return dme2Search;
    }

    public void setDme2Search(String dme2Search) {
        this.dme2Search = dme2Search;
    }

    public String getDme2Resolve() {
        return dme2Resolve;
    }

    public void setDme2Resolve(String dme2Resolve) {
        this.dme2Resolve = dme2Resolve;
    }

    @Override
    public String toString() {
        return "DmeConfiguration [dme2Search=" + dme2Search + ", dme2Resolve=" + dme2Resolve + "]";
    }
}
