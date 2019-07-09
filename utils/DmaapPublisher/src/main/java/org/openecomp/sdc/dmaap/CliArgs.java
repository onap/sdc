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

package org.openecomp.sdc.dmaap;

import com.google.common.base.MoreObjects;
import org.kohsuke.args4j.Option;

public class CliArgs {

    @Option(name="yml",aliases = {"-YML","YML","-yml","-YAML","YAML","-yaml"}, usage="mandatory arg. YAML filename", required=true)
    private String yamlFilename;

    @Option(name="path",aliases = {"-path","PATH","-PATH"}, usage="mandatory arg. path to the yaml file which contains topic config (publisher data + messages)", required=true)
    private String yamlPath;

    @Option(name="cr",aliases = {"CR","-cr","-CR"}, usage="optional arg. concurrent requests", required=false)
    private String concurrentRequests;

    @Option(name="notification",aliases = {"NOTIFICATION","-NOTIFICATION","-notification"}, usage="optional load dynamic messages", required=false)
    private String notificationData;

    public String getYamlPath() {
        return yamlPath;
    }

    public String getYamlFilename() {
        return yamlFilename;
    }

    public void setYamlPath(String yamlPath) {
        this.yamlPath = yamlPath;
    }


    public String getConcurrentRequests() {
        return concurrentRequests;
    }

    public void setConcurrentRequests(String concurrentRequests) {
        this.concurrentRequests = concurrentRequests;
    }

    public String getNotificationData() {
        return notificationData;
    }


    public void setYamlFilename(String yamlFilename) {
        this.yamlFilename = yamlFilename;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("yamlPath", yamlPath)
                .add("concurrentRequests", concurrentRequests)
                .toString();
    }
    
    
}
