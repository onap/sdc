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

'use strict';

export class SdcSingleTabDirective implements ng.IDirective {

    constructor(private $compile:ng.ICompileService, private $parse:ng.IParseService) {
    }

    restrict = 'E';

    link = (scope, elem:any, attrs:any, ctrl:any) => {
        if (!elem.attr('inner-sdc-single-tab')) {
            let name = this.$parse(elem.attr('ctrl'))(scope);
            elem = elem.removeAttr('ctrl');
            elem.attr('inner-sdc-single-tab', name);
            this.$compile(elem)(scope);
        }
    };

    public static factory = ($compile:ng.ICompileService, $parse:ng.IParseService)=> {
        return new SdcSingleTabDirective($compile, $parse);
    };
}

export class InnerSdcSingleTabDirective implements ng.IDirective {

    constructor() {
    }

    scope = {
        singleTab: "=",
        isViewOnly: "="
    };

    replace = true;
    restrict = 'A';
    controller = '@';
    template = '<div ng-include src="singleTab.templateUrl"></div>';

    public static factory = ()=> {
        return new InnerSdcSingleTabDirective();
    };
}

SdcSingleTabDirective.factory.$inject = ['$compile', '$parse'];
InnerSdcSingleTabDirective.factory.$inject = [];

