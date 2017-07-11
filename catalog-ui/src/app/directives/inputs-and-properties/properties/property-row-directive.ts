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
 * Created by obarda on 1/8/2017.
 */
'use strict';

export interface IPropertyRowDirective extends ng.IScope {
    onNameClicked:Function;
    isClickable:boolean;
}

export class PropertyRowDirective implements ng.IDirective {

    constructor() {

    }

    scope = {
        property: '=',
        instanceName: '=',
        instanceId: '=',
        instancePropertiesMap: '=',
        onNameClicked: '&',
        onCheckboxClicked: '&'
    };

    restrict = 'E';
    replace = true;
    template = ():string => {
        return require('./property-row-view.html');
    };

    link = (scope:IPropertyRowDirective, element:any, $attr:any) => {
        scope.isClickable = $attr.onNameClicked ? true : false;
    };

    public static factory = ()=> {
        return new PropertyRowDirective();
    };

}

PropertyRowDirective.factory.$inject = [];
