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

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@SuppressWarnings("CheckStyle")
@Getter
@Setter
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

    public boolean isEmpty() {
        return Objects.isNull(nf_type) && Objects.isNull(nfc_type) && Objects.isNull(nf_naming_code) && Objects.isNull(nfc_naming_code) && Objects
            .isNull(network_role) && Objects.isNull(pps_capacity);
    }
}
