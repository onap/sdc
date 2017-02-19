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
/// <reference path="../references"/>
module Sdc.Filters {

    export class CategoryIconFilter{

        constructor() {
            let filter = <CategoryIconFilter>( (category:string) => {
               let map = {
                   'Application Layer 4+/Application Servers': ['applicationServer', 'server'],
                   'Application Layer 4+/Media Servers': ['applicationServer', 'server'],
                   'Application Layer 4+/Web Server': ['applicationServer', 'server'],
                   'Network Layer 4+/Common Network Resources': ['network', 'loadBalancer'],
                   'Generic/Infrastructure': ['objectStorage', 'compute'],
                   'Generic/Network Elements': ['port', 'network', 'router'],
                   'Application Layer 4+/Database': ['database'],
                   'Generic/Database': ['database'],
                   'Network Layer 2-3/Router': ['router'],
                   'Network Layer 2-3/Gateway': ['gateway'],
                   'Network Layer 2-3/LAN Connectors': ['connector'],
                   'Network Layer 2-3/WAN Connectors': ['connector'],
                   'Application Layer 4+/Border Elements': ['borderElement'],
                   'Application Layer 4+/Load Balancer': ['loadBalancer'],
                   'Application Layer 4+/Call Control': ['call_controll'],
                   'VoIP Call Control': ['call_controll'],
                   'Mobility': ['mobility'],
                   'Network L1-3': ['network_l_1-3'],
                   'Network L4': ['network_l_4']
               }
                return map[category];

            });
            return filter;
        }
    }
}
