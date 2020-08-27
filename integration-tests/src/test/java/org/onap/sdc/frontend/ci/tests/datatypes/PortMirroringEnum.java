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

package org.onap.sdc.frontend.ci.tests.datatypes;

public enum PortMirroringEnum {

    PMC_ELEMENT_IN_PALLETE("Port Mirroring Configuration"),
    PMCP_ELEMENT_IN_PALLETE("Port Mirroring Configuration By Policy"),
    PM_REQ_TYPE("org.openecomp.capabilities.PortMirroring"),
    PMC_SOURCE_CAP("Port Mirroring Configuration 0: source: [1, UNBOUNDED]"),
    PMC1_SOURCE_CAP("Port Mirroring Configuration 1: source: [1, UNBOUNDED]"),
    PMCP_SOURCE_CAP("Port Mirroring Configuration By Policy 0: source: [1, UNBOUNDED]"),
    PMCP1_SOURCE_CAP("Port Mirroring Configuration By Policy 1: source: [1, UNBOUNDED]"),
    PMC_COLLECTOR_CAP("Port Mirroring Configuration 0: collector: [1, 1]"),
    CISCO_VENDOR_NAME("CISCO"),
    CISCO_VENDOR_MODEL_NUMBER("4500x"),
    APCON1_VENDOR_NAME("APCON1"),
    APCON1_VENDOR_MODEL_NUMBER("Test_APCON1"),
    APCON2_VENDOR_NAME("APCON2"),
    APCON2_VENDOR_MODEL_NUMBER("Test_APCON2"),
    VMME_ZIP("2016-227_vmme_vmme_30_1610_e2e.zip"),
    VPROBE_ZIP("vProbe_2017-10-22_07-24.zip"),
    SERVICE_PROXY_TYPE("Service Proxy"),
    SERVICE_TYPE("transport"),
    TYPE("Resource"),
    RESOURCE_TYPE("Configuration"),
    CATEGORY("Configuration"),
    SUB_CATEGORY("Configuration"),
    NETWORK_ROLE_XPATH("//input[@name='network_role']"),
    NFC_TYPE_XPATH("//input[@name='nfc_type']"),
    PPS_CAPACITY_XPATH("//input[@name='pps_capacity']"),
    NF_TYPE_XPATH("//input[@name='nf_type']"),
    NETWORK_ROLE_VALUE("NETWORK ROLE TEXT"),
    NFC_TYPE_VALUE("NFC TYPE TEXT"),
    PPS_CAPACITY_VALUE("PPS CAPACITY TEXT"),
    NF_TYPE_VALUE("NF TYPE TEXT"),
    PMCP_NEWNAME("NewPMCP_Name");


    private String value;

    public String getValue() {
        return value;
    }

    PortMirroringEnum(String value) {
        this.value = value;
    }

}
