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
 * Created by ob0695 on 4/9/2017.
 */

import { Injectable, Inject } from '@angular/core';
import { Http, Response } from '@angular/http';
import 'rxjs/add/operator/toPromise';
import {IAppConfigurtaion, ValidationConfiguration, Validations} from "app/models";
import {SdcConfigToken, ISdcConfig} from "../config/sdc-config.config";

@Injectable()
export class ConfigService {

    private baseUrl;
    public configuration: IAppConfigurtaion;

    constructor(private http: Http, @Inject(SdcConfigToken) private sdcConfig:ISdcConfig) {
        this.baseUrl = this.sdcConfig.api.root + this.sdcConfig.api.component_api_root;
    }

    loadValidationConfiguration(): Promise<ValidationConfiguration> {
        let url: string = this.sdcConfig.validationConfigPath;
        let promise: Promise<ValidationConfiguration> = this.http.get(url).map((res: Response) => res.json()).toPromise();
        promise.then((validationData: Validations) => {
            ValidationConfiguration.validation = validationData;
        }).catch((ex) => {
            console.error('Error loading validation.json configuration file, using fallback data', ex);
            
            let fallback:Validations = {
                "propertyValue": {
                    "max": 2500,
                    "min": 0
                },

                "validationPatterns": {
                    "string": "^[\\sa-zA-Z0-9+-]+$",
                    "comment": "^[\\sa-zA-Z0-9+-_\\{\\}\"]+$",
                    "integer": "^(([-+]?\\d+)|([-+]?0x[0-9a-fA-F]+))$"
                }
            };
            
            ValidationConfiguration.validation = fallback;
            
        });

        return promise;
    }

}
