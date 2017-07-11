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
 * Created by rcohen on 9/25/2016.
 */
'use strict';

export interface IInfoTooltipScope extends ng.IScope {
    infoMessageTranslate:string;
    direction:string;
}


export class InfoTooltipDirective implements ng.IDirective {

    constructor() {
    }

    scope = {
        infoMessageTranslate: '@',
        direction: '@'//get 'right' or 'left', the default is 'right'
    };

    restrict = 'E';
    replace = true;
    template = ():string => {
        return require('./info-tooltip.html');
    };

    link = (scope:IInfoTooltipScope, element:any, $attr:any) => {
        scope.direction = scope.direction || 'right';
    };

    public static factory = ()=> {
        return new InfoTooltipDirective();
    };
}

InfoTooltipDirective.factory.$inject = [];
