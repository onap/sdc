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
 * Created by obarda on 2/4/2016.
 */
'use strict';
import * as _ from "lodash";
import {IComponentService, ComponentService} from "./component-service";
import {IAppConfigurtaion, Combination, Component} from "../../models";
import {SharingService} from "../sharing-service";

export interface ICombinationService extends IComponentService {}

export class CombinationService extends ComponentService implements ICombinationService {

    static '$inject' = [
        'Restangular',
        'sdcConfig',
        'Sdc.Services.SharingService',
        '$q',
        '$base64'
    ];

    constructor(protected restangular:restangular.IElement,
                protected sdcConfig:IAppConfigurtaion,
                protected sharingService:SharingService,
                protected $q:ng.IQService,
                protected $base64:any
                ) {
        super(restangular, sdcConfig, sharingService, $q, $base64);

        this.restangular = restangular.one("combinations");
    }

    createComponentObject = (component:Component):Component => {
        return new Combination(this, this.$q, <Combination>component);
    };

}


   
