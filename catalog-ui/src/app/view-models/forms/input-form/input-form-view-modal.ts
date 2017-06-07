'use strict';
import {FormState, PROPERTY_TYPES, ValidationUtils, PROPERTY_VALUE_CONSTRAINTS} from "app/utils";
import {InputModel} from "app/models";

export  interface IInputEditModel {
    editInput:InputModel;
}

export interface IInputFormViewModelScope extends ng.IScope {
    forms:any;
    editForm:ng.IFormController;
    footerButtons:Array<any>;
    isService:boolean;
    modalInstanceInput:ng.ui.bootstrap.IModalServiceInstance;
    isLoading:boolean;
    inputEditModel:IInputEditModel;
    myValue:any;
    maxLength:number;

    save():void;
    close():void;
    validateIntRange(value:string):boolean;
    validateJson(json:string):boolean;
    getValidationPattern(type:string):RegExp;
    showSchema():boolean;
}

export class InputFormViewModel {

    static '$inject' = [
        '$scope',
        '$uibModalInstance',
        'ValidationUtils',
        'input'
    ];

    private formState:FormState;


    constructor(private $scope:IInputFormViewModelScope,
                private $uibModalInstance:ng.ui.bootstrap.IModalServiceInstance,
                private ValidationUtils:ValidationUtils,
                private input:InputModel) {
        this.initScope();
        this.initMyValue();
    }

    private initMyValue = ():void => {
        switch (this.$scope.inputEditModel.editInput.type) {
            case PROPERTY_TYPES.MAP:
                this.$scope.myValue = this.$scope.inputEditModel.editInput.defaultValue ? JSON.parse(this.$scope.inputEditModel.editInput.defaultValue) : {'': null};
                break;
            case PROPERTY_TYPES.LIST:
                this.$scope.myValue = this.$scope.inputEditModel.editInput.defaultValue ? JSON.parse(this.$scope.inputEditModel.editInput.defaultValue) : [];
                break;
        }
    };

    private initDefaultValueMaxLength = ():void => {
        switch (this.$scope.inputEditModel.editInput.type) {
            case PROPERTY_TYPES.MAP:
            case PROPERTY_TYPES.LIST:
                this.$scope.maxLength = this.$scope.inputEditModel.editInput.schema.property.type == PROPERTY_TYPES.JSON ?
                    PROPERTY_VALUE_CONSTRAINTS.JSON_MAX_LENGTH :
                    PROPERTY_VALUE_CONSTRAINTS.MAX_LENGTH;
                break;
            case PROPERTY_TYPES.JSON:
                this.$scope.maxLength = PROPERTY_VALUE_CONSTRAINTS.JSON_MAX_LENGTH;
                break;
            default:
                this.$scope.maxLength = PROPERTY_VALUE_CONSTRAINTS.MAX_LENGTH;
        }
    };

    private initScope = ():void => {
        this.$scope.forms = {};
        this.$scope.modalInstanceInput = this.$uibModalInstance;
        this.$scope.inputEditModel = {
            editInput: null
        };
        this.$scope.inputEditModel.editInput = this.input;
        this.initDefaultValueMaxLength();

        //scope methods
        this.$scope.save = ():void => {
            if (this.$scope.showSchema()) {
                this.$scope.inputEditModel.editInput.defaultValue = JSON.stringify(this.$scope.myValue);
            }
        };

        this.$scope.close = ():void => {
            this.$uibModalInstance.close();
        };

        this.$scope.validateIntRange = (value:string):boolean => {
            return !value || this.ValidationUtils.validateIntRange(value);
        };

        this.$scope.validateJson = (json:string):boolean => {
            if (!json) {
                return true;
            }
            return this.ValidationUtils.validateJson(json);
        };

        this.$scope.showSchema = ():boolean => {
            return ['list', 'map'].indexOf(this.$scope.inputEditModel.editInput.type) > -1;
        };

        this.$scope.getValidationPattern = (type:string):RegExp => {
            return this.ValidationUtils.getValidationPattern(type);
        };

        // Add the done button at the footer.
        this.$scope.footerButtons = [
            {'name': 'Done', 'css': 'blue', 'callback': this.$scope.save},
            {'name': 'Cancel', 'css': 'grey', 'callback': this.$scope.close}
        ];

        this.$scope.$watchCollection("forms.editForm.$invalid", (newVal, oldVal) => {
            this.$scope.footerButtons[0].disabled = this.$scope.forms.editForm.$invalid;
        });

    };
}

