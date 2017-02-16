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
module Sdc.ViewModels.Wizard {
    'use strict';

    interface IPropertiesStepViewModelScope extends IWizardCreationStepScope {
        component: Models.Components.Component;
        tableHeadersList: Array<any>;
        reverse: boolean;
        sortBy:string;

        addOrUpdateProperty(): void;
        delete(property: Models.PropertyModel): void;
        sort(sortBy:string): void;
    }

    export class PropertiesStepViewModel implements IWizardCreationStep {

        static '$inject' = [
            '$scope',
            '$filter',
            '$modal',
            '$templateCache',
            'ModalsHandler'
        ];


        constructor(
            private $scope:IPropertiesStepViewModelScope,
            private $filter:ng.IFilterService,
            private $modal:ng.ui.bootstrap.IModalService,
            private $templateCache:ng.ITemplateCacheService,
            private ModalsHandler: Utils.ModalsHandler
        ){

            this.$scope.registerChild(this);
            this.$scope.setValidState(true);
            this.initScope();
        }

        public save = (callback:Function):void => {
            this.$scope.setComponent(this.$scope.component);
            callback(true);
        };

        public back = (callback:Function):void => {
            this.$scope.setComponent(this.$scope.component);
            callback(true);
        }


        private openEditPropertyModal = (property: Models.PropertyModel): void => {
            let viewModelsHtmlBasePath: string = '/app/scripts/view-models/';

            let modalOptions: ng.ui.bootstrap.IModalSettings = {
                template: this.$templateCache.get(viewModelsHtmlBasePath+'wizard/property-form/property-form.html'),
                controller: 'Sdc.ViewModels.Wizard.PropertyFormViewModel',
                size: 'sdc-md',
                backdrop: 'static',
                keyboard: false,
                resolve: {
                    property: (): Models.PropertyModel => {
                        return property;
                    },
                    component: (): Models.Components.Component => {
                        return <Models.Components.Component> this.$scope.getComponent();
                    }
                }
            };
             this.$modal.open(modalOptions);
        };

        private initScope = (): void => {

            let self = this;
            this.$scope.component =  this.$scope.getComponent();
            this.$scope.sortBy = 'name';
            this.$scope.reverse = false;

            this.$scope.tableHeadersList = [
                {title:'Name', property: 'name'},
                {title:'Type', property: 'type'},
                {title:'Default Value', property: 'defaultValue'}
            ];
            this.$scope.sort = (sortBy:string):void => {
                this.$scope.reverse = (this.$scope.sortBy === sortBy) ?  !this.$scope.reverse : false;
                this.$scope.sortBy = sortBy;
            };

            this.$scope.addOrUpdateProperty = (property?: Models.PropertyModel): void => {
                this.openEditPropertyModal(property ? property : new Models.PropertyModel());
            };

            this.$scope.delete = (property: Models.PropertyModel): void => {

                let onOk = (): void => {
                    this.$scope.component.deleteProperty(property.uniqueId);
                };
                let title:string =  this.$filter('translate')("PROPERTY_VIEW_DELETE_MODAL_TITLE");
                let message:string = this.$filter('translate')("PROPERTY_VIEW_DELETE_MODAL_TEXT", "{'name': '" + property.name + "'}");
                this.ModalsHandler.openConfirmationModal(title, message, false).then(onOk);
            };
        }
    }
}
