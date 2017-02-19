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
/// <reference path="../../../../references"/>
module Sdc.ViewModels {
    'use strict';

    interface IAttributesViewModelScope extends IWorkspaceViewModelScope {
        tableHeadersList: Array<any>;
        reverse: boolean;
        sortBy:string;

        addOrUpdateAttribute(attribute?:Models.AttributeModel): void;
        delete(attribute:Models.AttributeModel): void;
        sort(sortBy:string): void;
    }

    export class AttributesViewModel {

        static '$inject' = [
            '$scope',
            '$filter',
            '$modal',
            '$templateCache',
            'ModalsHandler'
        ];


        constructor(private $scope:IAttributesViewModelScope,
                    private $filter:ng.IFilterService,
                    private $modal:ng.ui.bootstrap.IModalService,
                    private $templateCache:ng.ITemplateCacheService,
                    private ModalsHandler:Utils.ModalsHandler) {
            this.initScope();
            this.$scope.updateSelectedMenuItem();
        }


        private openEditAttributeModal = (attribute:Models.AttributeModel):void => {
            let viewModelsHtmlBasePath:string = '/app/scripts/view-models/';

            let modalOptions:ng.ui.bootstrap.IModalSettings = {
                template: this.$templateCache.get(viewModelsHtmlBasePath + 'forms/attribute-form/attribute-form-view.html'),
                controller: 'Sdc.ViewModels.AttributeFormViewModel',
                size: 'sdc-md',
                backdrop: 'static',
                keyboard: false,
                resolve: {
                    attribute: ():Models.AttributeModel => {
                        return attribute;
                    },
                    component: ():Models.Components.Component => {
                        return <Models.Components.Component> this.$scope.component;
                    }
                }
            };
            this.$modal.open(modalOptions);
        };

        private initScope = ():void => {

            //let self = this;
            this.$scope.sortBy = 'name';
            this.$scope.reverse = false;
            this.$scope.setValidState(true);
            this.$scope.tableHeadersList = [
                {title: 'Name', property: 'name'},
                {title: 'Type', property: 'type'},
                {title: 'Default Value', property: 'defaultValue'}
            ];
            this.$scope.sort = (sortBy:string):void => {
                this.$scope.reverse = (this.$scope.sortBy === sortBy) ? !this.$scope.reverse : false;
                this.$scope.sortBy = sortBy;
            };

            this.$scope.addOrUpdateAttribute = (attribute?:Models.AttributeModel):void => {
                this.openEditAttributeModal(attribute ? attribute : new Models.AttributeModel());
            };

            this.$scope.delete = (attribute:Models.AttributeModel):void => {

                let onOk = ():void => {
                    this.$scope.component.deleteAttribute(attribute.uniqueId);
                };
                let title:string = this.$filter('translate')("ATTRIBUTE_VIEW_DELETE_MODAL_TITLE");
                let message:string = this.$filter('translate')("ATTRIBUTE_VIEW_DELETE_MODAL_TEXT", "{'name': '" + attribute.name + "'}");
                this.ModalsHandler.openConfirmationModal(title, message, false).then(onOk);
            };
        }
    }
}
