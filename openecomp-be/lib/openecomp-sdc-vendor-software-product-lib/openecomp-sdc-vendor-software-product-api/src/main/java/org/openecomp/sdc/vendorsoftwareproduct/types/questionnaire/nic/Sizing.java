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

package org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.nic;

/**
 * Created by TALIO on 11/22/2016
 */
public class Sizing {
    private String describeQualityOfService;
    private Number acceptablePacketLoss;
    private PacketsAndBytes inflowTrafficPerSecond;
    private PacketsAndBytes outflowTrafficPerSecond;
    private PacketsAndBytes flowLength;
    private AcceptableJitter acceptableJitter;

    public String getDescribeQualityOfService() {
        return describeQualityOfService;
    }

    public void setDescribeQualityOfService(String describeQualityOfService) {
        this.describeQualityOfService = describeQualityOfService;
    }

    public Number getAcceptablePacketLoss() {
        return acceptablePacketLoss;
    }

    public void setAcceptablePacketLoss(Number acceptablePacketLoss) {
        this.acceptablePacketLoss = acceptablePacketLoss;
    }

    public PacketsAndBytes getInflowTrafficPerSecond() {
        return inflowTrafficPerSecond;
    }

    public void setInflowTrafficPerSecond(PacketsAndBytes inflowTrafficPerSecond) {
        this.inflowTrafficPerSecond = inflowTrafficPerSecond;
    }

    public PacketsAndBytes getOutflowTrafficPerSecond() {
        return outflowTrafficPerSecond;
    }

    public void setOutflowTrafficPerSecond(PacketsAndBytes outflowTrafficPerSecond) {
        this.outflowTrafficPerSecond = outflowTrafficPerSecond;
    }

    public PacketsAndBytes getFlowLength() {
        return flowLength;
    }

    public void setFlowLength(PacketsAndBytes flowLength) {
        this.flowLength = flowLength;
    }

    public AcceptableJitter getAcceptableJitter() {
        return acceptableJitter;
    }

    public void setAcceptableJitter(AcceptableJitter acceptableJitter) {
        this.acceptableJitter = acceptableJitter;
    }
}
