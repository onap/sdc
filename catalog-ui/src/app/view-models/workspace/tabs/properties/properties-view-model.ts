'use strict';
import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";
import {PropertyModel} from "app/models";
import {ModalsHandler} from "app/utils";
import {COMPONENT_FIELDS} from "../../../../utils/constants";
import {ComponentGenericResponse} from "../../../../ng2/services/responses/component-generic-response";
import {ComponentServiceNg2} from "../../../../ng2/services/component-services/component.service";

interface IPropertiesViewModelScope extends IWorkspaceViewModelScope {
    tableHeadersList:Array<any>;
    reverse:boolean;
    sortBy:string;
    filteredProperties:Array<PropertyModel>;

    addOrUpdateProperty(property?:PropertyModel):void;
    delete(property:PropertyModel):void;
    sort(sortBy:string):void;
}

export class PropertiesViewModel {

    static '$inject' = [
        '$scope',
        '$filter',
        'ModalsHandler',
        'ComponentServiceNg2'
    ];


    constructor(private $scope:IPropertiesViewModelScope,
                private $filter:ng.IFilterService,
                private ModalsHandler:ModalsHandler,
                private ComponentServiceNg2:ComponentServiceNg2) {
        this.initComponentProperties();
        this.$scope.updateSelectedMenuItem();
    }

    private initComponentProperties = ():void => {

        if(!this.$scope.component.properties) {
            this.$scope.isLoading = true;
            this.ComponentServiceNg2.getComponentProperties(this.$scope.component).subscribe((response:ComponentGenericResponse) => {
                this.$scope.component.properties = response.properties;
                this.initScope();
                this.$scope.isLoading = false;
            }, () => {
                this.$scope.isLoading = false;
            });
        } else {
            this.initScope();
        }
    }

    private openEditPropertyModal = (property:PropertyModel):void => {
        this.ModalsHandler.openEditPropertyModal(property, this.$scope.component, this.$scope.filteredProperties, false).then(() => {
        });
    };

    private initScope = ():void => {

        //let self = this;
        this.$scope.filteredProperties = this.$scope.component.properties;
        this.$scope.sortBy = 'name';
        this.$scope.reverse = false;
        this.$scope.setValidState(true);
        this.$scope.tableHeadersList = [
            {title: 'Name', property: 'name'},
            {title: 'Type', property: 'type'},
            {title: 'Schema', property: 'schema.property.type'},
            {title: 'Description', property: 'description'},
        ];
        this.$scope.sort = (sortBy:string):void => {
            this.$scope.reverse = (this.$scope.sortBy === sortBy) ? !this.$scope.reverse : false;
            this.$scope.sortBy = sortBy;
        };


        this.$scope.addOrUpdateProperty = (property?:PropertyModel):void => {
            this.openEditPropertyModal(property ? property : new PropertyModel());
        };

        this.$scope.delete = (property:PropertyModel):void => {

            let onOk = ():void => {
                this.$scope.component.deleteProperty(property.uniqueId);
            };
            let title:string = this.$filter('translate')("PROPERTY_VIEW_DELETE_MODAL_TITLE");
            let message:string = this.$filter('translate')("PROPERTY_VIEW_DELETE_MODAL_TEXT", "{'name': '" + property.name + "'}");
            this.ModalsHandler.openConfirmationModal(title, message, false).then(onOk);
        };
    }
}
