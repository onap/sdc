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
module Sdc.ViewModels {
    'use strict';

    interface IPreLoadingViewScope {
        startZoomIn: boolean;
    }

    export class PreLoadingViewModel {

        static '$inject' = ['$scope'];
        constructor(private $scope:IPreLoadingViewScope){
            this.init($scope);
        }

        private init = ($scope:IPreLoadingViewScope):void => {
            this.animate($('.caption1'),'fadeInUp',400);
            this.animate($('.caption2'),'fadeInUp',800);
        };

        private animate = (element:any, animation:string, when:number):void => {
            window.setTimeout(()=>{
                element.addClass("animated " + animation);
                element[0].style="visibility: visible;";
            },when);
        };

    }
}
