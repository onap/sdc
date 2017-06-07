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
        this.$scope.updateSelectedMenuItem();
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
