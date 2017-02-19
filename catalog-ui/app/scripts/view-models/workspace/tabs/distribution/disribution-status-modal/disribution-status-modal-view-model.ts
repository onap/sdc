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
/// <reference path="../../../../../references"/>
module Sdc.ViewModels {
    'use strict';

    interface IDistributionStatusModalViewModelScope {
        distribution:Models.Distribution;
        status:string;
        getStatusCount(distributionComponent:Array<Models.DistributionComponent>):any;
        getUrlName(url:string):string;
        modalDitributionStatus:ng.ui.bootstrap.IModalServiceInstance;
        footerButtons: Array<any>;
        close(): void;
    }

    export class DistributionStatusModalViewModel {

        static '$inject' = ['$scope','$modalInstance', 'data'];

        constructor(private $scope:IDistributionStatusModalViewModelScope,
                    private $modalInstance:ng.ui.bootstrap.IModalServiceInstance,
                    private data:any
                    ) {
            this.initScope();
        }

        private initScope = ():void => {
            this.$scope.distribution = this.data.distribution;
            this.$scope.status = this.data.status;
            this.$scope.modalDitributionStatus = this.$modalInstance;

            this.$scope.getUrlName = (url:string):string =>{
                let urlName:string = _.last(url.split('/'));
                return urlName;
            };

            this.$scope.close = ():void => {
                this.$modalInstance.close();
            };

            this.$scope.footerButtons = [
                {'name': 'Close', 'css': 'blue', 'callback': this.$scope.close }
            ];

        };


    }
}
