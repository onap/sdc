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
import {AssetPopoverObj} from "app/models";

export interface IAssetPopoverScope extends ng.IScope {
    assetPopoverObj:AssetPopoverObj;
    deleteAsset:Function;
}

export class AssetPopoverDirective implements ng.IDirective {

    constructor() {
    }

    scope = {
        assetPopoverObj: '=',
        deleteAsset: '&'
    };

    restrict = 'E';
    replace = true;
    template = ():string => {
        return require('app/directives/graphs-v2/asset-popover/asset-popover.html');
    };

    link = (scope:IAssetPopoverScope, element:JQuery, $attr:ng.IAttributes) => {

    };

    public static factory = ()=> {
        return new AssetPopoverDirective();
    };
}

AssetPopoverDirective.factory.$inject = [];

