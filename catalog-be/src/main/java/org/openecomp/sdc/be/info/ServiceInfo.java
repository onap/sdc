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

package org.openecomp.sdc.be.info;

import java.util.List;

public class ServiceInfo {

    private String name;
    private List<ServiceVersionInfo> versions;

    ServiceInfo() {
    }

    public ServiceInfo(String serviceName, List<ServiceVersionInfo> list) {
        this.name = serviceName;
        this.versions = list;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ServiceVersionInfo> getVersions() {
        return versions;
    }

    public void setVersions(List<ServiceVersionInfo> versions) {
        this.versions = versions;
    }

}
