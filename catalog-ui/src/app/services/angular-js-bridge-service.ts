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
 * Modifications Copyright (C) 2026 Deutsche Telekom AG.
 */

'use strict';
import {IAppConfigurtaion} from "../models/app-config";
import {getSdcConfig} from "../ng2/config/sdc-config.config.factory";

export class AngularJSBridge {
    private static _sdcConfig:IAppConfigurtaion;

    // The eight imagesPath readers are plain graph-node classes constructed with `new`, so they
    // cannot inject SdcConfigToken. They used to depend on the AngularJS run block constructing
    // this service for its side effect; seeding lazily from the same config module keeps them
    // working now that the run block is gone. getSdcConfig() resolves to the identical webpack
    // module object that ng1's `sdcConfig` constant held, so the value is unchanged.
    public static getAngularConfig():IAppConfigurtaion {
        if (!AngularJSBridge._sdcConfig) {
            AngularJSBridge._sdcConfig = getSdcConfig() as IAppConfigurtaion;
        }
        return AngularJSBridge._sdcConfig;
    }


    constructor(sdcConfig:IAppConfigurtaion) {
        AngularJSBridge._sdcConfig = sdcConfig;
    }
}

AngularJSBridge.$inject = ['sdcConfig']
