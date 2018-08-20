/*
 * Copyright (c) 2018 AT&T Intellectual Property.
 *
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
 *
 *
 */

import { Injectable, Inject } from '@angular/core';
import { Http, Response } from '@angular/http';
import 'rxjs/add/operator/toPromise';
import {IAppConfigurtaion, ValidationConfiguration, Validations, Plugins, PluginsConfiguration} from "app/models";
import {IApi} from "app/models/app-config";
import {SdcConfigToken, ISdcConfig} from "../config/sdc-config.config";

@Injectable()
export class ConfigService {

    private baseUrl;
    public configuration: IAppConfigurtaion;
    public api:IApi;

    constructor(private http: Http, @Inject(SdcConfigToken) private sdcConfig:ISdcConfig) {
    	this.api = this.sdcConfig.api;
        this.baseUrl = this.api.root + this.sdcConfig.api.component_api_root;
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

    loadPluginsConfiguration(): Promise<PluginsConfiguration> {
        let url:string = this.api.no_proxy_root + this.api.GET_plugins_configuration;
        let promise: Promise<any> = this.http.get(url).map((res: Response) => res.json()).toPromise();
        return new Promise<PluginsConfiguration>((resolve) => {
            promise.then((pluginsData: Plugins) => {
                PluginsConfiguration.plugins = pluginsData;
                resolve();
            }).catch((ex) => {
                console.warn("Error loading plugins configuration from FE", ex);

                PluginsConfiguration.plugins = [] as Plugins;
                resolve();
            });
        });
    }

}
