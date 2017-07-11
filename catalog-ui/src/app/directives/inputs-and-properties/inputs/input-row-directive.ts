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

export interface IInputRowDirective extends ng.IScope {
    showDeleteIcon:boolean;
}


export class InputRowDirective implements ng.IDirective {

    constructor() {

    }

    scope = {
        instanceInputsMap: '=',
        input: '=',
        instanceName: '=',
        instanceId: '=',
        isViewOnly: '=',
        deleteInput: '&',
        onNameClicked: '&',
        onCheckboxClicked: '&'
    };

    restrict = 'E';
    replace = true;
    template = ():string => {
        return require('./input-row-view.html');
    };

    link = (scope:IInputRowDirective, element:any, $attr:any) => {
        scope.showDeleteIcon = $attr.deleteInput ? true : false;
    };

    public static factory = ()=> {
        return new InputRowDirective();
    };
}

InputRowDirective.factory.$inject = [];
