'use strict';
import {DataTypesService} from "app/services/data-types-service";
import {PropertyModel, InputPropertyBase, Component} from "app/models";
import {IPropertyFormBaseViewScope, PropertyFormBaseView} from "../base-property-form/property-form-base-model";
import {PROPERTY_TYPES} from "app/utils/constants";

interface ISelectDataTypeViewModelScope extends IPropertyFormBaseViewScope {
    selectedPropertiesName:string;
    dataTypesService:DataTypesService;
    path:string;
    isTypeDataType:boolean;
    myValue:any;
    isReadOnly:boolean;
}

export class SelectDataTypeViewModel extends PropertyFormBaseView {

    static '$inject' = [
        '$scope',
        '$templateCache',
        '$uibModalInstance',
        '$injector',
        'originalProperty',
        'component',
        'filteredProperties',
        'Sdc.Services.DataTypesService',
        'propertiesMap',
        '$q'
    ];

    constructor(protected $scope:ISelectDataTypeViewModelScope,
                protected $templateCache:ng.ITemplateCacheService,
                protected $uibModalInstance:ng.ui.bootstrap.IModalServiceInstance,
                protected $injector:ng.auto.IInjectorService,
                protected originalProperty:PropertyModel,
                protected component:Component,
                protected filteredProperties:Array<PropertyModel>,
                protected DataTypesService:DataTypesService,
                private propertiesMap:Array<InputPropertyBase>,
                private $q:ng.IQService) {
        super($scope, $uibModalInstance, $injector, originalProperty, component, filteredProperties, DataTypesService);

        this.$templateCache.put("select-datatype-modal-view.html", require('app/view-models/forms/property-forms/select-datatype-modal/select-datatype-modal-view.html'));
        this.$scope.innerViewSrcUrl = "select-datatype-modal-view.html";
        this.initChildScope();
    }

    //scope methods
    save(isNeedToCloseModal):ng.IPromise<boolean> {
        let deferred = this.$q.defer();
        this.$scope.property.propertiesName = this.DataTypesService.selectedPropertiesName;
        this.$scope.property.input = this.DataTypesService.selectedInput;
        this.$scope.property.isAlreadySelected = true;
        this.$uibModalInstance.close(this.$scope.property);
        deferred.resolve(true);
        return deferred.promise;
    };

    private initForNotSimpleType = ():void => {
        let property = this.$scope.property;
        this.$scope.isTypeDataType = this.DataTypesService.isDataTypeForPropertyType(this.$scope.property);
        if (property.type && this.$scope.simpleTypes.indexOf(property.type) == -1) {
            if (!(property.value || property.defaultValue)) {
                switch (property.type) {
                    case PROPERTY_TYPES.MAP:
                        this.$scope.myValue = {'': null};
                        break;
                    case PROPERTY_TYPES.LIST:
                        this.$scope.myValue = [];
                        break;
                    default:
                        this.$scope.myValue = {};
                }
            } else {
                this.$scope.myValue = JSON.parse(property.value || property.defaultValue);
            }
        }
    };

    //remove selection property on the modal
    private removeSelected = ():void => {
        this.DataTypesService.selectedPropertiesName = null;
        this.DataTypesService.selectedInput = null;
    };

    private initChildScope = ():void => {
        //scope properties
        this.$scope.forms = {};
        this.$scope.path = this.$scope.property.name;
        this.$scope.isArrowsDisabled = true;
        this.DataTypesService.alreadySelectedProperties = this.propertiesMap;
        this.$scope.dataTypesService = this.DataTypesService;
        this.$scope.isReadOnly = true;
        this.initForNotSimpleType();
        this.removeSelected();
    }
}
