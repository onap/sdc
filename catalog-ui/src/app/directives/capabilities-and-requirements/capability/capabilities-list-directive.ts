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
 * Created by ob0695 on 5/9/2017.
 */
/**
 * Created by obarda on 1/8/2017.
 */
'use strict';
import {CapabilitiesGroup} from "app/models";

export interface ICapabilitiesListScope extends ng.IScope {
    capabilities:CapabilitiesGroup;
}


export class CapabilitiesListDirective implements ng.IDirective {

    constructor() {

    }

    scope = {
        capabilities: '=',
    };

    restrict = 'E';
    replace = true;
    template = ():string => {
        return require('./capabilities-list-view.html');
    };

    link = (scope:ICapabilitiesListScope, element:any, $attr:any) => {

    };

    public static factory = ()=> {
        return new CapabilitiesListDirective();
    };
}

CapabilitiesListDirective.factory.$inject = [];
