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
package org.openecomp.sdc.enrichment.impl.tosca.model;

import java.util.Objects;

@SuppressWarnings("CheckStyle")
public class PortMirroringConnectionPointDescription {

    private String nf_type;
    private String nfc_type;
    private String nf_naming_code;
    private String nfc_naming_code;
    //Keeping below attributes as objects to accomodate for tosca functions for property

    // values like get_input, get_attribute
    private Object network_role;
    private Object pps_capacity;

    public PortMirroringConnectionPointDescription() {
        //Populating empty strings as default values to be populated in tosca
        nf_type = "";
        nfc_type = "";
        nf_naming_code = "";
        nfc_naming_code = "";
        network_role = "";
        pps_capacity = "";
    }

    public String getNf_type() {
        return nf_type;
    }

    public void setNf_type(String nf_type) {
        this.nf_type = nf_type;
    }

    public String getNfc_type() {
        return nfc_type;
    }

    public void setNfc_type(String nfc_type) {
        this.nfc_type = nfc_type;
    }

    public String getNf_naming_code() {
        return nf_naming_code;
    }

    public void setNf_naming_code(String nf_naming_code) {
        this.nf_naming_code = nf_naming_code;
    }

    public String getNfc_naming_code() {
        return nfc_naming_code;
    }

    public void setNfc_naming_code(String nfc_naming_code) {
        this.nfc_naming_code = nfc_naming_code;
    }

    public Object getNetwork_role() {
        return network_role;
    }

    public void setNetwork_role(Object network_role) {
        this.network_role = network_role;
    }

    public Object getPps_capacity() {
        return pps_capacity;
    }

    public void setPps_capacity(String pps_capacity) {
        this.pps_capacity = pps_capacity;
    }

    public boolean isEmpty() {
        return Objects.isNull(nf_type) && Objects.isNull(nfc_type) && Objects.isNull(nf_naming_code) && Objects.isNull(nfc_naming_code) && Objects
            .isNull(network_role) && Objects.isNull(pps_capacity);
    }
}
