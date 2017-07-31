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
import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";
import {Component, AttributeModel} from "app/models";
import {ModalsHandler} from "app/utils";
import {ComponentServiceNg2} from "../../../../ng2/services/component-services/component.service";
import {ComponentGenericResponse} from "../../../../ng2/services/responses/component-generic-response";

interface IAttributesViewModelScope extends IWorkspaceViewModelScope {
    tableHeadersList:Array<any>;
    reverse:boolean;
    sortBy:string;

    addOrUpdateAttribute(attribute?:AttributeModel):void;
    delete(attribute:AttributeModel):void;
    sort(sortBy:string):void;
}

export class AttributesViewModel {

    static '$inject' = [
        '$scope',
        '$filter',
        '$uibModal',
        'ModalsHandler',
        'ComponentServiceNg2'
    ];


    constructor(private $scope:IAttributesViewModelScope,
                private $filter:ng.IFilterService,
                private $uibModal:ng.ui.bootstrap.IModalService,
                private ModalsHandler:ModalsHandler,
                private ComponentServiceNg2: ComponentServiceNg2) {

        this.initComponentAttributes();
    }

    private initComponentAttributes = () => {
        if(this.$scope.component.attributes) {
            this.initScope();
        } else {
            this.ComponentServiceNg2.getComponentAttributes(this.$scope.component).subscribe((response:ComponentGenericResponse) => {
                this.$scope.component.attributes = response.attributes;
                this.initScope();
            });
        }
    }


    private initScope = ():void => {

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

        this.$scope.addOrUpdateAttribute = (attribute?:AttributeModel):void => {
            this.ModalsHandler.openEditAttributeModal(attribute ? attribute : new AttributeModel(), this.$scope.component);
        };

        this.$scope.delete = (attribute:AttributeModel):void => {

            let onOk = ():void => {
                this.$scope.component.deleteAttribute(attribute.uniqueId);
            };
            let title:string = this.$filter('translate')("ATTRIBUTE_VIEW_DELETE_MODAL_TITLE");
            let message:string = this.$filter('translate')("ATTRIBUTE_VIEW_DELETE_MODAL_TEXT", "{'name': '" + attribute.name + "'}");
            this.ModalsHandler.openConfirmationModal(title, message, false).then(onOk);
        };
    }
}
