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

package org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.network;

/**
 * Created by TALIO on 11/22/2016.
 */
public class NetworkCapacity {
    private String protocolWithHighestTrafficProfileAcrossAllNICs;
    private int networkTransactionsPerSecond;

    public String getProtocolWithHighestTrafficProfileAcrossAllNICs() {
        return protocolWithHighestTrafficProfileAcrossAllNICs;
    }

    public void setProtocolWithHighestTrafficProfileAcrossAllNICs(String protocolWithHighestTrafficProfileAcrossAllNICs) {
        this.protocolWithHighestTrafficProfileAcrossAllNICs = protocolWithHighestTrafficProfileAcrossAllNICs;
    }

    public int getNetworkTransactionsPerSecond() {
        return networkTransactionsPerSecond;
    }

    public void setNetworkTransactionsPerSecond(int networkTransactionsPerSecond) {
        this.networkTransactionsPerSecond = networkTransactionsPerSecond;
    }
}
