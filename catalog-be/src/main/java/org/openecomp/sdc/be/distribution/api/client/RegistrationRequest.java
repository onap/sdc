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

package org.openecomp.sdc.be.distribution.api.client;

import java.util.List;

public class RegistrationRequest {
    private String apiPublicKey;
    private String distrEnvName;
    private Boolean isConsumerToSdcDistrStatusTopic;
    private List<String> distEnvEndPoints;
    private String managerApiPublicKey;
    private String managerApiSecretKey;

    public RegistrationRequest(String apiPublicKey, String distrEnvName, boolean isConsumerToSdcDistrStatusTopic) {
        this.apiPublicKey = apiPublicKey;
        this.distrEnvName = distrEnvName;
        this.isConsumerToSdcDistrStatusTopic = isConsumerToSdcDistrStatusTopic;
    }
    public RegistrationRequest(String apiPublicKey, String distrEnvName, List<String> distEnvEndPoints, boolean isConsumerToSdcDistrStatusTopic){
        this.apiPublicKey = apiPublicKey;
        this.distrEnvName = distrEnvName;
        this.distEnvEndPoints = distEnvEndPoints;
        this.isConsumerToSdcDistrStatusTopic = isConsumerToSdcDistrStatusTopic;
    }

    public String getApiPublicKey() {
        return apiPublicKey;
    }

    public String getDistrEnvName() {
        return distrEnvName;
    }

    public Boolean getIsConsumerToSdcDistrStatusTopic() {
        return isConsumerToSdcDistrStatusTopic;
    }
    public List<String> getDistEnvEndPoints() {
        return distEnvEndPoints;
    }

    public void setDistEnvEndPoints(List<String> distEnvEndPoints) {
        this.distEnvEndPoints = distEnvEndPoints;
    }

    public String getManagerApiPublicKey() {
        return managerApiPublicKey;
    }

    public void setManagerApiPublicKey(String managerApiPublicKey) {
        this.managerApiPublicKey = managerApiPublicKey;
    }

    public String getManagerApiSecretKey() {
        return managerApiSecretKey;
    }

    public void setManagerApiSecretKey(String managerApiSecretKey) {
        this.managerApiSecretKey = managerApiSecretKey;
    }
}
