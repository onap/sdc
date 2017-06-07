/**
 * Created by obarda on 1/19/2017.
 */
'use strict';
import {DataTypesService} from "app/services/data-types-service";
import {PropertyModel, DataTypesMap, Component} from "app/models";
import {ValidationUtils, PROPERTY_DATA} from "app/utils";

export interface IPropertyFormBaseViewScope extends ng.IScope {

    forms:any;
    editForm:ng.IFormController;

    property:PropertyModel;
    types:Array<string>;
    nonPrimitiveTypes:Array<string>;
    simpleTypes:Array<string>;

    footerButtons:Array<any>;
    modalPropertyFormBase:ng.ui.bootstrap.IModalServiceInstance;
    currentPropertyIndex:number;
    isLastProperty:boolean;
    innerViewSrcUrl:string;

    //Disabling filed - each child controller can change this when needed
    isNew:boolean;
    isTypeSelectorDisable:boolean;
    isDeleteDisable:boolean;
    isNameDisable:boolean;
    isDescriptionDisable:boolean;
    isPropertyValueDisable:boolean;
    isArrowsDisabled:boolean;

    //Validation pattern
    validationPattern:RegExp;
    propertyNameValidationPattern:RegExp;
    commentValidationPattern:RegExp;
    numberValidationPattern:RegExp;

    dataTypes:DataTypesMap;

    isLoading:boolean;

    save():void;
    close():void;
    getNext():void;
    getPrev():void;
    getValidationPattern(type:string):RegExp;
}

export abstract class PropertyFormBaseView {


    constructor(protected $scope:IPropertyFormBaseViewScope,
                protected $uibModalInstance:ng.ui.bootstrap.IModalServiceInstance,
                protected $injector:ng.auto.IInjectorService,
                protected originalProperty:PropertyModel,
                protected component:Component,
                protected filteredProperties:Array<PropertyModel>,
                protected DataTypesService:DataTypesService) {

        this.initScope();

    }

    protected validationUtils:ValidationUtils;

    protected isPropertyValueOwner = ():boolean => {
        return this.component.isService() || !!this.component.selectedInstance;
    };

    private isDisable = ():boolean => {
        return this.isPropertyValueOwner() || this.$scope.property.readonly;
    };


    //This is the difault state, Childs screens can change if needed
    protected initButtonsState = ():void => {
        let isDisable = this.isDisable();

        this.$scope.isArrowsDisabled = false;
        this.$scope.isDeleteDisable = isDisable;
        this.$scope.isDescriptionDisable = isDisable;
        this.$scope.isNameDisable = isDisable;
        this.$scope.isTypeSelectorDisable = isDisable;
        this.$scope.isPropertyValueDisable = this.$scope.property.readonly && !this.isPropertyValueOwner();
    };

    protected initValidations = ():void => {

        this.$scope.validationPattern = this.$injector.get('ValidationPattern');
        this.$scope.propertyNameValidationPattern = this.$injector.get('PropertyNameValidationPattern');
        this.$scope.commentValidationPattern = this.$injector.get('CommentValidationPattern');
        this.$scope.numberValidationPattern = this.$injector.get('NumberValidationPattern');
        this.validationUtils = this.$injector.get('ValidationUtils');
    };

    //Functions implemented on child's scope if needed
    abstract save(doNotCloseModal?:boolean):ng.IPromise<boolean>;

    protected onPropertyChange():void {
    };

    private updatePropertyByIndex = (index:number):void => {
        this.$scope.property = new PropertyModel(this.filteredProperties[index]);
        this.$scope.isLastProperty = this.$scope.currentPropertyIndex == (this.filteredProperties.length - 1);
        this.onPropertyChange();
    };

    private initScope = ():void => {

        this.$scope.forms = {};
        this.$scope.isLoading = false;
        this.$scope.property = new PropertyModel(this.originalProperty); //we create a new Object so if user press cance we won't update the property
        this.$scope.types = PROPERTY_DATA.TYPES; //All types - simple type + map + list
        this.$scope.simpleTypes = PROPERTY_DATA.SIMPLE_TYPES; //All simple types
        this.$scope.dataTypes = this.DataTypesService.getAllDataTypes(); //Get all data types in service
        this.$scope.modalPropertyFormBase = this.$uibModalInstance;
        this.$scope.isNew = !angular.isDefined(this.$scope.property.name);

        this.initValidations();
        this.initButtonsState();
        this.filteredProperties = _.sortBy(this.filteredProperties, 'name');
        this.$scope.currentPropertyIndex = _.findIndex(this.filteredProperties, propety => propety.uniqueId == this.$scope.property.uniqueId);
        this.$scope.isLastProperty = this.$scope.currentPropertyIndex == (this.filteredProperties.length - 1);

        this.$scope.nonPrimitiveTypes = _.filter(Object.keys(this.$scope.dataTypes), (type:string)=> {
            return this.$scope.types.indexOf(type) == -1;
        });

        this.$scope.close = ():void => {
            this.$uibModalInstance.close();
        };

        this.$scope.save = ():void => {

            let onSuccess = ():void => {
                this.$scope.isLoading = false;
            };
            let onFailed = ():void => {
                this.$scope.isLoading = false;
            };

            this.$scope.isLoading = true;
            this.save(true).then(onSuccess, onFailed); // Child controller implement save logic
        };

        // Add the done button at the footer.
        this.$scope.footerButtons = [
            {'name': 'Save', 'css': 'blue', 'callback': this.$scope.save},
            {'name': 'Cancel', 'css': 'grey', 'callback': this.$scope.close}
        ];


        this.$scope.getPrev = ():void=> {

            let onSuccess = ():void => {
                this.$scope.isLoading = false;
                this.updatePropertyByIndex(--this.$scope.currentPropertyIndex);
            };
            let onFailed = ():void => {
                this.$scope.isLoading = false;
            };

            if (!this.$scope.property.readonly) {
                this.$scope.isLoading = true;
                this.save(false).then(onSuccess, onFailed);

            } else {
                this.updatePropertyByIndex(--this.$scope.currentPropertyIndex);
            }

        };

        this.$scope.getNext = ():void=> {

            let onSuccess = ():void => {
                this.$scope.isLoading = false;
                this.updatePropertyByIndex(++this.$scope.currentPropertyIndex);
            };
            let onFailed = ():void => {
                this.$scope.isLoading = false;
            };

            if (!this.$scope.property.readonly) {
                this.$scope.isLoading = true;
                this.save(false).then(onSuccess, onFailed);
            } else {
                this.updatePropertyByIndex(++this.$scope.currentPropertyIndex);
            }

        };


        this.$scope.$watch("forms.editForm.$invalid", (newVal, oldVal) => {
            this.$scope.footerButtons[0].disabled = this.$scope.forms.editForm.$invalid;
        });


        this.$scope.getValidationPattern = (type:string):RegExp => {
            return this.validationUtils.getValidationPattern(type);
        };
    }
}
