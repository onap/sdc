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

import { HttpClient } from '@angular/common/http';
import { Inject, Injectable, Injector } from '@angular/core';
import { IAppConfigurtaion, Plugins, PluginsConfiguration, ValidationConfiguration, Validations } from 'app/models';
import { IApi } from 'app/models/app-config';
import 'rxjs/add/operator/toPromise';
import { ISdcConfig, SdcConfigToken } from '../config/sdc-config.config';
import { CacheService } from './cache.service';

@Injectable()
export class ConfigService {

    public configuration: IAppConfigurtaion;
    public api: IApi;
    private baseUrl;

    constructor(
                @Inject(SdcConfigToken) private sdcConfig: ISdcConfig,
                private cacheService: CacheService,
                private injector: Injector,
                private http: HttpClient
        ) {
            this.api = this.sdcConfig.api;
            this.baseUrl = this.api.root + this.sdcConfig.api.component_api_root;
    }

    loadSdcSetupData = (): Promise<void> =>  {
        const url: string = this.api.root + this.api.GET_SDC_Setup_Data;
        const promise: Promise<any> = this.http.get<any>(url).toPromise();
        promise.then((response) => {
            this.cacheService.set('version', response.version);
            this.cacheService.set('serviceCategories', response.categories.serviceCategories);
            this.cacheService.set('resourceCategories', response.categories.resourceCategories);
            this.cacheService.set('UIConfiguration', response.configuration);
        });
        return promise;
    }

    loadValidationConfiguration(): Promise<ValidationConfiguration> {
        const url: string = this.sdcConfig.validationConfigPath;
        const promise: Promise<ValidationConfiguration> = this.http.get<ValidationConfiguration>(url).toPromise();
        promise.then((validationData: Validations) => {
            ValidationConfiguration.validation = validationData;
            this.cacheService.set('validation', validationData);
        }).catch((ex) => {
            console.error('Error loading validation.json configuration file, using fallback data', ex);

            const fallback = {
                propertyValue: {
                    max: 2500,
                    min: 0
                },
                validationPatterns: {
                    string: '^[\\sa-zA-Z0-9+-]+$',
                    stringOrEmpty: '^[\\sa-zA-Z0-9&-]*$',
                    comment: '^[\\sa-zA-Z0-9+-_\\{\\}"]+$',
                    integer: '^(([-+]?\\d+)|([-+]?0x[0-9a-fA-F]+))$'
                }
            };

            this.cacheService.set('validation', fallback);
        });

        return promise;
    }

    loadPluginsConfiguration = (): Promise<PluginsConfiguration> =>  {
        const url: string = this.api.no_proxy_root + this.api.GET_plugins_configuration;
        const promise: Promise<any> = this.http.get<PluginsConfiguration>(url).toPromise();
        return new Promise<PluginsConfiguration>((resolve) => {
            promise.then((pluginsData: Plugins) => {
                PluginsConfiguration.plugins = pluginsData;
                resolve();
            }).catch((ex) => {
                console.error('Error loading plugins configuration from FE', ex);

                PluginsConfiguration.plugins = [] as Plugins;
                resolve();
            });
        });
    }

}
