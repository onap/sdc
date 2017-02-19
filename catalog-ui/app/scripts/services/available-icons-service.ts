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
/**
 * Created by obarda on 2/23/2016.
 */
/// <reference path="../references"/>
module Sdc.Services {
    'use strict';

    interface IAvailableIconsService {
        getIcons(componentType: Utils.Constants.ComponentType):Array<string>;
    }

    export class AvailableIconsService implements IAvailableIconsService {
        constructor() {}
        public getIcons = (componentType: string): Array<string> => {
            let icons: string[];
            switch (componentType){
                case Utils.Constants.ComponentType.SERVICE:
                    icons = [
                        'call_controll',
                        'mobility',
                        'network_l_1-3',
                        'network_l_4'
                    ];
                    break;

                case Utils.Constants.ComponentType.RESOURCE:
                    icons= [
                        'router',
                        'database',
                        'network',
                        'objectStorage',
                        'connector',
                        'brocade',
                        'cisco',
                        'ericsson',
                        'tropo',
                        'fortinet',
                        'att',
                        'broadsoft',
                        'alcatelLucent',
                        'metaswitch',
                        'aricent',
                        'mySql',
                        'oracle',
                        'nokia_siemens',
                        'juniper',
                        'call_controll',
                        'borderElement',
                        'applicationServer',
                        'server',
                        'port',
                        'loadBalancer',
                        'compute',
                        'gateway',
                        'cp',
                        'vl',
                        'vfw',
                        'firewall'
                    ];
                    break;

                case Utils.Constants.ComponentType.PRODUCT:
                    icons = [
                        'vfw',
                        'network',
                        'security',
                        'cloud',
                        'setting',
                        'orphan',
                        'wanx',
                        'vrouter',
                        'ucpe',
                        'mobility'

                    ];
                    break;

            }
            return icons;
        }

    }
}



