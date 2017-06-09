/**
 * Created by ob0695 on 4/9/2017.
 */

import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import 'rxjs/add/operator/toPromise';
import {IAppConfigurtaion, ValidationConfiguration, Validations} from "app/models";
import {sdc2Config} from './../../../main';

declare var __ENV__: string;

@Injectable()
export class ConfigService {

    private baseUrl;
    public configuration: IAppConfigurtaion;

    constructor(private http: Http) {
        this.baseUrl = sdc2Config.api.root + sdc2Config.api.component_api_root;
    }

    loadValidationConfiguration(): Promise<ValidationConfiguration> {
        let url: string = sdc2Config.validationConfigPath;
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
