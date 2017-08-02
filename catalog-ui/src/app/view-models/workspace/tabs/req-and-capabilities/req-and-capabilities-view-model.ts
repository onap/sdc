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
'use strict';
import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";
import {ModalsHandler} from "app/utils";
import {Capability, PropertyModel, Requirement} from "app/models";
import {ComponentGenericResponse} from "../../../../ng2/services/responses/component-generic-response";
import {ComponentServiceNg2} from "../../../../ng2/services/component-services/component.service";

export class SortTableDefined {
    reverse:boolean;
    sortByField:string;
}

interface IReqAndCapabilitiesViewModelScope extends IWorkspaceViewModelScope {
    requirementsTableHeadersList:Array<any>;
    capabilitiesTableHeadersList:Array<any>;
    capabilityPropertiesTableHeadersList:Array<any>;
    requirementsSortTableDefined:SortTableDefined;
    capabilitiesSortTableDefined:SortTableDefined;
    propertiesSortTableDefined:SortTableDefined;
    requirements:Array<Requirement>;
    capabilities:Array<Capability>;
    mode:string;
    filteredProperties:Array<Array<PropertyModel>>;
    searchText:string;

    sort(sortBy:string, sortByTableDefined:SortTableDefined):void;
    updateProperty(property:PropertyModel, indexInFilteredProperties:number):void;
    allCapabilitiesSelected(selected:boolean):void;
}

export class ReqAndCapabilitiesViewModel {

    static '$inject' = [
        '$scope',
        '$filter',
        'ModalsHandler',
        'ComponentServiceNg2'
    ];


    constructor(private $scope:IReqAndCapabilitiesViewModelScope,
                private $filter:ng.IFilterService,
                private ModalsHandler:ModalsHandler,
                private ComponentServiceNg2: ComponentServiceNg2) {

        this.initCapabilitiesAndRequirements();
    }

    private initCapabilitiesAndRequirements = (): void => {

        if(!this.$scope.component.capabilities || !this.$scope.component.requirements) {
            this.$scope.isLoading = true;
            this.ComponentServiceNg2.getCapabilitiesAndRequirements(this.$scope.component.componentType, this.$scope.component.uniqueId).subscribe((response:ComponentGenericResponse) => {
                this.$scope.component.capabilities = response.capabilities;
                this.$scope.component.requirements = response.requirements;
                this.initScope();
                this.$scope.isLoading = false;
            }, () => {
                this.$scope.isLoading = false;
            });
        } else {
            this.initScope();
        }

    }

    private openEditPropertyModal = (property:PropertyModel, indexInFilteredProperties:number):void => {
        //...because there is not be api
        _.forEach(this.$scope.filteredProperties[indexInFilteredProperties], (prop:PropertyModel)=> {
            prop.readonly = true;
        });
        this.ModalsHandler.openEditPropertyModal(property, this.$scope.component, this.$scope.filteredProperties[indexInFilteredProperties], false).then(() => {

        });
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
        this.$scope.filteredProperties = [];

        this.$scope.mode = 'requirements';
        this.$scope.requirements = [];
        _.forEach(this.$scope.component.requirements, (req:Array<Requirement>, capName)=> {
            this.$scope.requirements = this.$scope.requirements.concat(req);
        });

        this.$scope.capabilities = [];
        _.forEach(this.$scope.component.capabilities, (cap:Array<Capability>, capName)=> {
            this.$scope.capabilities = this.$scope.capabilities.concat(cap);
        });

        this.$scope.sort = (sortBy:string, sortByTableDefined:SortTableDefined):void => {
            sortByTableDefined.reverse = (sortByTableDefined.sortByField === sortBy) ? !sortByTableDefined.reverse : false;
            sortByTableDefined.sortByField = sortBy;
        };

        this.$scope.updateProperty = (property:PropertyModel, indexInFilteredProperties:number):void => {
            this.openEditPropertyModal(property, indexInFilteredProperties);
        };

        this.$scope.allCapabilitiesSelected = (selected:boolean):void => {
            _.forEach(this.$scope.capabilities, (cap:Capability)=> {
                cap.selected = selected;
            });
        };
    }
}

