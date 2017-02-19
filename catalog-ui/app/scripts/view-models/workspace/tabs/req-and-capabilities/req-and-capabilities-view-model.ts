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
 * Created by rcohen on 9/22/2016.
 */
/// <reference path="../../../../references"/>
module Sdc.ViewModels {
    'use strict';
    import tree = d3.layout.tree;

    export class SortTableDefined {
        reverse:boolean;
        sortByField:string;
    }

    interface IReqAndCapabilitiesViewModelScope extends IWorkspaceViewModelScope {
        requirementsTableHeadersList: Array<any>;
        capabilitiesTableHeadersList: Array<any>;
        capabilityPropertiesTableHeadersList: Array<any>;
        requirementsSortTableDefined: SortTableDefined;
        capabilitiesSortTableDefined: SortTableDefined;
        propertiesSortTableDefined: SortTableDefined;
        requirements:Array<Models.Requirement>;
        capabilities:Array<Models.Capability>;
        mode:string;
        filteredProperties:Array<Array<Models.PropertyModel>>;
        searchText:string;

        sort(sortBy:string, sortByTableDefined:SortTableDefined):void;
        updateProperty(property:Models.PropertyModel, indexInFilteredProperties:number):void;
        allCapabilitiesSelected(selected:boolean):void;
    }

    export class ReqAndCapabilitiesViewModel {

        static '$inject' = [
            '$scope',
            '$filter',
            '$modal',
            '$templateCache',
            'ModalsHandler'
        ];


        constructor(private $scope:IReqAndCapabilitiesViewModelScope,
                    private $filter:ng.IFilterService,
                    private $modal:ng.ui.bootstrap.IModalService,
                    private $templateCache:ng.ITemplateCacheService) {
            this.initScope();
            this.$scope.updateSelectedMenuItem();
        }


        private openEditPropertyModal = (property:Models.PropertyModel, indexInFilteredProperties:number):void => {
            let viewModelsHtmlBasePath:string = '/app/scripts/view-models/';
            //...because there is not be api
            _.forEach(this.$scope.filteredProperties[indexInFilteredProperties],(prop:Models.PropertyModel)=>{
                prop.readonly = true;
            });
            let modalOptions:ng.ui.bootstrap.IModalSettings = {
                template: this.$templateCache.get(viewModelsHtmlBasePath + 'forms/property-form/property-form-view.html'),
                controller: 'Sdc.ViewModels.PropertyFormViewModel',
                size: 'sdc-l',
                backdrop: 'static',
                keyboard: false,
                resolve: {
                    property: ():Models.PropertyModel => {
                        return property;
                    },
                    component: ():Models.Components.Component => {
                        return <Models.Components.Component> this.$scope.component;
                    },
                    filteredProperties: ():Array<Models.PropertyModel> => {
                        return this.$scope.filteredProperties[indexInFilteredProperties];
                    }
                }
            };
            this.$modal.open(modalOptions);
        };

        private initScope = ():void => {

            this.$scope.requirementsSortTableDefined = {
                reverse: false,
                sortByField: 'name'
            };
            this.$scope.capabilitiesSortTableDefined = {
                reverse: false,
                sortByField: 'name'
            };
            this.$scope.propertiesSortTableDefined = {
                reverse: false,
                sortByField: 'name'
            };

            this.$scope.setValidState(true);
            this.$scope.requirementsTableHeadersList = [
                {title: 'Name', property: 'name'},
                {title: 'Capability', property: 'capability'},
                {title: 'Node', property: 'node'},
                {title: 'Relationship', property: 'relationship'},
                {title: 'Connected To', property: ''},
                {title: 'Occurrences', property: ''}
            ];
            this.$scope.capabilitiesTableHeadersList = [
                {title: 'Name', property: 'name'},
                {title: 'Type', property: 'type'},
                {title: 'Description', property: ''},
                {title: 'Valid Source', property: ''},
                {title: 'Occurrences', property: ''}
            ];
            this.$scope.capabilityPropertiesTableHeadersList = [
                {title: 'Name', property: 'name'},
                {title: 'Type', property: 'type'},
                {title: 'Schema', property: 'schema.property.type'},
                {title: 'Description', property: 'description'},
            ];
            this.$scope.filteredProperties=[];

            this.$scope.mode='requirements';
            this.$scope.requirements=[];
            _.forEach(this.$scope.component.requirements,(req:Array<Models.Requirement>,capName)=>{
                this.$scope.requirements=this.$scope.requirements.concat(req);
            });

            this.$scope.capabilities=[];
            _.forEach(this.$scope.component.capabilities,(cap:Array<Models.Capability>,capName)=>{
                this.$scope.capabilities=this.$scope.capabilities.concat(cap);
            });

            this.$scope.sort = (sortBy:string, sortByTableDefined:SortTableDefined):void => {
                sortByTableDefined.reverse = (sortByTableDefined.sortByField === sortBy) ? !sortByTableDefined.reverse : false;
                sortByTableDefined.sortByField = sortBy;
            };

            this.$scope.updateProperty = (property:Models.PropertyModel, indexInFilteredProperties:number):void => {
                this.openEditPropertyModal(property, indexInFilteredProperties);
            };

            this.$scope.allCapabilitiesSelected = (selected:boolean):void => {
                _.forEach(this.$scope.capabilities,(cap:Models.Capability)=>{
                    cap.selected = selected;
                });
            };
        }
    }
}

