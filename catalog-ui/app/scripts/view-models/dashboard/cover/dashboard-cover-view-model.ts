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
/// <reference path="../../../references"/>
module Sdc.ViewModels {

    'use strict';

    export interface IDashboardCoverViewModelScope extends ng.IScope {
        showTutorial:boolean;
        version:string;
        modalInstance:ng.ui.bootstrap.IModalServiceInstance;
    }

    export class DashboardCoverViewModel {
        static '$inject' = [
            '$scope',
            '$stateParams',
            'Sdc.Services.CacheService',
            '$templateCache',
            '$state',
            '$modal',
            'sdcConfig'
        ];

        constructor(private $scope:IDashboardCoverViewModelScope,
                    private $stateParams:any,
                    private cacheService:Services.CacheService,
                    private $templateCache:ng.ITemplateCacheService,
                    private $state:any,
                    private $modal:ng.ui.bootstrap.IModalService,
                    private sdcConfig:Models.IAppConfigurtaion) {

            // Show the tutorial if needed when the dashboard page is opened.<script src="bower_components/angular-filter/dist/angular-filter.min.js"></script>
            // This is called from the welcome page.
            if (this.$stateParams.show === 'tutorial') {
                this.$scope.showTutorial = true;
            } else if (this.$stateParams.show === 'whatsnew') {
                this.$scope.version = this.cacheService.get('version');
                this.openWhatsNewModal(this.$scope);
            }

            this.initScope();
        }

        private initScope = ():void => {

        };

        private openWhatsNewModal = (scope:IDashboardCoverViewModelScope):void => {

            let onOk = ():void => {};

            let onCancel = ():void => {
                this.$state.go('dashboard.welcome', {show: ''});
            };

            let modalOptions:ng.ui.bootstrap.IModalSettings = {
                template: this.$templateCache.get('/app/scripts/view-models/whats-new/whats-new-view.html'),
                controller: 'Sdc.ViewModels.WhatsNewViewModel',
                size: 'sdc-l',
                backdrop: 'static',
                scope: scope,
                resolve: {
                    'version': scope.version
                }
            };

            scope.modalInstance = this.$modal.open(modalOptions);
            scope.modalInstance.result.then(onOk, onCancel);
        };

    }

}
