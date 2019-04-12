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

public class ServiceVersionInfo {

    private String version;
    private String url;

    ServiceVersionInfo() {}

    public ServiceVersionInfo(String serviceName, String serviceVersion, String context) {
        super();
        this.version = serviceVersion;
        StringBuilder sb = new StringBuilder(context);
        sb.append("services/").append(serviceName).append("/").append(serviceVersion);
        url = sb.toString();
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String serviceVersion) {
        this.version = serviceVersion;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
